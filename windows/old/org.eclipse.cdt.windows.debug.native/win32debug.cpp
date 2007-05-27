#include <windows.h>
#include <jni.h>

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
	
	SIZE_T numRead;
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
	if (debugEvent == 0)
		env->ThrowNew(env->FindClass("java/lang/NullPointerException"), "WaitForDebugEvent debugEvent");
		
	return WaitForDebugEvent((DEBUG_EVENT *)debugEvent, milliseconds);
}

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_Win32Debug_WriteProcessMemory(
		JNIEnv * env, jclass cls,
		jlong processHandle, jlong baseAddress,
		jbyteArray buffer, jlongArray numberOfBytesWritten) {
	jbyte * bytes = env->GetByteArrayElements(buffer, NULL);
	jsize size = env->GetArrayLength(buffer);
	
	SIZE_T numWritten;
	jboolean rc = WriteProcessMemory((HANDLE)processHandle, (void *)baseAddress, bytes, size, &numWritten)
		? JNI_TRUE : JNI_FALSE;
	
	if (numberOfBytesWritten != NULL && env->GetArrayLength(numberOfBytesWritten) > 0) {
		jlong jnumWritten = numWritten;
		env->SetLongArrayRegion(numberOfBytesWritten, 0, 1, &jnumWritten);
	}

	env->ReleaseByteArrayElements(buffer, bytes, 0);
	
	return rc;
}

// CreateProcess

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_Win32Debug_CreateProcess(
		JNIEnv * env, jclass cls,
		jstring cmdline, jstring envp, jstring dir, jlongArray handles) {
	if (cmdline == NULL)
		env->ThrowNew(env->FindClass("java/lang/NullPointerException"), "CreateProcess cmdline");
		
	if (handles == NULL)
		env->ThrowNew(env->FindClass("java/lang/NullPointerException"), "CreateProcess handles");
		
	if (env->GetArrayLength(handles) < 4)
		env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), "CreateProcess handles length");
	
	const jchar * _cmdline = env->GetStringChars(cmdline, NULL);
	jsize cmdlineLen = env->GetStringLength(cmdline) + 1;
	wchar_t * wcmdline = new wchar_t[cmdlineLen];
	wcscpy_s(wcmdline, cmdlineLen, (const wchar_t *)_cmdline);
	
	const jchar * _envp = NULL;
	wchar_t * wenvp = NULL;
	if (envp != NULL) {
		_envp = env->GetStringChars(envp, NULL);
		jsize envpLen = env->GetStringLength(envp);
		wenvp = new wchar_t[envpLen];
		wcscpy_s(wenvp, envpLen, (const wchar_t *)_envp);
	}

	const jchar * _dir = dir != NULL ? env->GetStringChars(dir, NULL) : NULL;
	
	// TODO Create child pipes for stdin, stdout, stderr
	SECURITY_ATTRIBUTES sa;
	sa.nLength = sizeof(sa);
	sa.bInheritHandle = TRUE;
	sa.lpSecurityDescriptor = NULL;
	
	HANDLE childStdinRd, childStdinWr;
	CreatePipe(&childStdinRd, &childStdinWr, &sa, 0);
	SetHandleInformation(childStdinWr, HANDLE_FLAG_INHERIT, 0);
	
	HANDLE childStdoutRd, childStdoutWr;
	CreatePipe(&childStdoutRd, &childStdoutWr, &sa, 0);
	SetHandleInformation(childStdoutRd, HANDLE_FLAG_INHERIT, 0);
	
	HANDLE childStderrRd, childStderrWr;
	CreatePipe(&childStderrRd, &childStderrWr, &sa, 0);
	SetHandleInformation(childStderrRd, HANDLE_FLAG_INHERIT, 0);

	PROCESS_INFORMATION pi;
	ZeroMemory(&pi, sizeof(pi));
	
	STARTUPINFO si;
	ZeroMemory(&si, sizeof(si));
	si.cb = sizeof(si);
	
	DWORD flags
		= DEBUG_ONLY_THIS_PROCESS
		| CREATE_UNICODE_ENVIRONMENT
		| CREATE_NEW_PROCESS_GROUP;
	
	jboolean rc = CreateProcess(NULL, wcmdline, NULL, NULL, TRUE, flags,
			wenvp, (const wchar_t *)dir, &si, &pi)
		? JNI_TRUE : JNI_FALSE;
	
	delete[] wcmdline;
	delete[] wenvp;
	
	env->ReleaseStringChars(cmdline, _cmdline);
	if (envp != NULL)
		env->ReleaseStringChars(envp, _envp);
	if (dir != NULL)
		env->ReleaseStringChars(dir, _dir);
	
	CloseHandle(pi.hThread);
	CloseHandle(childStdinRd);
	CloseHandle(childStdoutWr);
	CloseHandle(childStderrWr);
	
	jlong _handles[4];
	_handles[0] = (jlong)pi.hProcess;
	_handles[1] = (jlong)childStdinWr;
	_handles[2] = (jlong)childStdoutRd;
	_handles[3] = (jlong)childStderrRd;
	
	env->SetLongArrayRegion(handles, 0, 4, _handles);
	
	return rc;
}

