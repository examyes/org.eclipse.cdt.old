#include <windows.h>
#include <jni.h>

wchar_t * getString(JNIEnv * env, jstring string) {
	jsize length = env->GetStringLength(string);
	const jchar * jchars = env->GetStringChars(string, NULL);
	wchar_t * _string = new wchar_t[length + 1];
	CopyMemory(_string, jchars, length * sizeof(wchar_t));
	_string[length] = 0;
	env->ReleaseStringChars(string, jchars);
	return _string;
}
