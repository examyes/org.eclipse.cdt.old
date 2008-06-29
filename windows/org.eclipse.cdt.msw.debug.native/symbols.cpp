#include <jni.h>
#include <dbgeng.h>
#include "native.h"

#define NATIVE(ret, func) extern "C" JNIEXPORT ret JNICALL Java_org_eclipse_cdt_msw_debug_dbgeng_IDebugSymbols_##func

NATIVE(jstring, nativeGetNameByOffset)(JNIEnv * env, jclass cls, jlong object, jlong offset) {
	IDebugSymbols3 * symbols = (IDebugSymbols3 *)object;
	wchar_t buff[1024];
	ULONG size;
	HRESULT hr = symbols->GetNameByOffsetWide(offset, buff, sizeof(buff)/sizeof(wchar_t), &size, NULL);
	if (FAILED(hr)) {
		throwHRESULT(env, hr);
		return NULL;
	}
	return env->NewString((jchar *)buff, size - 1); // size seems to include the terminating zero
}

NATIVE(jint, nativeGetSymbolOptions)(JNIEnv * env, jclass cls, jlong object) {
	IDebugSymbols3 * symbols = (IDebugSymbols3 *)object;
	ULONG options;
	HRESULT hr = symbols->GetSymbolOptions(&options);
	if (FAILED(hr)) {
		throwHRESULT(env, hr);
		return 0;
	}
	return options;
}

NATIVE(void, nativeSetSymbolOptions)(JNIEnv * env, jclass cls, jlong object, jint options) {
	IDebugSymbols3 * symbols = (IDebugSymbols3 *)object;
	HRESULT hr = symbols->SetSymbolOptions(options);
	if (FAILED(hr))
		throwHRESULT(env, hr);
}

NATIVE(jboolean, nativeGetSymbolEntryByOffset)(JNIEnv * env, jclass cls, jlong object,
		jlong offset, jint flags, jbyteArray id) {
	IDebugSymbols3 * symbols = (IDebugSymbols3 *)object;
	DEBUG_MODULE_AND_ID mid;
	ULONG64 disp;
	ULONG entries;
	HRESULT hr = symbols->GetSymbolEntriesByOffset(offset, flags, &mid, &disp, 1, &entries);
	if (FAILED(hr)) {
		throwHRESULT(env, hr);
		return JNI_FALSE;
	} else if (entries > 0) {
		env->SetByteArrayRegion(id, 0, sizeof(mid), (jbyte *)&mid);
		return JNI_TRUE;
	} else
		return JNI_FALSE;
}

NATIVE(jstring, nativeGetSymbolEntryString)(JNIEnv * env, jclass cls, jlong object,
		jbyteArray id, jint which) {
	IDebugSymbols3 * symbols = (IDebugSymbols3 *)object;
	DEBUG_MODULE_AND_ID mid;
	env->GetByteArrayRegion(id, 0, sizeof(mid), (jbyte *)&mid);
	wchar_t buff[1024];
	ULONG size;
	HRESULT hr = symbols->GetSymbolEntryStringWide(&mid, which, buff, sizeof(buff)/sizeof(wchar_t), &size);
	if (FAILED(hr)) {
		throwHRESULT(env, hr);
		return NULL;
	}
	return env->NewString((jchar *)buff, size - 1);
}

NATIVE(void, nativeSetImagePath)(JNIEnv * env, jclass cls, jlong object, jstring path) {
	IDebugSymbols3 * symbols = (IDebugSymbols3 *)object;
	const jchar * pathstr = env->GetStringChars(path, NULL);
	HRESULT hr = symbols->SetImagePathWide((const wchar_t *)pathstr);
	if (FAILED(hr)) {
		throwHRESULT(env, hr);
	}
	env->ReleaseStringChars(path, pathstr);
}

NATIVE(void, nativeSetSymbolPath)(JNIEnv * env, jclass cls, jlong object, jstring path) {
	IDebugSymbols3 * symbols = (IDebugSymbols3 *)object;
	const jchar * pathstr = env->GetStringChars(path, NULL);
	HRESULT hr = symbols->SetSymbolPathWide((const wchar_t *)pathstr);
	if (FAILED(hr)) {
		throwHRESULT(env, hr);
	}
	env->ReleaseStringChars(path, pathstr);
}

