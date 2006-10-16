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
#include <dbgeng.h>
#include <jni.h>

#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_HRESULT_ ## name

extern "C" JNIEXPORT jboolean JNINAME(FAILED)(JNIEnv * env, jclass cls, jint hr) {
	return FAILED(hr) ? JNI_TRUE : JNI_FALSE;
}
