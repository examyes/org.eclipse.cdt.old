#include <jni.h>
#include <windows.h>
#include <dbghelp.h>
#include "imagehlp_line64.h"

#define NATIVE(type, name) extern "C" JNIEXPORT type JNICALL Java_org_eclipse_cdt_debug_win32_core_os_IMAGEHLP_LINE64_##name

static jfieldID pID = NULL;

NATIVE(void, staticInitNative)(JNIEnv * env, jclass cls)
{
	pID = env->GetFieldID(cls, "p", "J");
}

NATIVE(void, initNative)(JNIEnv * env, jobject obj)
{
	IMAGEHLP_LINE64 * object = new IMAGEHLP_LINE64();
	ZeroMemory(object, sizeof(*object));
	env->SetLongField(obj, pID, (jlong)object);
}

IMAGEHLP_LINE64 * getIMAGEHLP_LINE64(JNIEnv * env, jobject obj)
{
	return (IMAGEHLP_LINE64 *)env->GetLongField(obj, pID);
}

NATIVE(void, clear)(JNIEnv * env, jobject obj)
{
	IMAGEHLP_LINE64 * object = getIMAGEHLP_LINE64(env, obj);
	ZeroMemory(object, sizeof(*object));
}

NATIVE(void, finalize)(JNIEnv * env, jobject obj)
{
	delete getIMAGEHLP_LINE64(env, obj);
}
