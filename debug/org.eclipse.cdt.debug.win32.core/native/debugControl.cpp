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

// Bridge to IDebugControl

#define NATIVE(type, name) extern "C" JNIEXPORT type JNICALL Java_org_eclipse_cdt_debug_win32_core_dbgeng_IDebugControl_##name

// private static native long create();
NATIVE(jlong, create)(JNIEnv * env, jclass cls) {
	IDebugControl * debugControl = NULL;
	HRESULT hr = DebugCreate(__uuidof(IDebugControl), (void **)&debugControl);
	if (hr != S_OK)
		fprintf(stderr, "DebugControl failed %x\n", hr);

	return (jlong)debugControl;
}

// private static native void release(long p);
NATIVE(void, release)(JNIEnv * env, jclass cls, jlong p) {
	IDebugControl * debugControl = (IDebugControl *)p;
	debugControl->Release();
}

// private static native void waitForEvent(long p, int timeout);
NATIVE(jint, waitForEvent)(JNIEnv * env, jclass cls, jlong p, jint timeout) {
	IDebugControl * debugControl = (IDebugControl *)p;
	return debugControl->WaitForEvent(0, timeout);
}
