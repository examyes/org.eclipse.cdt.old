#include <windows.h>
#include <jni.h>

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
