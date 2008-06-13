/*
 * hresult.cpp
 *
 *  Created on: Jun 6, 2008
 *      Author: DSchaefe
 */

#include <jni.h>
#include <windows.h>

extern void throwHRESULT(JNIEnv * env, HRESULT hr) {
	jclass excCls = env->FindClass("org/eclipse/cdt/msw/debug/dbgeng/HRESULTException");
	jmethodID excCons = env->GetMethodID(excCls, "<init>", "(I)V");
	jthrowable exc = (jthrowable)env->NewObject(excCls, excCons, hr);
	env->Throw(exc);
}
