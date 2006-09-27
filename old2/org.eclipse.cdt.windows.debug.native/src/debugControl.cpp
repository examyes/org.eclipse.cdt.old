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

// Class IDebugControl

#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_IDebugControl_## name

static jfieldID pID;

static IDebugControl4 * getObject(JNIEnv * env, jobject obj) {
	jlong p = env->GetLongField(obj, pID);
	return (IDebugControl4 *)p;
}

extern "C" JNIEXPORT jlong JNINAME(init)(JNIEnv * env, jobject obj) {
	jclass cls = env->GetObjectClass(obj);
	if (cls == NULL) {
		throwHRESULT(env, E_FAIL, __FILE__, __LINE__);
		return NULL;
	}
	
	pID = env->GetFieldID(cls, "p", "J");
	if (pID == 0) {
		throwHRESULT(env, E_FAIL, __FILE__, __LINE__);
		return NULL;
	}
	
	IDebugControl4 * debugControl;
	HRESULT hr = DebugCreate(__uuidof(IDebugControl4), (void **)&debugControl);
	if (hr != S_OK) {
		throwHRESULT(env, hr, __FILE__, __LINE__);
		return NULL;
	}

	return (jlong)debugControl;
}

extern "C" JNIEXPORT jint JNINAME(waitForEvent)(JNIEnv * env, jobject obj,
		jint flags, jint timeout) {
	IDebugControl4 * debugControl = getObject(env, obj);
	HRESULT hr = debugControl->WaitForEvent(flags, timeout);
	if (FAILED(hr)) {
		throwHRESULT(env, hr, __FILE__, __LINE__);
	}
	return hr;
}
