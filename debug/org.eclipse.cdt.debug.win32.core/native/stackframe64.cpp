#include <jni.h>
#include <windows.h>
#include <dbghelp.h>
#include "stackframe64.h"

#define NATIVE(type, name) extern "C" JNIEXPORT type JNICALL Java_org_eclipse_cdt_debug_win32_core_os_STACKFRAME64_##name

static jfieldID pID = NULL;

NATIVE(void, staticInitNative)(JNIEnv * env, jclass cls)
{
	pID = env->GetFieldID(cls, "p", "J");
}

NATIVE(void, initNative)(JNIEnv * env, jobject obj)
{
	STACKFRAME64 * stackframe64 = new STACKFRAME64();
	ZeroMemory(stackframe64, sizeof(*stackframe64));
	env->SetLongField(obj, pID, (jlong)stackframe64);
}

STACKFRAME64 * getStackFrame64(JNIEnv * env, jobject obj)
{
	return (STACKFRAME64 *)env->GetLongField(obj, pID);
}

NATIVE(void, clear)(JNIEnv * env, jobject obj)
{
	STACKFRAME64 * stackframe = getStackFrame64(env, obj);
	ZeroMemory(stackframe, sizeof(*stackframe));
}

NATIVE(void, finalize)(JNIEnv * env, jobject obj)
{
	delete (STACKFRAME64 *)getStackFrame64(env, obj);
}
