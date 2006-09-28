#include <windows.h>
#include <dbgeng.h>
#include <jni.h>

#include "HRESULTFailure.h"
#include "debugEventCallbacks.h"

// Class IDebugEventCallbacks
#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_IDebugEventCallbacks_## name

static JavaVM * vm;

static jfieldID pID;

static jmethodID getInterestMaskID;
static jmethodID createProcessID;
static jmethodID exitProcessID;

DebugEventCallbacks * DebugEventCallbacks::getObject(JNIEnv * env, jobject obj) {
	jlong p = env->GetLongField(obj, pID);
	return (DebugEventCallbacks *)p;
}

extern "C" JNIEXPORT jlong JNINAME(init)(JNIEnv * env, jobject obj) {
	if (env->GetJavaVM(&vm)) {
		throwHRESULT(env, E_FAIL, __FILE__, __LINE__);
		return NULL;
	}		
	
	jclass cls = env->GetObjectClass(obj);
	if (cls == NULL) {
		throwHRESULT(env, E_FAIL, __FILE__, __LINE__);
		return NULL;
	}
	
	pID = env->GetFieldID(cls, "p", "J");
	if (pID == 0) {
		throwHRESULT(env, E_FAIL, __FILE__, __LINE__);
		return NULL;
	}
	
	getInterestMaskID = env->GetMethodID(cls, "getInterestMask", "()I");
	if (getInterestMaskID == 0) {
		throwHRESULT(env, E_FAIL, __FILE__, __LINE__);
		return NULL;
	}
	
	createProcessID = env->GetMethodID(cls, "createProcess", "(JJJILjava/lang/String;Ljava/lang/String;IIJJJ)I");
	if (createProcessID == 0) {
		throwHRESULT(env, E_FAIL, __FILE__, __LINE__);
		return NULL;
	}
	
	exitProcessID = env->GetMethodID(cls, "exitProcess", "(I)I");
	if (exitProcessID == 0) {
		throwHRESULT(env, E_FAIL, __FILE__, __LINE__);
		return NULL;
	}
		
	return (jlong)new DebugEventCallbacks(env, obj);
}

DebugEventCallbacks::DebugEventCallbacks(JNIEnv * env, jobject obj)
{
	ref = env->NewGlobalRef(obj);
}

DebugEventCallbacks::~DebugEventCallbacks()
{
	// env->DeleteGlobalRef(obj);
}

ULONG __stdcall DebugEventCallbacks::AddRef() {
	return 1;
}

ULONG __stdcall DebugEventCallbacks::Release() {
	return 1;
}

static JNIEnv * getEnv() {
}

HRESULT __stdcall DebugEventCallbacks::GetInterestMask(ULONG * Mask) {
	JNIEnv * env;
	if (vm->GetEnv((void **)&env, JNI_VERSION_1_4) != JNI_OK) {
		fprintf(stderr, "DebugEventCallbacks: Failed to get env\n");
		return E_FAIL;
	}

	*Mask = env->CallIntMethod(ref, getInterestMaskID);
	return S_OK;
}

HRESULT __stdcall DebugEventCallbacks::CreateProcess(
		ULONG64 ImageFileHandle,
		ULONG64 Handle,
		ULONG64 BaseOffset,
		ULONG ModuleSize,
		PCWSTR ModuleName,
		PCWSTR ImageName,
		ULONG CheckSum,
		ULONG TimeDateStamp,
		ULONG64 InitialThreadHandle,
		ULONG64 ThreadDataOffset,
		ULONG64 StartOffset) {
	JNIEnv * env;
	if (vm->GetEnv((void **)&env, JNI_VERSION_1_4) != JNI_OK) {
		fprintf(stderr, "DebugEventCallbacks: Failed to get env\n");
		return E_FAIL;
	}
	
	jstring moduleName = env->NewString((const jchar *)ModuleName, wcslen(ModuleName));
	jstring imageName = env->NewString((const jchar *)ImageName, wcslen(ImageName));
	
	return env->CallIntMethod(ref, createProcessID, ImageFileHandle, Handle,
		BaseOffset, ModuleSize, moduleName, imageName, CheckSum, TimeDateStamp,
		InitialThreadHandle, ThreadDataOffset, StartOffset); 
}

HRESULT __stdcall DebugEventCallbacks::ExitProcess(ULONG ExitCode) {
	JNIEnv * env;
	if (vm->GetEnv((void **)&env, JNI_VERSION_1_4) != JNI_OK) {
		fprintf(stderr, "DebugEventCallbacks: Failed to get env\n");
		return E_FAIL;
	}
	
	return env->CallIntMethod(ref, exitProcessID, ExitCode);
}
