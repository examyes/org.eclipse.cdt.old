#include <windows.h>
#include <jni.h>

// MSVC doesn't need this, and our parser doesn't support __stdcall yet
#undef JNICALL
#define JNICALL

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_Win32Debug_test(
		JNIEnv * env, jclass cls) {
	return JNI_TRUE;
}

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_Win32Debug_ContinueDebugEvent(
		JNIEnv * env, jclass cls,
		jint processId, jint threadId, jint continueStatus) {
	return ContinueDebugEvent(processId, threadId, continueStatus) ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_Win32Debug_DebugBreakProcess(
		JNIEnv * env, jclass cls, long processHandle) {
	return DebugBreakProcess((HANDLE)processHandle) ? JNI_TRUE : JNI_FALSE;
}

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_Win32Debug_ReadProcessMemory(
		JNIEnv * env, jclass cls,
		jlong processHandle, jlong baseAddress, jbyteArray buffer, jlongArray numberOfBytesRead) {
	jbyte * bytes = env->GetByteArrayElements(buffer, NULL);
	jsize size = env->GetArrayLength(buffer);
	
	DWORD numRead;
	jboolean rc = ReadProcessMemory((HANDLE)processHandle, (void *)baseAddress, bytes, size, &numRead)
		? JNI_TRUE : JNI_FALSE;

	if (numberOfBytesRead != NULL && env->GetArrayLength(numberOfBytesRead) > 0) {
		jlong jnumRead = numRead;
		env->SetLongArrayRegion(numberOfBytesRead, 0, 1, &jnumRead);
	}
	
	env->ReleaseByteArrayElements(buffer, bytes, 0);
	
	return rc;
}

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_Win32Debug_WaitForDebugEvent(
		JNIEnv * env, jclass cls,
		jlong debugEvent, jint milliseconds) {
	return WaitForDebugEvent((DEBUG_EVENT *)debugEvent, milliseconds);
}

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_Win32Debug_WriteProcessMemory(
		JNIEnv * env, jclass cls,
		jlong processHandle, jlong baseAddress,
		jbyteArray buffer, jlongArray numberOfBytesWritten) {
	jbyte * bytes = env->GetByteArrayElements(buffer, NULL);
	jsize size = env->GetArrayLength(buffer);
	
	DWORD numWritten;
	jboolean rc = WriteProcessMemory((HANDLE)processHandle, (void *)baseAddress, bytes, size, &numWritten)
		? JNI_TRUE : JNI_FALSE;
	
	if (numberOfBytesWritten != NULL && env->GetArrayLength(numberOfBytesWritten) > 0) {
		jlong jnumWritten = numWritten;
		env->SetLongArrayRegion(numberOfBytesWritten, 0, 1, &jnumWritten);
	}

	env->ReleaseByteArrayElements(buffer, bytes, 0);
	
	return rc;
}
