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
