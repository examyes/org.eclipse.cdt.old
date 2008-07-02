#include <jni.h>
#include <dbgeng.h>
#include "native.h"

#define NATIVE(ret, func) extern "C" JNIEXPORT ret JNICALL Java_org_eclipse_cdt_msw_debug_dbgeng_IDebugBreakpoint_##func

NATIVE(jint, nativeGetId)(JNIEnv * env, jclass cls, jlong p) {
	IDebugBreakpoint2 * bp = (IDebugBreakpoint2 *)p;
	ULONG id;
	HRESULT hr = bp->GetId(&id);
	if (FAILED(hr))
		throwHRESULT(env, hr);
	return id;
}

NATIVE(void, nativeSetOffsetExpression)(JNIEnv * env, jclass cls, jlong p, jstring expression) {
	IDebugBreakpoint2 * bp = (IDebugBreakpoint2 *)p;
	const jchar * exprStr = env->GetStringChars(expression, NULL);
	HRESULT hr = bp->SetOffsetExpressionWide((const wchar_t *)exprStr);
	if (FAILED(hr))
		throwHRESULT(env, hr);
	env->ReleaseStringChars(expression, exprStr);
}

NATIVE(void, nativeAddFlags)(JNIEnv * env, jclass cls, jlong p, jint flags) {
	IDebugBreakpoint2 * bp = (IDebugBreakpoint2 *)p;
	HRESULT hr = bp->AddFlags(flags);
	if (FAILED(hr))
		throwHRESULT(env, hr);
}
