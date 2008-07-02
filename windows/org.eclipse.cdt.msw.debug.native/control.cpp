#include <jni.h>
#include <dbgeng.h>
#include "native.h"

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

NATIVE(jint, nativeGetStackTrace)(JNIEnv * env, jclass cls, jlong object,
		jlong frameOffset, jlong stackOffset, jlong instructionOffset, jlong frame) {
	IDebugControl4 * control = (IDebugControl4 *)object;
	DEBUG_STACK_FRAME * stackFrame = (DEBUG_STACK_FRAME *)frame;
	ULONG size;
	HRESULT hr = control->GetStackTrace(frameOffset, stackOffset, instructionOffset, stackFrame,
			FRAME_CHUNK_SIZE, &size);
	if (FAILED(hr))
		throwHRESULT(env, hr);
	return size;
}

NATIVE(jlong, nativeAddBreakpoint)(JNIEnv * env, jclass cls, jlong object, jint type, jint desiredId) {
	IDebugControl4 * control = (IDebugControl4 *)object;
	IDebugBreakpoint2 * bp;
	HRESULT hr = control->AddBreakpoint2(type, desiredId, &bp);
	if (FAILED(hr))
		throwHRESULT(env, hr);
	return (jlong)bp;
}

NATIVE(void, nativeRemoveBreakpoint)(JNIEnv * env, jclass cls, jlong object, jlong bp) {
	IDebugControl4 * control = (IDebugControl4 *)object;
	IDebugBreakpoint2 * dbp = (IDebugBreakpoint2 *)bp;
	HRESULT hr = control->RemoveBreakpoint2(dbp);
	if (FAILED(hr))
		throwHRESULT(env, hr);
}

NATIVE(jlong, nativeGetBreakpointById)(JNIEnv * env, jclass cls, jlong object, jint id) {
	IDebugControl4 * control = (IDebugControl4 *)object;
	IDebugBreakpoint2 * bp;
	HRESULT hr = control->GetBreakpointById2(id, &bp);
	if (FAILED(hr))
		throwHRESULT(env, hr);
	return (jlong)bp;
}
