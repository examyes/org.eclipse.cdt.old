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

#include "debugControl.h"
#include "util.h"
#include "debugBreakpoint.h"

// Class IDebugControl

#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_IDebugControl_## name

static jfieldID pID = NULL;

static jfieldID getPID(JNIEnv * env, jobject obj) {
	if (pID == NULL) {
		jclass cls = env->GetObjectClass(obj);
		pID = env->GetFieldID(cls, "p", "J");
		checkNull(env, pID);
	}
	return pID;
}

static IDebugControl4 * getObject(JNIEnv * env, jobject obj) {
	IDebugControl4 * control = (IDebugControl4 *)env->GetLongField(obj, getPID(env, obj));
	checkNull(env, control);
	return control;
}

void setObject(JNIEnv * env, jobject obj, IDebugControl4 * debugControl) {
	env->SetLongField(obj, getPID(env, obj), (jlong)debugControl);
}

extern "C" JNIEXPORT jint JNINAME(waitForEvent)(JNIEnv * env, jobject obj,
		jint flags, jint timeout) {
	IDebugControl4 * debugControl = getObject(env, obj);
	return debugControl->WaitForEvent(flags, timeout);
}

extern "C" JNIEXPORT jint JNINAME(addBreakpoint)(JNIEnv * env, jobject obj,
		jint type, jint desiredId, jobject bp) {
	IDebugControl4 * debugControl = getObject(env, obj);
	IDebugBreakpoint2 * bpcom;
	HRESULT hr = debugControl->AddBreakpoint2(type, desiredId, &bpcom);
	if (FAILED(hr))
		return hr;

	setObject(env, bp, bpcom);
	return hr;
}
