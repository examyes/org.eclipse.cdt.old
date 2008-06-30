#include <jni.h>
#include <dbgeng.h>
#include "native.h"

#define NATIVE(ret, func) extern "C" JNIEXPORT ret JNICALL Java_org_eclipse_cdt_msw_debug_dbgeng_IDebugSymbolGroup_##func

NATIVE(jint, nativeGetNumberSymbols)(JNIEnv * env, jclass cls, jlong object) {
	IDebugSymbolGroup2 * symbolGroup = (IDebugSymbolGroup2 *)object;
	ULONG number;
	HRESULT hr = symbolGroup->GetNumberSymbols(&number);
	if (FAILED(hr))
		throwHRESULT(env, hr);
	return number;
}

NATIVE(jstring, nativeGetSymbolName)(JNIEnv * env, jclass cls, jlong object, jint index) {
	IDebugSymbolGroup2 * symbolGroup = (IDebugSymbolGroup2 *)object;
	wchar_t buff[1024];
	ULONG size;
	HRESULT hr = symbolGroup->GetSymbolNameWide(index, buff, sizeof(buff)/sizeof(wchar_t), &size);
	if (FAILED(hr)) {
		throwHRESULT(env, hr);
		return NULL;
	}
	return env->NewString((jchar *)buff, size - 1);
}

NATIVE(jstring, nativeGetSymbolValueText)(JNIEnv * env, jclass cls, jlong object, jint index) {
	IDebugSymbolGroup2 * symbolGroup = (IDebugSymbolGroup2 *)object;
	wchar_t buff[1024];
	ULONG size;
	HRESULT hr = symbolGroup->GetSymbolValueTextWide(index, buff, sizeof(buff)/sizeof(wchar_t), &size);
	if (FAILED(hr)) {
		throwHRESULT(env, hr);
		return NULL;
	}
	return env->NewString((jchar *)buff, size - 1);
}
