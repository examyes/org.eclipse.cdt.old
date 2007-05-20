#include <windows.h>
#include <jni.h>

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_Win32Debug_WriteProcessMemory(
		JNIEnv * env, jclass cls,
		jlong processHandle, jlong baseAddress,
		jobjectArray buffer, jobjectArray numberOfBytesWritten) {
	return JNI_TRUE;
}
