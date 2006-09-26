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
