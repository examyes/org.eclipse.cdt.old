#include <jni.h>
#include <windows.h>
#include <dbghelp.h>
#include "symbol_info.h"

#define NATIVE(type, name) extern "C" JNIEXPORT type JNICALL Java_org_eclipse_cdt_debug_win32_core_os_SYMBOL_INFO_##name

static jfieldID pID = NULL;

NATIVE(void, staticInitNative)(JNIEnv * env, jclass cls)
{
	pID = env->GetFieldID(cls, "p", "J");
}

NATIVE(void, initNative)(JNIEnv * env, jobject obj, jint maxNameLength)
{
	SYMBOL_INFO * symbolInfo = (SYMBOL_INFO *)new char[sizeof(SYMBOL_INFO) + maxNameLength];
	ZeroMemory(symbolInfo, sizeof(*symbolInfo));
	symbolInfo->SizeOfStruct = sizeof(*symbolInfo);
	symbolInfo->MaxNameLen = maxNameLength;
	env->SetLongField(obj, pID, (jlong)symbolInfo);
}

SYMBOL_INFO * getSymbolInfo(JNIEnv * env, jobject obj)
{
	return (SYMBOL_INFO *)env->GetLongField(obj, pID);
}

NATIVE(void, clear)(JNIEnv * env, jobject obj)
{
	SYMBOL_INFO * symbolInfo = getSymbolInfo(env, obj);
	int maxNameLength = symbolInfo->MaxNameLen;
	ZeroMemory(symbolInfo, sizeof(*symbolInfo));
	symbolInfo->SizeOfStruct = sizeof(*symbolInfo);
	symbolInfo->MaxNameLen = maxNameLength;
}

NATIVE(void, finalize)(JNIEnv * env, jobject obj)
{
	delete [] (char *)getSymbolInfo(env, obj);
}
