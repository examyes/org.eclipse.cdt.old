#include <jni.h>
#include <dbgeng.h>
#include "hresult.h"

#define NATIVE(ret, func) extern "C" JNIEXPORT ret JNICALL Java_org_eclipse_cdt_msw_debug_dbgeng_IDebugSystemObjects_##func

NATIVE(jint, nativeGetProcessIdByHandle)(JNIEnv * env, jclass cls, jlong object, jlong handle) {
	IDebugSystemObjects4 * systemObjects = (IDebugSystemObjects4 *)object;
	ULONG pid;
	HRESULT hr = systemObjects->GetProcessIdByHandle(handle, &pid);
	if (FAILED(hr))
		throwHRESULT(env, hr);
	return pid;
}

NATIVE(void, nativeSetCurrentProcessId)(JNIEnv * env, jclass cls, jlong object, jint id) {
	IDebugSystemObjects4 * systemObjects = (IDebugSystemObjects4 *)object;
	HRESULT hr = systemObjects->SetCurrentProcessId(id);
	if (FAILED(hr))
		throwHRESULT(env, hr);
}

NATIVE(jlong, nativeGetCurrentProcessHandle)(JNIEnv * env, jclass cls, jlong object) {
	IDebugSystemObjects4 * systemObjects = (IDebugSystemObjects4 *)object;
	ULONG64 handle;
	HRESULT hr = systemObjects->GetCurrentProcessHandle(&handle);
	if (FAILED(hr))
		throwHRESULT(env, hr);
	return handle;
}
