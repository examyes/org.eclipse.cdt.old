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
#include "debugBreakpoint.h"

// Class IDebugControl

#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_IDebugControl_## name

static jfieldID pID;

static IDebugControl4 * getObject(JNIEnv * env, jobject obj) {
	jlong p = env->GetLongField(obj, pID);
	return (IDebugControl4 *)p;
}

extern "C" JNIEXPORT jint JNINAME(init)(JNIEnv * env, jobject obj) {
	jclass cls = env->GetObjectClass(obj);
	if (cls == NULL)
		return E_FAIL;
	
	pID = env->GetFieldID(cls, "p", "J");
	if (pID == 0)
		return E_FAIL;
	
	IDebugControl4 * debugControl;
	HRESULT hr = DebugCreate(__uuidof(IDebugControl4), (void **)&debugControl);
	if (FAILED(hr))
		return hr;

	env->SetLongField(obj, pID, (jlong)debugControl);
	
	return S_OK;
}

extern "C" JNIEXPORT jint JNINAME(waitForEvent)(JNIEnv * env, jobject obj,
		jint flags, jint timeout) {
	IDebugControl4 * debugControl = getObject(env, obj);
	return debugControl->WaitForEvent(flags, timeout);
}

extern "C" JNIEXPORT jint JNINAME(addBreakpoint)(JNIEnv * env, jobject obj,
		jint type, jint desiredId, jobjectArray bp) {
	IDebugControl4 * debugControl = getObject(env, obj);
	IDebugBreakpoint2 * bpcom;
	HRESULT hr = debugControl->AddBreakpoint2(type, desiredId, &bpcom);
	if (FAILED(hr))
		return hr;
	
	jobject bpobj = createBreakpoint(env, bpcom);
	env->SetObjectArrayElement(bp, 0, bpobj);
	
	return S_OK;
}
