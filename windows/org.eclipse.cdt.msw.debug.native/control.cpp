#include <jni.h>
#include <dbgeng.h>
#include "hresult.h"

#define NATIVE(ret, func) extern "C" JNIEXPORT ret JNICALL Java_org_eclipse_cdt_msw_debug_dbgeng_IDebugControl_##func

NATIVE(int, nativeWaitForEvent)(JNIEnv * env, jclass cls, jlong object, jint flags, jint timeout) {
	IDebugControl4 * control = (IDebugControl4 *)object;
	HRESULT hr = control->WaitForEvent(flags, timeout);
	if (FAILED(hr))
		throwHRESULT(env, hr);
	return hr;
}

NATIVE(void, nativeSetExecutionStatus)(JNIEnv * env, jclass cls, jlong object, jint status) {
	IDebugControl4 * control = (IDebugControl4 *)object;
	HRESULT hr = control->SetExecutionStatus(status);
	if (FAILED(hr))
		throwHRESULT(env, hr);
}

NATIVE(void, nativeSetInterrupt)(JNIEnv * env, jclass cls, jlong object, jint flags) {
	IDebugControl4 * control = (IDebugControl4 *)object;
	HRESULT hr = control->SetInterrupt(flags);
	if (FAILED(hr))
		throwHRESULT(env, hr);
}

NATIVE(jint, nativeGetInterrupt)(JNIEnv * env, jclass cls, jlong object) {
	IDebugControl4 * control = (IDebugControl4 *)object;
	HRESULT hr = control->GetInterrupt();
	if (FAILED(hr))
		throwHRESULT(env, hr);
	return hr;
}
