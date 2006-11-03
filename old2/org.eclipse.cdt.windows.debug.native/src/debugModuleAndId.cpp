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
#include <dbgeng.h>
#include "debugModuleAndId.h"
#include "util.h"

static jclass cls = NULL;
static jmethodID constructorID = NULL;
static jfieldID moduleBaseID = NULL;
static jfieldID idID = NULL;

jobject createObject(JNIEnv * env, DEBUG_MODULE_AND_ID & mid) {
	if (cls == NULL) {
		cls = env->FindClass("org/eclipse/cdt/windows/debug/core/DebugModuleAndId");
		checkNull(env, cls);
		constructorID = env->GetMethodID(cls, "<init>", "(JJ)V");
		checkNull(env, constructorID);
	}
	return env->NewObject(cls, constructorID, mid.ModuleBase, mid.Id);
}

void getObject(JNIEnv * env, jobject obj, DEBUG_MODULE_AND_ID & mid) {
	if (moduleBaseID == NULL) {
		if (cls == NULL)
			cls = env->GetObjectClass(obj);
		moduleBaseID = env->GetFieldID(cls, "moduleBase", "J");
		idID = env->GetFieldID(cls, "id", "J");
	}
	mid.ModuleBase = env->GetLongField(obj, moduleBaseID);
	mid.Id = env->GetLongField(obj, idID);
}
