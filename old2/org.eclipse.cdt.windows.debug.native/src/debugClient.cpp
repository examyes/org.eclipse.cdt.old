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
#include "HRESULTFailure.h"
#include "debugCreateProcessOptions.h"

// Class IDebugClient

#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_IDebugClient_ ## name

static IDebugClient5 * getObject(JNIEnv * env, jobject obj) {
	jclass cls = env->GetObjectClass(obj);
	if (cls == NULL) {
		throwHRESULT(env, E_FAIL);
		return NULL;
	}
	
	jfieldID fieldID = env->GetFieldID(cls, "p", "J");
	if (fieldID == 0) {
		throwHRESULT(env, E_FAIL);
		return NULL;
	}
	
	jlong p = env->GetLongField(obj, fieldID);
	return (IDebugClient5 *)p;
}

extern "C" JNIEXPORT jobject JNINAME(debugCreate)(JNIEnv * env, jclass cls) {
	IDebugClient5 * debugClient;
	HRESULT hr = DebugCreate(__uuidof(IDebugClient5), (void **)&debugClient);
	if (hr != S_OK) {
		throwHRESULT(env, hr);
		return NULL;
	}

	jmethodID constructor = env->GetMethodID(cls, "<init>", "(J)V");
	if (constructor == 0) {
		throwHRESULT(env, E_FAIL);
		return NULL;
	}
	
	return env->NewObject(cls, constructor, (jlong)debugClient);
}

extern "C" JNIEXPORT jstring JNINAME(getIdentity)(JNIEnv * env, jobject obj) {
	IDebugClient5 * debugClient = getObject(env, obj);
	if (debugClient == NULL) {
		throwHRESULT(env, E_FAIL);
		return NULL;
	}
	
	ULONG size;
	HRESULT hr = debugClient->GetIdentityWide(NULL, 0, &size);
	if (FAILED(hr)) {
		throwHRESULT(env, hr);
		return NULL;
	}
	
	if (size == 0)
		return NULL;
	
	wchar_t * str = new wchar_t[size];
	hr = debugClient->GetIdentityWide(str, size, NULL);
	delete str;
	if (FAILED(hr)) {
		throwHRESULT(env, hr);
		return NULL;
	}

	return env->NewString((jchar *)str, size - 1);
}

extern "C" JNIEXPORT void JNINAME(createProcess2)(JNIEnv * env, jobject obj,
		jlong server, jstring commandLine, jobject options, jstring initialDirectory,
		jobject environment) {
	IDebugClient5 * debugClient = getObject(env, obj);
	if (debugClient == NULL) {
		throwHRESULT(env, E_FAIL);
		return;
	}
	
	wchar_t * _commandLine = getString(env, commandLine);
	
	DEBUG_CREATE_PROCESS_OPTIONS _options;
	getDebugCreateProcessOptions(env, options, _options);

	wchar_t * _initialDirectory = getString(env, initialDirectory);

	HRESULT hr = debugClient->CreateProcess2Wide(server, _commandLine,
			&_options, sizeof(_options),
			_initialDirectory, NULL);
	
	delete _commandLine;
	delete _initialDirectory;
	
	if (FAILED(hr))
		throwHRESULT(env, hr);
}
