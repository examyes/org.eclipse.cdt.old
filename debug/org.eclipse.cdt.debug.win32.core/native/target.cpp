/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 ******************************************************************************/
#include <windows.h>
#include <dbghelp.h>
#include <jni.h>
#include <map>

using namespace std;

// IDs for Java methods/fields we want access to
static jmethodID handleBreakpointID;
static jmethodID createThreadID;
static jmethodID handleExitProcessID;

#define TEMPORARY_BREAKPOINT 0x100

class WinDbgTarget
{
public:
	WinDbgTarget(char * cmd) : cmd(cmd), bpKeep(0) { }
	void debugLoop(JNIEnv * env, jobject obj);
	DEBUG_EVENT & getDebugEvent() { return debugEvent; }	
	long getFunctionAddress(const char * symbol);
	long getLineAddress(const char * file, int lineNumber);
	void setBreakpoint(long address, bool temporary);

private:
	char * cmd;
	HANDLE imageFile;
	HANDLE process;
	// This should turn into a map once we handle more than one thread
	HANDLE thread;
	
	// Debug event stuff
	DEBUG_EVENT debugEvent;
	
	// lower 8 bits - the underwritten byte
	// upper 8 bits > 0 if temporary breakpoint (i.e. don't reset)
	typedef map<jlong, short> BPStore;
	BPStore bpStore;
	DWORD64 bpKeep;
	
	// Event handlers
	void handleCreateProcess(JNIEnv * env, jobject obj);
	void handleBreakpoint(JNIEnv * env, jobject obj);
	void handleSingleStep();
	void handleExitProcess(JNIEnv * env, jobject obj);
};

static void reportError(const wchar_t * title)
{
	LPVOID lpMsgBuf;
	FormatMessage(
	    FORMAT_MESSAGE_ALLOCATE_BUFFER | 
	    FORMAT_MESSAGE_FROM_SYSTEM | 
	    FORMAT_MESSAGE_IGNORE_INSERTS,
	    NULL,
	    GetLastError(),
	    MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
	    (LPTSTR) &lpMsgBuf,
	    0,
	    NULL );
	
	// Process any inserts in lpMsgBuf.
	// ...
	
	// Display the string.
	MessageBox( NULL, (LPCTSTR)lpMsgBuf, title, MB_OK | MB_ICONINFORMATION );
	
	// Free the buffer.
	LocalFree( lpMsgBuf );
	
}

void WinDbgTarget::debugLoop(JNIEnv * env, jobject obj)
{
	while (true) {
		DWORD status = DBG_CONTINUE;

		if (!WaitForDebugEvent(&debugEvent, INFINITE))
			return;
		
		switch (debugEvent.dwDebugEventCode) {
			case CREATE_PROCESS_DEBUG_EVENT:
			{
				handleCreateProcess(env, obj);
				status = DBG_CONTINUE;
				break;
			}
			case EXCEPTION_DEBUG_EVENT:
			{
				switch (debugEvent.u.Exception.ExceptionRecord.ExceptionCode) {
					case EXCEPTION_BREAKPOINT:
						handleBreakpoint(env, obj);
						break;
					case EXCEPTION_SINGLE_STEP:
						handleSingleStep();
						break;
				}

				break;
			}
			case EXIT_PROCESS_DEBUG_EVENT:
				handleExitProcess(env, obj);
				return;
		}
		
		ContinueDebugEvent(debugEvent.dwProcessId, debugEvent.dwThreadId, status ? status : DBG_CONTINUE);
	}
}

void WinDbgTarget::handleCreateProcess(JNIEnv * env, jobject obj)
{
	imageFile = debugEvent.u.CreateProcessInfo.hFile;
	process = debugEvent.u.CreateProcessInfo.hProcess;
	thread = debugEvent.u.CreateProcessInfo.hThread;

	// Load in the symbol information
	SymSetOptions(SYMOPT_UNDNAME | SYMOPT_DEFERRED_LOADS);
	if (!SymInitialize(process, NULL, TRUE))
		reportError(L"SymInitialize");

	if (!SymLoadModule64(
			process,
			imageFile,
			cmd,
			cmd, 
			NULL,
			0))
		reportError(L"SymLoadModule");

	env->CallVoidMethod(obj, createThreadID, debugEvent.dwThreadId, (jlong)process, (jlong)thread);
}

