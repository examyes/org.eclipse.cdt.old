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

#include <DbgEng.h>
#include <jni.h>

// Bridge to IDebugClient

#define NATIVE(type, name) extern "C" JNIEXPORT type JNICALL Java_org_eclipse_cdt_debug_win32_core_dbgeng_IDebugClient_##name

// private static native long create();
NATIVE(jlong, create)(JNIEnv * env, jclass cls) {
	IDebugClient4 * debugClient = NULL;
	HRESULT hr = DebugCreate(__uuidof(IDebugClient4), (void **)&debugClient);
	return (jlong)debugClient;
}

// private static native int createProcess(long p,
//										   long server,
//										   String commandLine,
//										   int createFlags);
NATIVE(jint, createProcess)(JNIEnv * env, jclass cls, jlong p, jlong server,
							jstring commandLine, jint createFlags) {
	IDebugClient4 * debugClient = (IDebugClient4 *)p;
	
	const jchar * commandLineJchar = env->GetStringChars(commandLine, NULL);
	wchar_t * commandLineStr = _wcsdup(commandLineJchar);
	env->ReleaseStringChars(commandLine, commandLineStr);
	
	return debugClient->CreateProcessWide(server, commandLineStr, createFlags);
}

// private static native int setOutputCallbacks(long p, long callbackp);
NATIVE(jint, setOutputCallbacks)(JNIEnv * env, jclass cls, jlong p, jlong callbackp) {
	IDebugClient4 * debugClient = (IDebugClient4 *)p;
	IDebugOutputCallbacks * callback = (IDebugOutputCallbacks *)callbackp;
	
	return debugClient->SetOutputCallbacks(callback);
}
