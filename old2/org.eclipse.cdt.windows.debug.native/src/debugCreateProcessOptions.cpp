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

#include "debugCreateProcessOptions.h"
#include "HRESULTFailure.h"

// Class DebugCreateProcessOptions

#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_DebugCreateProcessOptions_ ## name

void getDebugCreateProcessOptions(JNIEnv * env, jobject obj, DEBUG_CREATE_PROCESS_OPTIONS & options) {
	ZeroMemory(&options, sizeof(DEBUG_CREATE_PROCESS_OPTIONS));

	jclass cls = env->GetObjectClass(obj);
	if (cls == NULL) {
		throwHRESULT(env, E_FAIL);
		return;
	}
	
	jfieldID createFlagsID = env->GetFieldID(cls, "createFlags", "I");
	if (createFlagsID == 0) {
		throwHRESULT(env, E_FAIL);
		return;
	}
	options.CreateFlags = env->GetIntField(obj, createFlagsID);
	
	jfieldID engCreateFlagsID = env->GetFieldID(cls, "engCreateFlags", "I");
	if (engCreateFlagsID == 0) {
		throwHRESULT(env, E_FAIL);
		return;
	}
	options.EngCreateFlags = env->GetIntField(obj, engCreateFlagsID);
	
	jfieldID verifierFlagsID = env->GetFieldID(cls, "verifierFlags", "I");
	if (verifierFlagsID == 0) {
		throwHRESULT(env, E_FAIL);
		return;
	}
	options.VerifierFlags = env->GetIntField(obj, verifierFlagsID);
}
