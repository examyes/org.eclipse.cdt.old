/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/
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

void checkNull(JNIEnv * env, void * ptr) {
	if (ptr != NULL)
		return;
	
	// Throw an NPE
	jclass npecls = env->FindClass("java/lang/NullPointerException");
	jmethodID cons = env->GetMethodID(npecls, "<init>", "()V");
	jobject npe = env->NewObject(npecls, cons);
	throw npe;
}

static jfieldID stringID = NULL;

void setString(JNIEnv * env, jobject obj, wchar_t * string) {
	if (stringID == NULL) {
		stringID = env->GetFieldID(env->GetObjectClass(obj), "string", "Ljava/lang/String;");
		checkNull(env, stringID);
	}
	
	env->SetObjectField(obj, stringID, env->NewString((jchar *)string, wcslen(string)));
}

static jfieldID longID = NULL;

void setLong(JNIEnv * env, jobject obj, jlong l) {
	if (longID == NULL) {
		longID = env->GetFieldID(env->GetObjectClass(obj), "l", "J");
		checkNull(env, longID);
	}
	
	env->SetLongField(obj, longID, l);
}

static jfieldID intID = NULL;

void setInt(JNIEnv * env, jobject obj, jint i) {
	if (intID == NULL) {
		intID = env->GetFieldID(env->GetObjectClass(obj), "i", "I");
		checkNull(env, intID);
	}
	
	env->SetIntField(obj, intID, i);
}

static jfieldID intArrayID = NULL;

void setIntArray(JNIEnv * env, jobject obj, jint * i, int count) {
	if (intArrayID == NULL) {
		intArrayID = env->GetFieldID(env->GetObjectClass(obj), "ints", "[I");
		checkNull(env, intArrayID);
	}
	
	jintArray intArray = env->NewIntArray(count);
	env->SetIntArrayRegion(intArray, 0, count, i);
	env->SetObjectField(obj, intArrayID, intArray);
}
