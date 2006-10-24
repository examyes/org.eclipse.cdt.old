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
//	public native int getRunningProcessDescription(
//			long server, int systemId, int flags,
//			IDebugString exeName, IDebugString description);
//	public native int attachProcess(long server, int processId, int attachFlags);
//	public native int createProcess(long server, String commandLine, int createFlags);
//	public native int createProcessAndAttach(long server, String commandLine,
//			int createFlags, int processId, int attachFlags);
//	public native int getProcessOptions(IDebugInt options);
//	public native int addProcessOptions(int options);
//	public native int removeProcessOptions(int options);
//	public native int setProcessOptions(int options);
//	public native int openDumpFile(String dumpFile);
//	public native int writeDumpFile(String dumpFile, int qualifier);
//	public native int connectSession(int flags, int historyLimit);
//	public native int startServer(String options);
//	public native int outputServers(int outputControl, String machine, int flags);
//	public native int terminateProcesses();
//	public native int detachProcesses();
//	public native int endSession(int flags);
//	public native int getExitCode(IDebugInt code);

//	public native int dispatchCallbacks(int timeout);
extern "C" JNIEXPORT jint JNINAME(dispatchCallbacks)(JNIEnv * env, jobject obj, jint timeout) {
	IDebugClient5 * debugClient = getObject(env, obj);
	return debugClient->DispatchCallbacks(timeout);
}

//	public native int exitDispatch(IDebugClient client);
extern "C" JNIEXPORT jint JNINAME(exitDispatch)(JNIEnv * env, jobject obj, jobject client) {
	IDebugClient5 * debugClient = getObject(env, obj);
	IDebugClient5 * otherClient5 = getObject(env, client);
	IDebugClient * otherClient;
	HRESULT hr = otherClient5->QueryInterface(__uuidof(IDebugClient), (void **)&otherClient);
	return debugClient->ExitDispatch((IDebugClient *)otherClient);
}

//	public native int createClient(IDebugClient client);
extern "C" JNIEXPORT jint JNINAME(createClient)(JNIEnv * env, jobject obj, jobject client) {
	IDebugClient5 * debugClient = getObject(env, obj);
	
	IDebugClient * newclient;
	HRESULT hr = debugClient->CreateClient(&newclient);
	if (FAILED(hr))
		return hr;
	
	IDebugClient5 * newclient5;
	hr = newclient->QueryInterface(__uuidof(IDebugClient5), ((void **)&newclient5));
	if (FAILED(hr))
		return hr;
	
	setObject(env, client, newclient5);
	
	return hr;
}

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
extern "C" JNIEXPORT jint JNINAME(getIdentity)(JNIEnv * env, jobject obj, jobject identity) {
	IDebugClient5 * debugClient = getObject(env, obj);
	
	ULONG size;
	HRESULT hr = debugClient->GetIdentityWide(NULL, 0, &size);
	if (FAILED(hr))
		return hr;
	
	wchar_t * str = new wchar_t[size];
	hr = debugClient->GetIdentityWide(str, size, NULL);
	if (FAILED(hr)) {
		delete str;
		return hr;
	}
	
	setString(env, identity, str);
	delete[] str;

	return S_OK;
}

//	public native int outputIdentity(int outputControl, int flags, String format);
//	public native int getEventCallbacks(IDebugEventCallbacks callbacks);

//	public native int setEventCallbacks(IDebugEventCallbacks callbacks);
extern "C" JNIEXPORT jint JNINAME(setEventCallbacks)(JNIEnv * env, jobject obj,
		jobject callbacks) {
	IDebugClient5 * debugClient = getObject(env, obj);
	
	DebugEventCallbacks * _callbacks = DebugEventCallbacks::getObject(env, callbacks);
	return debugClient->SetEventCallbacksWide(_callbacks);
}
	
//	public native int flushCallbacks();
//	public native int writeDumpFile2(String dumpFile, int qualifier, int formatFlags,
//			String comment);
//	public native int addDumpInformationFile(String infoFile, int type);
//	public native int endProcessServer(long server);
//	public native int waitForProcessServerEnd(int timeout);
//	public native int isKernelDebuggerEnabled();
//	public native int terminateCurrentProcess();
//	public native int detachCurrentProcess();
//	public native int abandonCurrentProcess();
//	public native int getNumberDumpFiles(IDebugInt number);
//	public native int getDumpFile(int index, IDebugString dumpFile,
//			IDebugInt nameSize, IDebugLong handle, int type);

//	public native int createProcess2(long server, String commandLine,
//			DebugCreateProcessOptions options, String initialDirectory,
//			String environment);
extern "C" JNIEXPORT jint JNINAME(createProcess2)(JNIEnv * env, jobject obj,
		jlong server, jstring commandLine, jobject options, jstring initialDirectory,
		jstring environment) {
	IDebugClient5 * debugClient = getObject(env, obj);
	
	wchar_t * _commandLine = getString(env, commandLine);
	
	DEBUG_CREATE_PROCESS_OPTIONS _options;
	getDebugCreateProcessOptions(env, options, _options);

	wchar_t * _initialDirectory = initialDirectory != NULL ? getString(env, initialDirectory) : NULL;
	
	wchar_t * _environment = environment != NULL ? getString(env, environment) : NULL;

	HRESULT hr = debugClient->CreateProcess2Wide(server, _commandLine,
			&_options, sizeof(_options), _initialDirectory, _environment);
	
	delete[] _commandLine;
	delete[] _initialDirectory;
	delete[] _environment;
	
	return hr;
}

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
extern "C" JNIEXPORT jint JNINAME(createControl)(JNIEnv * env, jobject obj, jobject control) {
	try {
		IDebugClient5 * debugClient = getObject(env, obj);
		
		IDebugControl4 * debugControl;
		HRESULT hr = debugClient->QueryInterface(__uuidof(IDebugControl4), ((void **)&debugControl));
		if (FAILED(hr))
			return hr;
		
		setObject(env, control, debugControl);
		
		return hr;
	} catch (jobject e) {
		env->Throw((jthrowable)e);
		return E_FAIL;
	}
}
