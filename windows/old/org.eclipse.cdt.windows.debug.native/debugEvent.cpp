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

extern "C"
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_DebugEvent_getDebugEventCode(
		JNIEnv * env, jclass cls, jlong debugEvent) {
	return ((DEBUG_EVENT *)debugEvent)->dwDebugEventCode;
}

extern "C"
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_DebugEvent_getProcessId(
		JNIEnv * env, jclass cls, jlong debugEvent) {
	return ((DEBUG_EVENT *)debugEvent)->dwProcessId;
}

extern "C"
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_DebugEvent_getThreadId(
		JNIEnv * env, jclass cls, jlong debugEvent) {
	return ((DEBUG_EVENT *)debugEvent)->dwThreadId;
}

extern "C"
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_DebugEvent_getExitProcessExitCode(
		JNIEnv * env, jobject obj, jlong debugEvent) {
	return ((DEBUG_EVENT *)debugEvent)->u.ExitProcess.dwExitCode;
}