void WinDbgTarget::handleBreakpoint(JNIEnv * env, jobject obj)
{
	jlong bpAddress = (jlong)debugEvent.u.Exception.ExceptionRecord.ExceptionAddress;
	BYTE byte;
	DWORD nread;
	ReadProcessMemory(process, (void *)bpAddress, &byte, 1, &nread);

	env->CallVoidMethod(obj, handleBreakpointID, bpAddress);	
	
	BPStore::iterator i = bpStore.find(bpAddress);
	if (i != bpStore.end()) {
		// It's ours, replace opcode
		BYTE opcode = i->second & 0xff;
		DWORD nwritten;
		WriteProcessMemory(process, (void *)bpAddress, &opcode, 1, &nwritten);
		
		CONTEXT context;
		ZeroMemory(&context, sizeof(context));
		context.ContextFlags = CONTEXT_CONTROL;
		if (!GetThreadContext(thread, &context))
			reportError(L"breakpoint get thread context");
		
		// Backup to the bp address
		context.Eip = bpAddress;
		
		if (!(i->second & TEMPORARY_BREAKPOINT)) {
			// keep the bpAddress and set the trap bit
			bpKeep = bpAddress;
			context.EFlags |= 0x100;
		}
		
		if (!SetThreadContext(thread, &context))
			reportError(L"breakpoint set thread context");
			
	}
}

void WinDbgTarget::handleSingleStep()
{
	if (!bpKeep)
		return;
	
	// Restore the breakpoint
	BYTE intcc = 0xcc;
	DWORD n;
	if (!WriteProcessMemory(process, (void *)bpKeep, &intcc, 1, &n)) {
		reportError(L"Breakpoint Restore Memory");
		return;
	}
	
	bpKeep = 0;
}

void WinDbgTarget::handleExitProcess(JNIEnv * env, jobject obj)
{
	env->CallVoidMethod(obj, handleExitProcessID, debugEvent.u.ExitProcess.dwExitCode);
	SymCleanup(process);
	CloseHandle(imageFile);
}

long WinDbgTarget::getFunctionAddress(const char * symbol)
{
	char * symstr = strdup(symbol);
	
	BYTE buffer[1024];
	SYMBOL_INFO * symInfo = (SYMBOL_INFO *)buffer;
	ZeroMemory(buffer, sizeof(buffer));
	symInfo->SizeOfStruct = sizeof(SYMBOL_INFO);
	symInfo->MaxNameLen = sizeof(buffer) - sizeof(SYMBOL_INFO) + 1;
	
	if (!SymFromName(process, symstr, symInfo)) {
		fprintf(stderr, "Error in SymFromName for '%s'\n", symstr);
		fflush(stderr);
		reportError(L"SymFromName");
		symInfo->Address = 0;
	}
	
	free(symstr);
	// We add three to get by the preamble
	return symInfo->Address + 3;
}

struct FindLineAddressData {
	HANDLE process;
	char * file;
	int lineNumber;
	jlong address;
};

BOOL CALLBACK findLineAddress(PSTR moduleName, DWORD64 baseOfDll, PVOID userContext)
{
	FindLineAddressData * data = (FindLineAddressData *)userContext;

	IMAGEHLP_MODULE64 modinfo;
	ZeroMemory(&modinfo, sizeof(modinfo));
	modinfo.SizeOfStruct = sizeof(modinfo);
	
	if (!SymGetModuleInfo64(data->process, baseOfDll, &modinfo)) {
		reportError(L"modinfo");
	}
	
	IMAGEHLP_LINE64 lineinfo;
	ZeroMemory(&lineinfo, sizeof(lineinfo));
	lineinfo.SizeOfStruct = sizeof(lineinfo);
	
	LONG displacement;
	if (!SymGetLineFromName64(
			data->process,
			modinfo.ModuleName,
			data->file,
			data->lineNumber,
			&displacement,
			&lineinfo))
	{
		reportError(L"lineinfo");
		return TRUE;
	}
	
	data->address = lineinfo.Address;
	return FALSE;
}

