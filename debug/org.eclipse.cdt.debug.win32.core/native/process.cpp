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
#include <jni.h>
#include <stdio.h>

class WinDbgProcess
{
public:
	WinDbgProcess(const TCHAR * cmdline, const TCHAR * dirname);
	
	int read(int fd);
	void write(int fd, int b);
	int exitCode();
	int waitFor();
	void destroy();
	
private:
	HANDLE childStdinWrite;
	HANDLE childStdoutRead;
	HANDLE childStderrRead;
	PROCESS_INFORMATION pi;
};

WinDbgProcess::WinDbgProcess(const TCHAR * cmdline, const TCHAR * dirname)
{
	SECURITY_ATTRIBUTES sa;
	ZeroMemory(&sa, sizeof(sa));
	sa.nLength = sizeof(sa);
	sa.bInheritHandle = true;
    sa.lpSecurityDescriptor = NULL;
	
	class CreationException
	{
	public:
		char * msg;
		CreationException(char * msg) { this->msg = msg; }
	};

	try {
		STARTUPINFO si;
		GetStartupInfo(&si);
		si.dwFlags = STARTF_USESTDHANDLES | STARTF_USESHOWWINDOW;
		si.wShowWindow = SW_HIDE;
		
		// Create child <- parent stdin pipe
		if (!CreatePipe(&si.hStdInput, &childStdinWrite, &sa, 0))
			throw CreationException("Create stdin pipe");
		
		// Create child -> parent stdout pipe
		if (!CreatePipe(&childStdoutRead, &si.hStdOutput, &sa, 0))
			throw CreationException("Create stdout pipe");
		
		// Create child -> parent stderr pipe
		if (!CreatePipe(&childStderrRead, &si.hStdError, &sa, 0))
			throw CreationException("Create stderr pipe");
		
		// Create Process
		ZeroMemory(&pi, sizeof(pi));
		
		TCHAR * cmd = new TCHAR[wcslen(cmdline) + 1];
		wcscpy(cmd, cmdline);
		
		if (!CreateProcess(NULL,
				cmd,
				&sa,
				&sa,
				TRUE,
				CREATE_NEW_CONSOLE | DEBUG_PROCESS | DEBUG_ONLY_THIS_PROCESS,
				NULL,
				dirname,
				&si,
				&pi))
			throw CreationException("Create Process");
			
		CloseHandle(si.hStdInput);
		CloseHandle(si.hStdOutput);
		CloseHandle(si.hStdError);
	} catch (CreationException & e) {
		fprintf(stderr, "ERROR: %s\n", e.msg);
		fflush(stderr);
	}
}

int WinDbgProcess::read(int fd)
{
	// Which handle
	HANDLE h;
	switch (fd) {
		case 1:
			h = childStdoutRead;
			break;
		case 2:
			h = childStderrRead;
			break;
		default:
			// Ouch
			return -1;
	}
	
	char c;
	DWORD nread;
	if (!ReadFile(h, &c, 1, &nread, NULL) || nread == 0) {
		CloseHandle(h);
		return -1;
	} else {
		return c;
	}
}

void WinDbgProcess::write(int fd, int b)
{
	// stdin only please
	if (fd != 0)
		return;
	
	char c = b;
	DWORD nwritten;
	if (!WriteFile(childStdinWrite, &c, 1, &nwritten, NULL)) {
		CloseHandle(childStdinWrite);
		return;
	}
}

int WinDbgProcess::exitCode()
{
	DWORD exitCode;
	GetExitCodeProcess(pi.hProcess, &exitCode);
	return exitCode;	
}

int WinDbgProcess::waitFor()
{
	if (WaitForMultipleObjects(1, &pi.hProcess, FALSE, INFINITE) == WAIT_FAILED) {
		printf("Wait Failed\n");
		fflush(stdout);
	}
	
	return exitCode();
}

void WinDbgProcess::destroy()
{
	TerminateProcess(pi.hProcess, -1);
}

#define NATIVE(type, name) extern "C" JNIEXPORT type JNICALL Java_org_eclipse_cdt_debug_win32_cdi_WinDbgProcess_##name

static jfieldID pID;

NATIVE(void, initNative)(JNIEnv * env, jclass cls)
{
	pID = env->GetFieldID(cls, "p", "J");
}

static WinDbgProcess * getPointer(JNIEnv * env, jobject obj)
{
	return (WinDbgProcess *)env->GetLongField(obj, pID);
}

NATIVE(void, spawn)(JNIEnv * env, jobject obj, jstring cmdline, jstring dirname)
{
	jboolean cmdcopy;
	const jchar * cmd = env->GetStringChars(cmdline, &cmdcopy);
	jboolean dircopy;
	const jchar * dir = env->GetStringChars(dirname, &dircopy);
	WinDbgProcess * process = new WinDbgProcess(cmd, dir);
	if (cmdcopy == JNI_TRUE)
		env->ReleaseStringChars(cmdline, cmd);
	if (dircopy == JNI_TRUE)
		env->ReleaseStringChars(dirname, dir);
	env->SetLongField(obj, pID, (jlong)process);
}

NATIVE(jint, read)(JNIEnv * env, jobject obj, jint fd)
{
	return getPointer(env, obj)->read(fd);
}

NATIVE(void, write)(JNIEnv * env, jobject obj, jint fd, jint b)
{
	getPointer(env, obj)->write(fd, b);
}

NATIVE(jint, exitCode)(JNIEnv * env, jobject obj)
{
	return getPointer(env, obj)->exitCode();
}

NATIVE(jint, waitFor)(JNIEnv * env, jobject obj)
{
	return getPointer(env, obj)->waitFor();
}

NATIVE(void, destroy)(JNIEnv * env, jobject obj)
{
	getPointer(env, obj)->destroy();
}
