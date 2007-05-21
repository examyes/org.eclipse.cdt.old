#include <windows.h>
#include <jni.h>

// MSVC doesn't need this, and our parser doesn't support __stdcall yet
#undef JNICALL
#define JNICALL

extern "C"
JNIEXPORT jlong JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_DebugEvent_allocateDebugEvent(
		JNIEnv * env, jclass cls) {
	return (jlong)new DEBUG_EVENT;
}

extern "C"
JNIEXPORT void JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_DebugEvent_freeDebugEvent(
		JNIEnv * env, jclass cls, jlong debugEvent) {
	delete (DEBUG_EVENT *)debugEvent;
}