long WinDbgTarget::getLineAddress(const char * file, int lineNumber)
{
	FindLineAddressData data;
	data.process = process;
	data.file = strdup(file);
	data.lineNumber = lineNumber;
	data.address = 0;
	
	if (!SymEnumerateModules64(process, findLineAddress, &data)) {
		printf("Enumerate modules failed\n");
		fflush(stdout);
	}

	free(data.file);
	return data.address;
}

void WinDbgTarget::setBreakpoint(long address, bool temporary)
{
	BYTE savedByte;
	SIZE_T n;
	
	if (!ReadProcessMemory(process, (void *)address, &savedByte, 1, &n)) {
		reportError(L"Breakpoint Read Memory");
		return;
	}
	
	BYTE intcc = 0xcc;
	if (!WriteProcessMemory(process, (void *)address, &intcc, 1, &n)) {
		reportError(L"Breakpoint Write Memory");
		return;
	}
	
	short storedbp = savedByte;
	if (temporary)
		storedbp |= TEMPORARY_BREAKPOINT;
	bpStore[address] = storedbp;
}

// JNI interface

#define NATIVE(type, name) extern "C" JNIEXPORT type JNICALL Java_org_eclipse_cdt_debug_win32_core_cdi_WinDbgTarget_##name

static jfieldID pID;

NATIVE(void, initNative)(JNIEnv * env, jclass cls)
{
	pID = env->GetFieldID(cls, "p", "J");
	createThreadID = env->GetMethodID(cls, "createThread", "(IJJ)V");
	handleBreakpointID = env->GetMethodID(cls, "handleBreakpoint", "(J)V");
	handleExitProcessID = env->GetMethodID(cls, "handleExitProcess", "(I)V");
}

NATIVE(void, init)(JNIEnv * env, jobject obj, jstring cmd)
{
	jboolean copy;
	const char * cmdstr = env->GetStringUTFChars(cmd, &copy);
	
	env->SetLongField(obj, pID, (jlong)new WinDbgTarget(strdup(cmdstr)));
	
	if (copy == JNI_TRUE)
		env->ReleaseStringUTFChars(cmd, cmdstr);
}

static WinDbgTarget * getPointer(JNIEnv * env, jobject obj)
{
	return (WinDbgTarget *)env->GetLongField(obj, pID);
}

NATIVE(void, debugLoop)(JNIEnv * env, jobject obj)
{
	getPointer(env, obj)->debugLoop(env, obj);
}

NATIVE(jlong, getFunctionAddress)(JNIEnv * env, jobject obj, jstring symbol)
{
	jboolean isCopy;
	const char * symstr = env->GetStringUTFChars(symbol, &isCopy);
	
	jlong address = getPointer(env, obj)->getFunctionAddress(symstr);
	
	if (isCopy == JNI_TRUE)
		env->ReleaseStringUTFChars(symbol, symstr);
		
	return address;
}

NATIVE(jlong, getLineAddress)(JNIEnv * env, jobject obj, jstring file, jint lineNumber)
{
	jboolean copy;
	const char * filestr = env->GetStringUTFChars(file, &copy);
	
	jlong address = getPointer(env, obj)->getLineAddress(filestr, lineNumber);
	
	if (copy == JNI_TRUE)
		env->ReleaseStringUTFChars(file, filestr);
		
	return address;
}

NATIVE(void, setBreakpoint)(JNIEnv * env, jobject obj, jlong address, jboolean temporary)
{
	getPointer(env, obj)->setBreakpoint(address, temporary == JNI_TRUE);
}
