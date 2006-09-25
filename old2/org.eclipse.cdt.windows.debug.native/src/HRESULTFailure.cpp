#include <windows.h>
#include <jni.h>
#include <comdef.h>

#include "HRESULTFailure.h"

void throwHRESULT(JNIEnv * env, HRESULT hr) {
	jclass cls = env->FindClass("org/eclipse/cdt/windows/debug/core/HRESULTFailure");
	if (cls == NULL)
		return;
	
	jmethodID constructor = env->GetMethodID(cls, "<init>", "(ILjava/lang/String;)V");
	if (constructor == NULL)
		return;

	_com_error error(hr);
	jstring message = env->NewStringUTF(error.ErrorMessage());
	
	env->Throw((jthrowable)env->NewObject(cls, constructor, (jint)hr, message)); 
}
