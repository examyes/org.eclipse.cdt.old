/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 ******************************************************************************/

#include <DbgEng.h>
#include <jni.h>

// Utility should be moved
static JavaVM * javaVM = NULL;

static JNIEnv * attachCurrentThread() {
	if (javaVM == NULL)
		return NULL;
	
	JNIEnv * env = NULL;
	javaVM->AttachCurrentThread((void **)&env, NULL);
	return env;
}

// Bridge to IDebugOutputCallbacks

#define NATIVE(type, name) extern "C" JNIEXPORT type JNICALL Java_org_eclipse_cdt_debug_win32_core_dbgeng_IDebugOutputCallbacks_##name

class DebugOutputCallbacks : public IDebugOutputCallbacks {
public:
	DebugOutputCallbacks(JNIEnv * env, jobject _obj)
		: count(1)
	{
		obj = env->NewGlobalRef(_obj);
		jclass cls = env->GetObjectClass(obj);
		outputID = env->GetMethodID(cls, "output", "(ILjava/lang/String;)V");
	}
	
	~DebugOutputCallbacks() {
		attachCurrentThread()->DeleteGlobalRef(obj);
	}
	
	HRESULT __stdcall QueryInterface(const IID & iid, PVOID * ref) {
		*ref = NULL;
		
		if (iid == __uuidof(IDebugOutputCallbacks))
			*ref = this;
		else if (iid == __uuidof(IUnknown))
			*ref = static_cast<IUnknown *>(this);

		return S_OK;
	}
	
	ULONG __stdcall AddRef(void) {
		return ++count;
	}
	
	ULONG _stdcall Release(void) {
		int c = --count;
		if (c == 0)
			delete this;
		return c;
	}
	
	HRESULT __stdcall Output(ULONG mask, PCSTR text) {
		JNIEnv * env = attachCurrentThread();
		jstring textStr = env->NewStringUTF(text);
		env->CallVoidMethod(obj, outputID, mask, textStr);
		return S_OK;
	}

	int count;
	jobject obj;
	jmethodID outputID;
};

// private native long create();
NATIVE(jlong, create)(JNIEnv * env, jobject obj) {
	if (javaVM == NULL)
		env->GetJavaVM(&javaVM);
		
	return (jlong)new DebugOutputCallbacks(env, obj);
}
