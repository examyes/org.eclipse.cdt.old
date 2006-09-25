#include <windows.h>
#include <dbgeng.h>
#include <jni.h>

#include "util.h"
#include "HRESULTFailure.h"

// Class IDebugControl

#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_IDebugControl_## name

static IDebugControl4 * getObject(JNIEnv * env, jobject obj) {
	jclass cls = env->GetObjectClass(obj);
	if (cls == NULL) {
		throwHRESULT(env, E_FAIL);
		return NULL;
	}
	
	jfieldID fieldID = env->GetFieldID(cls, "p", "J");
	if (fieldID == 0) {
		throwHRESULT(env, E_FAIL);
		return NULL;
	}
	
	jlong p = env->GetLongField(obj, fieldID);
	return (IDebugControl4 *)p;
}

extern "C" JNIEXPORT jobject JNINAME(debugCreate)(JNIEnv * env, jclass cls) {
	IDebugControl4 * debugControl;
	HRESULT hr = DebugCreate(__uuidof(IDebugControl4), (void **)&debugControl);
	if (hr != S_OK) {
		throwHRESULT(env, hr);
		return NULL;
	}

	jmethodID constructor = env->GetMethodID(cls, "<init>", "(J)V");
	if (constructor == 0) {
		throwHRESULT(env, E_FAIL);
		return NULL;
	}
	
	return env->NewObject(cls, constructor, (jlong)debugControl);
}

extern "C" JNIEXPORT jint JNINAME(waitForEvent)(JNIEnv * env, jobject obj,
		jint flags, jint timeout) {
	IDebugControl4 * debugControl = getObject(env, obj);
	printf("debugControl %x\n", debugControl);
	printf("flags %d\n", flags);
	printf("timeout %d\n", timeout);
	HRESULT hr = debugControl->WaitForEvent(flags, timeout);
	if (FAILED(hr)) {
		printf("Failed %x\n", hr);
		throwHRESULT(env, hr);
	}
	return hr;
}
