// Implementation of org.eclipse.cdt.debug.win32.core.os.OS

#include <jni.h>
#include <windows.h>
#include <dbghelp.h>
#include "context.h"
#include "stackframe64.h"
#include "symbol_info.h"
#include "imagehlp_line64.h"

// There is no native object implemented in this class. This is just a conduit
// to the Platform SDK calls

#define NATIVE(type, name) extern "C" JNIEXPORT type JNICALL Java_org_eclipse_cdt_debug_win32_core_os_OS_##name

NATIVE(jboolean, GetThreadContext)(JNIEnv * env, jclass cls, jlong threadHandle, jobject contextObj)
{
	return GetThreadContext(reinterpret_cast<HANDLE>(threadHandle), getContext(env, contextObj))
		? JNI_TRUE : JNI_FALSE;
}

NATIVE(jboolean, SymInitialize)(JNIEnv * env, jclass cls,
								jlong processHandle,
								jstring userSearchPath,
								jboolean invadeProcess)
{
	PSTR _userSearchPath = NULL;
	if (userSearchPath != NULL)
		_userSearchPath = const_cast<PSTR>(env->GetStringUTFChars(userSearchPath, NULL));
		
	jboolean result = SymInitialize(reinterpret_cast<HANDLE>(processHandle), _userSearchPath, invadeProcess)
		? JNI_TRUE : JNI_FALSE;

	if (userSearchPath != NULL)
		env->ReleaseStringUTFChars(userSearchPath, _userSearchPath);
	return result;
}

NATIVE(jboolean, SymCleanup)(JNIEnv * env, jclass cls, jlong processHandle)
{
	return SymCleanup(reinterpret_cast<HANDLE>(processHandle));
}

NATIVE(jboolean, StackWalk64)(JNIEnv * env, jclass cls,
							  jint machineType,
							  jlong processHandle,
							  jlong threadHandle,
							  jobject stackFrame,
							  jobject context)
{
	return StackWalk64(
		machineType,
		reinterpret_cast<HANDLE>(processHandle),
		reinterpret_cast<HANDLE>(threadHandle),
		getStackFrame64(env, stackFrame),
		getContext(env, context),
		NULL,
		SymFunctionTableAccess64,
		SymGetModuleBase64,
		NULL) ? JNI_TRUE : JNI_FALSE;
}

NATIVE(jboolean, SymFromAddr)(JNIEnv * env, jclass cls,
							  jlong processHandle,
							  jlong address,
							  jlongArray displacement,
							  jobject symbol)
{
	DWORD64 disp;
	jboolean rc = SymFromAddr(
		reinterpret_cast<HANDLE>(processHandle),
		address,
		&disp,
		getSymbolInfo(env, symbol)) ? JNI_TRUE : JNI_FALSE;

	env->SetLongArrayRegion(displacement, 0, 1, (jlong *)&disp);
	return rc;
}

NATIVE(jboolean, SymGetLineFromAddr64)(JNIEnv * env, jclass cls,
									   jlong processHandle,
									   jlong address,
									   jintArray displacement,
									   jobject line)
{
	DWORD disp;
	jboolean rc = SymGetLineFromAddr64(
		reinterpret_cast<HANDLE>(processHandle),
		address,
		&disp,
		getIMAGEHLP_LINE64(env, line)) ? JNI_TRUE : JNI_FALSE;
	
	env->SetIntArrayRegion(displacement, 0, 1, (jint *)&disp);
	return rc;
}
					