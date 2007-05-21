#include <windows.h>
#include <jni.h>

#undef JNICALL
#define JNICALL

// create

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_DebugProcess_create(
		JNIEnv * env, jclass cls,
		jstring cmdline, jstring envp, jstring dir, jlongArray handles) {
	const jchar * _cmdline = env->GetStringChars(cmdline, NULL);
	jsize cmdlineLen = env->GetStringLength(cmdline) + 1;
	wchar_t * wcmdline = new wchar_t[cmdlineLen];
	wcscpy_s(wcmdline, cmdlineLen, (const wchar_t *)_cmdline);
	
	const jchar * _envp = envp != NULL ? env->GetStringChars(envp, NULL) : NULL;
	jsize envpLen = env->GetStringLength(envp);
	wchar_t * wenvp = new wchar_t[envpLen];
	wcscpy_s(wenvp, envpLen, (const wchar_t *)_envp);

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
	
	DWORD flags = DEBUG_PROCESS | CREATE_UNICODE_ENVIRONMENT;
	
	jboolean rc = CreateProcess(NULL, wcmdline, NULL, NULL, TRUE, flags,
			wenvp, (const wchar_t *)dir, &si, &pi)
		? JNI_TRUE : JNI_FALSE;
	
	delete[] wcmdline;
	delete[] wenvp;
	
	env->ReleaseStringChars(cmdline, _cmdline);
	env->ReleaseStringChars(envp, _envp);
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

// DebugInputStream.ReadFile

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_DebugInputStream_ReadFile(
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

// DebugOutputProcess.WriteFile

extern "C"
JNIEXPORT jboolean JNICALL Java_org_eclipse_cdt_windows_debug_core_sdk_DebugOutputStream_WriteFile(
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

