#include <jni.h>
#include <dbgeng.h>
#include <string.h>
#include "hresult.h"

#define NATIVE(ret, func) extern "C" JNIEXPORT ret JNICALL Java_org_eclipse_cdt_msw_debug_dbgeng_IDebugClient_##func

NATIVE(void, nativeCreateProcess)(JNIEnv * env, jclass cls, jlong p, jlong server,
		jstring commandLine, jint createFlags) {
	IDebugClient5 * client = (IDebugClient5 *)p;
	const jchar * commandLineJchar = env->GetStringChars(commandLine, NULL);
	wchar_t * commandLineStr = _wcsdup((const wchar_t *)commandLineJchar);
	HRESULT hr = client->CreateProcessWide(server, commandLineStr, createFlags);
	free(commandLineStr);
	env->ReleaseStringChars(commandLine, commandLineJchar);
	if (hr != S_OK)
		throwHRESULT(env, hr);
}
