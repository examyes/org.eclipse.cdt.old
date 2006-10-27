/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/
#include <windows.h>
#include <dbgeng.h>
#include <jni.h>

#include "util.h"
#include "debugCreateProcessOptions.h"
#include "debugEventCallbacks.h"
#include "debugControl.h"

// Class IDebugClient

#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_IDebugClient_ ## name
#define JNISTDMETHOD(name, ...) extern "C" JNIEXPORT jint JNINAME(name)(JNIEnv * env, jobject obj, __VA_ARGS__ ) { \
	try { IDebugClient5 * client = getObject(env, obj);
#define JNISTDEND } catch (jobject e) { env->Throw((jthrowable)e); return E_FAIL; } }


static jfieldID pID = NULL;

static jfieldID getPID(JNIEnv * env, jobject obj) {
	if (pID == NULL) {
		jclass cls = env->GetObjectClass(obj);
		pID = env->GetFieldID(cls, "p", "J");
		checkNull(env, pID);
	}
	return pID;
}

static IDebugClient5 * getObject(JNIEnv * env, jobject obj) {
	IDebugClient5 * client = (IDebugClient5 *)env->GetLongField(obj, getPID(env, obj));
	checkNull(env, client);
	return client;
}

void setObject(JNIEnv * env, jobject obj, IDebugClient5 * client) {
	env->SetLongField(obj, getPID(env, obj), (jlong)client);
}

//	public static native IDebugClient create();
extern "C" JNIEXPORT jobject JNINAME(create)(JNIEnv * env, jclass cls) {
	IDebugClient5 * client;
	HRESULT hr = DebugCreate(__uuidof(IDebugClient5), (void **)&client);
	if (FAILED(hr))
		return NULL;
	
	jmethodID cons = env->GetMethodID(cls, "<init>", "()V");
	jobject obj = env->NewObject(cls, cons);
	setObject(env, obj, client);
	return obj;
}

//	public native int attachKernel(int flags, String connectOptions);
JNISTDMETHOD(attachKernel, jint flags, jstring connectOptions)
	wchar_t * _connectOptions = getString(env, connectOptions);
	HRESULT hr = client->AttachKernelWide(flags, _connectOptions);
	delete[] _connectOptions;
	return hr;
JNISTDEND

//	public native int getKernelConnectionOptions(IDebugString options);
JNISTDMETHOD(getKernelConnectionOptions, jobject options)
	ULONG size;
	HRESULT hr = client->GetKernelConnectionOptionsWide(NULL, 0, &size);
	if (FAILED(hr))
		return hr;
	
	wchar_t * str = new wchar_t[size];
	hr = client->GetKernelConnectionOptionsWide(str, size, NULL);
	if (!FAILED(hr))
		setString(env, options, str);

	delete[] str;
	return hr;
JNISTDEND

//	public native int setKernelConnectionOptions(String options);
JNISTDMETHOD(setKernelConnectionOptions, jstring options)
	wchar_t * _options = getString(env, options);
	HRESULT hr = client->SetKernelConnectionOptionsWide(_options);
	delete[] _options;
	return hr;
JNISTDEND

//	public native int startProcessServer(int flags, String options);
JNISTDMETHOD(startProcessServer, jint flags, jstring options)
	wchar_t * _options = getString(env, options);
	HRESULT hr = client->StartProcessServerWide(flags, _options, NULL);
	delete[] _options;
	return hr;
JNISTDEND

//	public native int connectProcessServer(String remoteOptions, IDebugLong server);
JNISTDMETHOD(connectProcessServer, jstring remoteOptions, jobject server)
	wchar_t * _remoteOptions = getString(env, remoteOptions);
	ULONG64 _server;
	HRESULT hr = client->ConnectProcessServerWide(_remoteOptions, &_server);
	if (!FAILED(hr))
		setLong(env, server, _server);
	delete[] _remoteOptions;
	return hr;
JNISTDEND

//	public native int disconnectProcessServer(long server);
JNISTDMETHOD(disconnectProcessServer, jlong server)
	return client->DisconnectProcessServer(server);
JNISTDEND

//	public native int getRunningProcessSystemIds(long server, IDebugIntArray ids);
JNISTDMETHOD(getRunningProcessSystemIds, jlong server, jobject ids)
	ULONG count;
	HRESULT hr = client->GetRunningProcessSystemIds(server, NULL, 0, &count);
	if (FAILED(hr))
		return hr;
	ULONG * _ids = new ULONG[count];
	hr = client->GetRunningProcessSystemIds(server, _ids, count, NULL);
	if (!FAILED(hr)) {
		// The cast to jint just removes the signedness
		setIntArray(env, ids, (jint *)_ids, count);
	}
	delete[] _ids;
	return hr;
JNISTDEND

//	public native int getRunningProcessSystemIdByExecutableName(
//			long server, String exeName, int flags, IDebugInt id);
JNISTDMETHOD(getRunningProcessSystemIdByExecutableName,
		jlong server, jstring exeName, jint flags, jobject id)
	wchar_t * _exeName = getString(env, exeName);
	ULONG _id;
	HRESULT hr = client->GetRunningProcessSystemIdByExecutableNameWide(
			server, _exeName, flags, &_id);
	delete[] _exeName;
	if (FAILED(hr))
		return hr;
	setInt(env, id, _id);
	return hr;
JNISTDEND

//	public native int getRunningProcessDescription(
//			long server, int systemId, int flags,
//			IDebugString exeName, IDebugString description);
JNISTDMETHOD(getRunningProcessDescription, jlong server, jint systemId,
		jint flags, jobject exeName, jobject description)
	ULONG exeNameSize;
	ULONG descriptionSize;
	HRESULT hr = client->GetRunningProcessDescription(
			server, systemId, flags, NULL, 0, &exeNameSize,
			NULL, 0, &descriptionSize);
	if (FAILED(hr))
		return hr;
	wchar_t * _exeName = new wchar_t[exeNameSize];
	wchar_t * _description = new wchar_t[descriptionSize];
	hr = client->GetRunningProcessDescriptionWide(server, systemId, flags,
			_exeName, exeNameSize, NULL,
			_description, descriptionSize, NULL);
	delete[] _exeName;
	delete[] _description;
	return hr;
JNISTDEND

//	public native int attachProcess(long server, int processId, int attachFlags);
JNISTDMETHOD(attachProcess, jlong server, jint processId, jint attachFlags)
	return client->AttachProcess(server, processId, attachFlags);
JNISTDEND

//	public native int createProcess(long server, String commandLine, int createFlags);
JNISTDMETHOD(createProcess, jlong server, jstring commandLine, jint createFlags)
	wchar_t * _commandLine = getString(env, commandLine);
	HRESULT hr = client->CreateProcessWide(server, _commandLine, createFlags);
	delete[] _commandLine;
	return hr;
JNISTDEND

//	public native int createProcessAndAttach(long server, String commandLine,
//			int createFlags, int processId, int attachFlags);
JNISTDMETHOD(createProcessAndAttach, jlong server, jstring commandLine,
		jint createFlags, jint processId, jint attachFlags)
	wchar_t * _commandLine = getString(env, commandLine);
	HRESULT hr = client->CreateProcessAndAttachWide(server, _commandLine,
			createFlags, processId, attachFlags);
	delete[] _commandLine;
	return hr;
JNISTDEND

//	public native int getProcessOptions(IDebugInt options);
JNISTDMETHOD(getProcessOptions, jobject options)
	ULONG _options;
	HRESULT hr = client->GetProcessOptions(&_options);
	if (FAILED(hr))
		return hr;
	setInt(env, options, _options);
JNISTDEND

//	public native int addProcessOptions(int options);
JNISTDMETHOD(addProcessOptions, jint options)
	return client->AddProcessOptions(options);
JNISTDEND

//	public native int removeProcessOptions(int options);
JNISTDMETHOD(removeProcessOptions, jint options)
	return client->RemoveProcessOptions(options);
JNISTDEND

//	public native int setProcessOptions(int options);
JNISTDMETHOD(setProcessOptions, jint options)
	return client->SetProcessOptions(options);
JNISTDEND

//	public native int openDumpFile(String dumpFile);
JNISTDMETHOD(openDumpFile, jstring dumpFile)
	wchar_t * _dumpFile = getString(env, dumpFile);
	HRESULT hr = client->OpenDumpFileWide(_dumpFile, 0);
	delete[] _dumpFile;
	return hr;
JNISTDEND

//	public native int connectSession(int flags, int historyLimit);
JNISTDMETHOD(connectSession, jint flags, jint historyLimit)
	return client->ConnectSession(flags, historyLimit);
JNISTDEND

//	public native int startServer(String options);
JNISTDMETHOD(startServer, jstring options)
	wchar_t * _options = getString(env, options);
	HRESULT hr = client->StartServerWide(_options);
	delete[] _options;
	return hr;
JNISTDEND

//	public native int outputServers(int outputControl, String machine, int flags);
JNISTDMETHOD(outputServers, jint outputControl, jstring machine, jint flags)
	wchar_t * _machine = getString(env, machine);
	HRESULT hr = client->OutputServersWide(outputControl, _machine, flags);
	delete[] _machine;
	return hr;
JNISTDEND

//	public native int terminateProcesses();
JNISTDMETHOD(terminateProcesses)
	return client->TerminateProcesses();
JNISTDEND

//	public native int detachProcesses();
JNISTDMETHOD(detachProcesses)
	return client->DetachProcesses();
JNISTDEND

//	public native int endSession(int flags);
JNISTDMETHOD(endSession, jint flags)
	return client->EndSession(flags);
JNISTDEND

//	public native int getExitCode(IDebugInt code);
JNISTDMETHOD(getExitCode, jobject code)
	ULONG _code;
	HRESULT hr = client->GetExitCode(&_code);
	if (FAILED(hr))
		return hr;
	setInt(env, code, _code);
	return hr;
JNISTDEND

//	public native int dispatchCallbacks(int timeout);
JNISTDMETHOD(dispatchCallbacks, jint timeout)
	return client->DispatchCallbacks(timeout);
JNISTDEND

//	public native int exitDispatch(IDebugClient client);
JNISTDMETHOD(exitDispatch, jobject otherClient)
	IDebugClient5 * _otherClient5 = getObject(env, otherClient);
	IDebugClient * _otherClient;
	HRESULT hr = _otherClient5->QueryInterface(__uuidof(IDebugClient), (void **)&_otherClient);
	return client->ExitDispatch((IDebugClient *)_otherClient);
JNISTDEND

//	public native int createClient(IDebugClient client);
JNISTDMETHOD(createClient, jobject newclient)
	IDebugClient * _newclient;
	HRESULT hr = client->CreateClient(&_newclient);
	if (FAILED(hr))
		return hr;
	IDebugClient5 * _newclient5;
	hr = _newclient->QueryInterface(__uuidof(IDebugClient5), ((void **)&_newclient5));
	if (FAILED(hr))
		return hr;
	setObject(env, newclient, _newclient5);
	return hr;
JNISTDEND

//	public native int getInputCallbacks(IDebugInputCallbacks callbacks);
//	public native int setInputCallbacks(IDebugInputCallbacks callbacks);
//	public native int getOutputCallbacks(IDebugOutputCallbacks callbacks);
//	public native int setOutputCallbacks(IDebugOutputCallbacks callbacks);
//	public native int getOutputMask(IDebugInt mask);
//	public native int setOutputMask(int mask);
//	public native int getOtherOutputMask(IDebugClient client, IDebugInt mask);
//	public native int setOtherOutputMask(IDebugClient client, int mask);
//	public native int getOutputWidth(IDebugInt columns);
//	public native int setOutputWidth(int columns);
//	public native int getOutputLinePrefix(IDebugString prefix);
//	public native int setOutputLinePrefix(String prefix);

//	public native int getIdentity(IDebugString identity);
JNISTDMETHOD(getIdentity, jobject identity)
	ULONG size;
	HRESULT hr = client->GetIdentityWide(NULL, 0, &size);
	if (FAILED(hr))
		return hr;
	wchar_t * str = new wchar_t[size];
	hr = client->GetIdentityWide(str, size, NULL);
	if (!FAILED(hr))
		setString(env, identity, str);
	delete[] str;
	return hr;
JNISTDEND

//	public native int outputIdentity(int outputControl, int flags, String format);
JNISTDMETHOD(outputIdentity, jint outputControl, jint flags, jstring format)
	wchar_t * _format = getString(env, format);
	HRESULT hr = client->OutputIdentityWide(outputControl, flags, _format);
	delete[] _format;
	return hr;
JNISTDEND

//	public native int getEventCallbacks(IDebugEventCallbacks callbacks);
JNISTDMETHOD(getEventCallbacks, jobject callbacks)
	IDebugEventCallbacksWide * _icallbacks;
	HRESULT hr = client->GetEventCallbacksWide(&_icallbacks);
	if (FAILED(hr))
		return hr;
	// TODO this isn't very clean, we should be doing a QI for this
	DebugEventCallbacks * _callbacks = (DebugEventCallbacks *)_icallbacks;
	DebugEventCallbacks::setObject(env, callbacks, _callbacks);
	return hr;
JNISTDEND

//	public native int setEventCallbacks(IDebugEventCallbacks callbacks);
JNISTDMETHOD(setEventCallbacks, jobject callbacks)
	DebugEventCallbacks * _callbacks = DebugEventCallbacks::getObject(env, callbacks);
	return client->SetEventCallbacksWide(_callbacks);
JNISTDEND

//	public native int flushCallbacks();
JNISTDMETHOD(flushCallbacks)
	return client->FlushCallbacks();
JNISTDEND

//	public native int writeDumpFile2(String dumpFile, int qualifier, int formatFlags,
//			String comment);
JNISTDMETHOD(writeDumpFile2, jstring dumpFile, jint qualifier, jint formatFlags,
		jstring comment)
	wchar_t * _dumpFile = getString(env, dumpFile);
	wchar_t * _comment = getString(env, comment);
	HRESULT hr = client->WriteDumpFileWide(_dumpFile, 0, qualifier, formatFlags, _comment);
	delete[] _dumpFile;
	delete[] _comment;
	return hr;
JNISTDEND

//	public native int addDumpInformationFile(String infoFile, int type);
JNISTDMETHOD(addDumpInformationFile, jstring infoFile, jint type)
	wchar_t * _infoFile = getString(env, infoFile);
	HRESULT hr = client->AddDumpInformationFileWide(_infoFile, 0, type);
	delete[] _infoFile;
	return hr;
JNISTDEND

//	public native int endProcessServer(long server);
JNISTDMETHOD(endProcessServer, jlong server)
	return client->EndProcessServer(server);
JNISTDEND

//	public native int waitForProcessServerEnd(int timeout);
JNISTDMETHOD(waitForProcessServerEnd, jint timeout)
	return client->WaitForProcessServerEnd(timeout);
JNISTDEND

//	public native int isKernelDebuggerEnabled();
JNISTDMETHOD(isKernelDebuggerEnabled)
	return client->IsKernelDebuggerEnabled();
JNISTDEND

//	public native int terminateCurrentProcess();
JNISTDMETHOD(terminateCurrentProcess)
	return client->TerminateCurrentProcess();
JNISTDEND

//	public native int detachCurrentProcess();
JNISTDMETHOD(detachCurrentProcess)
	return client->DetachCurrentProcess();
JNISTDEND

//	public native int abandonCurrentProcess();
JNISTDMETHOD(abandonCurrentProcess)
	return client->AbandonCurrentProcess();
JNISTDEND

//	public native int getNumberDumpFiles(IDebugInt number);
JNISTDMETHOD(getNumberDumpFiles, jobject number)
	ULONG _number;
	HRESULT hr = client->GetNumberDumpFiles(&_number);
	if (FAILED(hr))
		return hr;
	setInt(env, number, _number);
	return hr;
JNISTDEND

//	public native int getDumpFile(int index, IDebugString dumpFile,
//			IDebugInt nameSize, IDebugLong handle, int type);

//	public native int createProcess2(long server, String commandLine,
//			DebugCreateProcessOptions options, String initialDirectory,
//			String environment);
JNISTDMETHOD(createProcess2, jlong server, jstring commandLine,
		jobject options, jstring initialDirectory,
		jstring environment)
	wchar_t * _commandLine = getString(env, commandLine);
	DEBUG_CREATE_PROCESS_OPTIONS _options;
	getDebugCreateProcessOptions(env, options, _options);
	wchar_t * _initialDirectory = initialDirectory != NULL ? getString(env, initialDirectory) : NULL;
	wchar_t * _environment = environment != NULL ? getString(env, environment) : NULL;
	HRESULT hr = client->CreateProcess2Wide(server, _commandLine,
			&_options, sizeof(_options), _initialDirectory, _environment);
	delete[] _commandLine;
	delete[] _initialDirectory;
	delete[] _environment;
	return hr;
JNISTDEND

//	public native int createProcessAndAttach2(long server, String commandLine,
//			DebugCreateProcessOptions options, String initialDirectory,
//			String enviornment, int processId, int attachFlags);
//	public native int pushOutputLinePrefix(String newPrefix, IDebugLong handle);
//	public native int popOutputLinePrefix(IDebugLong handle);
//	public native int getNumberInputCallbacks(IDebugInt count);
//	public native int getNumberOutputCallbacks(IDebugInt count);
//	public native int getNumberEventCallbacks(int eventFlags, IDebugInt count);
//	public native int getQuitLockString(IDebugString string);
//	public native int setQuitLockString(String string);

//public native int createControl(IDebugControl control);
JNISTDMETHOD(createControl, jobject control)
	IDebugControl4 * _control;
	HRESULT hr = client->QueryInterface(__uuidof(IDebugControl4), ((void **)&_control));
	if (FAILED(hr))
		return hr;
	setObject(env, control, _control);
	return hr;
JNISTDEND
