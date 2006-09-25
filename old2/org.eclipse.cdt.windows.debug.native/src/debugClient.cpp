#include <windows.h>
#include <dbgeng.h>
#include <jni.h>

// Class IDebugClient

#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_IDebugClient_ ## name

extern "C" JNIEXPORT jobject JNINAME(debugCreate)(JNIEnv * env, jclass cls) {
	IDebugClient5 * debugClient;
	HRESULT hr = DebugCreate(__uuidof(IDebugClient5), (void **)&debugClient);
	if (hr != S_OK)
		return NULL;

	jmethodID constructor = env->GetMethodID(cls, "<init>", "(J)V");
	if (constructor == 0)
		return NULL;
	
	return env->NewObject(cls, constructor, (jlong)debugClient);
}
