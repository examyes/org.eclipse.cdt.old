/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 ******************************************************************************/

#include <jni.h>
#include <dbgeng.h>

// Bridge to IDebugClient

#define NATIVE(type, name) extern "C" JNIEXPORT type JNICALL Java_org_eclipse_cdt_debug_win32_core_dbgeng_IDebugClient_##name

// private static native long create();
NATIVE(jlong, create)(JNIEnv * env, jclass cls) {
	IDebugClient5 * debugClient = NULL;
	HRESULT hr = DebugCreate(__uuidof(IDebugClient5), (void **)&debugClient);
	if (hr != S_OK)
		fprintf(stderr, "DebugCreate failed %x\n", hr);

	return (jlong)debugClient;
}

// private static native void release(long p);
NATIVE(void, release)(JNIEnv * env, jclass cls, jlong p) {
	IDebugClient5 * debugClient = (IDebugClient5 *)p;
	debugClient->Release();
}

// private static native long createClient(long p);
NATIVE(jlong, createClient)(JNIEnv * env, jclass cls, jlong p) {
	IDebugClient5 * debugClient = (IDebugClient5 *)p;
	IDebugClient * newClient = NULL;
	HRESULT hr = debugClient->CreateClient(&newClient);
	if (hr != S_OK)
		fprintf(stderr, "CreateClient failed %x\n", hr);
	IDebugClient5 * newClient5 = NULL;
	newClient->QueryInterface(__uuidof(IDebugClient5), (void**)&newClient5);
	if (hr != S_OK)
		fprintf(stderr, "CreateClient(5) failed %x\n", hr);
	return (jlong)newClient5;
}

// private static native int createProcess(long p,
//										   long server,
//										   String commandLine,
//										   int createFlags);
NATIVE(jint, createProcess)(JNIEnv * env, jclass cls, jlong p, jlong server,
							jstring commandLine, jint createFlags) {
	IDebugClient5 * debugClient = (IDebugClient5 *)p;
	
	const wchar_t * commandLineWchar = (const wchar_t *)env->GetStringChars(commandLine, NULL);
	wchar_t * commandLineStr = wcsdup(commandLineWchar);
	env->ReleaseStringChars(commandLine, (const jchar *)commandLineWchar);

	return debugClient->CreateProcessWide(server, commandLineStr, createFlags);
}

// private static native int setOutputCallbacks(long p, long callbackp);
NATIVE(jint, setOutputCallbacks)(JNIEnv * env, jclass cls, jlong p, jlong callbackp) {
	IDebugClient5 * debugClient = (IDebugClient5 *)p;
	IDebugOutputCallbacks * callback = (IDebugOutputCallbacks *)callbackp;
	
	return debugClient->SetOutputCallbacks(callback);
}
