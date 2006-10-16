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

// Class IDebugClient

#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_IDebugClient_ ## name

static jfieldID pID;

static IDebugClient5 * getObject(JNIEnv * env, jobject obj) {
	jlong p = env->GetLongField(obj, pID);
	return (IDebugClient5 *)p;
}

extern "C" JNIEXPORT jint JNINAME(init)(JNIEnv * env, jobject obj) {
	// Initialize the field IDs
	jclass cls = env->GetObjectClass(obj);
	if (cls == NULL)
		return E_FAIL;
	
	pID = env->GetFieldID(cls, "p", "J");
	if (pID == 0)
		return E_FAIL;
	
	IDebugClient5 * debugClient;
	HRESULT hr = DebugCreate(__uuidof(IDebugClient5), (void **)&debugClient);
	if (FAILED(hr))
		return hr;

	env->SetLongField(obj, pID, (jlong)debugClient);
	
	return S_OK;
}

extern "C" JNIEXPORT jint JNINAME(getIdentity)(JNIEnv * env, jobject obj, jobjectArray identity) {
	IDebugClient5 * debugClient = getObject(env, obj);
	if (debugClient == NULL)
		return E_FAIL;
	
	ULONG size;
	HRESULT hr = debugClient->GetIdentityWide(NULL, 0, &size);
	if (FAILED(hr))
		return hr;
	
	if (size == 0)
		return E_FAIL;
	
	wchar_t * str = new wchar_t[size];
	hr = debugClient->GetIdentityWide(str, size, NULL);
	delete str;
	if (FAILED(hr))
		return hr;

	env->SetObjectArrayElement(identity, 0, env->NewString((jchar *)str, size - 1));
	
	return S_OK;
}

extern "C" JNIEXPORT jint JNINAME(createProcess2)(JNIEnv * env, jobject obj,
		jlong server, jstring commandLine, jobject options, jstring initialDirectory,
		jobject environment) {
	IDebugClient5 * debugClient = getObject(env, obj);
	if (debugClient == NULL)
		return E_FAIL;
	
	wchar_t * _commandLine = getString(env, commandLine);
	
	DEBUG_CREATE_PROCESS_OPTIONS _options;
	getDebugCreateProcessOptions(env, options, _options);

	wchar_t * _initialDirectory = getString(env, initialDirectory);

	HRESULT hr = debugClient->CreateProcess2Wide(server, _commandLine,
			&_options, sizeof(_options),
			_initialDirectory, NULL);
	
	delete _commandLine;
	delete _initialDirectory;
	
	return hr;
}

extern "C" JNIEXPORT jint JNINAME(setEventCallbacks)(JNIEnv * env, jobject obj,
		jobject callbacks) {
	IDebugClient5 * debugClient = getObject(env, obj);
	DebugEventCallbacks * _callbacks = DebugEventCallbacks::getObject(env, callbacks);
	return debugClient->SetEventCallbacksWide(_callbacks);
}
