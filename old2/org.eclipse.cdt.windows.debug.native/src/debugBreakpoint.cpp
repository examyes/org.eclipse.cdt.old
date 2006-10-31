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
#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_IDebugBreakpoint_## name

static jfieldID pID = NULL;

static jfieldID getPID(JNIEnv * env, jobject obj) {
	if (pID == NULL) {
		jclass cls = env->GetObjectClass(obj);
		pID = env->GetFieldID(cls, "p", "J");
		checkNull(env, pID);
	}
	return pID;
}

static IDebugBreakpoint2 * getObject(JNIEnv * env, jobject obj) {
	IDebugBreakpoint2 * bp = (IDebugBreakpoint2 *)env->GetLongField(obj, getPID(env, obj));
	checkNull(env, bp);
	return bp;
}

void setObject(JNIEnv * env, jobject obj, IDebugBreakpoint2 * bp) {
	env->SetLongField(obj, getPID(env, obj), (jlong)bp);
}

jobject createBreakpoint(JNIEnv * env, IDebugBreakpoint2 * bp) {
	jclass cls = env->FindClass("org/eclipse/cdt/windows/debug/core/IDebugBreakpoint");
	checkNull(env, cls);
	
	jmethodID constructor = env->GetMethodID(cls, "<init>", "()V");
	checkNull(env, constructor);
	
	jobject bpobj = env->NewObject(cls, constructor, (jlong)bp);
	checkNull(env, bpobj);
	
	setObject(env, bpobj, bp);
	return bpobj;
}

extern "C" JNIEXPORT jint JNINAME(getId)(JNIEnv * env, jobject obj,
		jintArray id) {
	IDebugBreakpoint2 * bp = getObject(env, obj);
	ULONG Id;
	HRESULT hr = bp->GetId(&Id);
	if (FAILED(hr))
		return hr;
	jint jid = Id;
	env->SetIntArrayRegion(id, 0, 1, &jid);
	return S_OK;
}

extern "C" JNIEXPORT jint JNINAME(setOffsetExpression)(JNIEnv * env, jobject obj,
		jstring expression) {
	IDebugBreakpoint2 * bp = getObject(env, obj);
	wchar_t * exprstr = getString(env, expression);
	HRESULT hr = bp->SetOffsetExpressionWide(exprstr);
	delete[] exprstr;
	return hr;
}

extern "C" JNIEXPORT jint JNINAME(getOffsetExpression)(JNIEnv * env, jobject obj,
		jobjectArray expression) {
	IDebugBreakpoint2 * bp = getObject(env, obj);
	
	ULONG size;
	HRESULT hr = bp->GetOffsetExpression(NULL, 0, &size);
	if (FAILED(hr))
		return hr;

	wchar_t * buff = new wchar_t[size];
	hr = bp->GetOffsetExpressionWide(buff, size, NULL);
	if (FAILED(hr)) {
		delete buff;
		return hr;
	}
	jstring offstr = env->NewString((jchar *)buff, size - 1);
	env->SetObjectArrayElement(expression, 0, offstr);
	delete buff;
	
	return S_OK;
}

extern "C" JNIEXPORT jint JNINAME(addFlags)(JNIEnv * env, jobject obj, jint flags) {
	IDebugBreakpoint2 * bp = getObject(env, obj);
	return bp->AddFlags(flags);
}