// GetExitCodeProcess

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_Win32Debug_GetExitCodeProcess(
		JNIEnv * env, jclass cls,
		jlong processHandle, jintArray exitCode) {
	DWORD code;
	if (!GetExitCodeProcess((HANDLE)processHandle, &code))
		return JNI_FALSE;
	env->SetIntArrayRegion(exitCode, 0, 1, (jint *)&code);
	return JNI_TRUE;
}

// WaitForSingleObject

extern "C"
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_Win32Debug_WaitForSingleObject(
		JNIEnv * env, jclass cls, jlong handle, jint milliseconds) {
	return WaitForSingleObject((HANDLE)handle, milliseconds);
}

// TerminateProcess

extern "C"
JNIEXPORT jint JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_Win32Debug_TerminateProcess(
		JNIEnv * env, jclass cls, jlong processHandle, jint exitCode) {
	return TerminateProcess((HANDLE)processHandle, exitCode);
}

// GenerateConsoleCtrlEvent

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_Win32Debug_GenerateConsoleCtrlEvent(
		JNIEnv * env, jclass cls, jint ctrlEvent, jint processGroupId) {
	return GenerateConsoleCtrlEvent(ctrlEvent, processGroupId) ? JNI_TRUE : JNI_FALSE;
}

// GetProcessId

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_Win32Debug_GetProcessId(
		JNIEnv * env, jclass cls, jlong processHandle) {
	return GetProcessId((HANDLE)processHandle);
}

// ReadFile

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_Win32Debug_ReadFile(
		JNIEnv * env, jclass cls,
		jlong handle, jbyteArray buffer, jintArray numberOfBytesRead) {
	jbyte * bytes = env->GetByteArrayElements(buffer, NULL);
	jsize size = env->GetArrayLength(buffer);
	
	DWORD numRead;
	jboolean rc = ReadFile((HANDLE)handle, bytes, size, &numRead, NULL) ? JNI_TRUE : JNI_FALSE;
	
	env->SetIntArrayRegion(numberOfBytesRead, 0, 1, (jint *)&numRead);
	env->ReleaseByteArrayElements(buffer, bytes, 0);
	
	return rc;
}

// WriteFile

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_Win32Debug_WriteFile(
		JNIEnv * env, jclass cls,
		jlong handle, jbyteArray buffer, jint numberOfBytesToWrite,	jintArray numberOfBytesWritten) {
	jbyte * bytes = env->GetByteArrayElements(buffer, NULL);
	jsize size = env->GetArrayLength(buffer);
	
	DWORD numWritten;
	jboolean rc = WriteFile((HANDLE)handle, bytes, size, &numWritten, NULL) ? JNI_TRUE : JNI_FALSE;
	
	env->SetIntArrayRegion(numberOfBytesWritten, 0, 1, (jint *)&numWritten);
	env->ReleaseByteArrayElements(buffer, bytes, 0);
	
	return rc;
}
