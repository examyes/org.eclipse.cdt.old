// Implementation of org.eclipse.cdt.debug.win32.core.os.CONTEXT

#include <jni.h>
#include <windows.h>
#include "context.h"

#define NATIVE(type, name) extern "C" JNIEXPORT type JNICALL Java_org_eclipse_cdt_debug_win32_core_os_CONTEXT_##name

static jfieldID pID = NULL;

NATIVE(void, staticInitNative)(JNIEnv * env, jclass cls)
{
	pID = env->GetFieldID(cls, "p", "J");
}

NATIVE(void, initNative)(JNIEnv * env, jobject obj)
{
	CONTEXT * context = new CONTEXT();
	ZeroMemory(context, sizeof(*context));
	env->SetLongField(obj, pID, (jlong)context);
}

CONTEXT * getContext(JNIEnv * env, jobject obj)
{
	return (CONTEXT *)env->GetLongField(obj, pID);
}

NATIVE(void, clear)(JNIEnv * env, jobject obj)
{
	CONTEXT * context = getContext(env, obj);
	ZeroMemory(context, sizeof(*context));
}

NATIVE(void, finalize)(JNIEnv * env, jobject obj)
{
	delete (CONTEXT *)getContext(env, obj);
}
