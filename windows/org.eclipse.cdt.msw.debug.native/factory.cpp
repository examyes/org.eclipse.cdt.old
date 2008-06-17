#include <jni.h>
#include <dbgeng.h>
#include "hresult.h"

#define NATIVE(ret, func) extern "C" JNIEXPORT ret JNICALL Java_org_eclipse_cdt_msw_debug_dbgeng_DebugObjectFactory_##func

NATIVE(jlong, nativeCreateClient)(JNIEnv * env, jclass cls) {
	void * client;
	HRESULT hr = DebugCreate(__uuidof(IDebugClient5), &client);
	if (FAILED(hr)) {
		throwHRESULT(env, hr);
		return NULL;
	}
	return (jlong)client;
}

NATIVE(jlong, nativeCreateControl)(JNIEnv * env, jclass cls) {
	void * control;
	HRESULT hr = DebugCreate(__uuidof(IDebugControl4), &control);
	if (FAILED(hr)) {
		throwHRESULT(env, hr);
		return NULL;
	}
	return (jlong)control;
}
