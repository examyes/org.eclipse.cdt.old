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

#include "debugEventCallbacks.h"
#include "debugBreakpoint.h"

// Class IDebugEventCallbacks
#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_IDebugEventCallbacks_## name

static JavaVM * vm;

#define VMENV(env) JNIEnv * env; \
	if (vm->GetEnv((void **)&env, JNI_VERSION_1_4) != JNI_OK) { \
		fprintf(stderr, "DebugEventCallbacks: Failed to get env\n"); \
		return E_FAIL; \
	}

static jfieldID pID;

static jmethodID getInterestMaskID;
static jmethodID createProcessID;
static jmethodID exitProcessID;
static jmethodID createThreadID;
static jmethodID exitThreadID;
static jmethodID breakpointID;

DebugEventCallbacks * DebugEventCallbacks::getObject(JNIEnv * env, jobject obj) {
	jlong p = env->GetLongField(obj, pID);
	return (DebugEventCallbacks *)p;
}

extern "C" JNIEXPORT jint JNINAME(init)(JNIEnv * env, jobject obj) {
	if (env->GetJavaVM(&vm))
		return E_FAIL;
	
	jclass cls = env->GetObjectClass(obj);
	if (cls == NULL)
		return E_FAIL;
	
	pID = env->GetFieldID(cls, "p", "J");
	if (pID == 0)
		return E_FAIL;
	
	getInterestMaskID = env->GetMethodID(cls, "getInterestMask", "()I");
	if (getInterestMaskID == 0)
		return E_FAIL;
	
	createProcessID = env->GetMethodID(cls, "createProcess", "(JJJILjava/lang/String;Ljava/lang/String;IIJJJ)I");
	if (createProcessID == 0)
		return E_FAIL;
	
	exitProcessID = env->GetMethodID(cls, "exitProcess", "(I)I");
	if (exitProcessID == 0)
		return E_FAIL;

	createThreadID = env->GetMethodID(cls, "createThread", "(JJJ)I");
	if (createThreadID == 0)
		return E_FAIL;
		
	exitThreadID = env->GetMethodID(cls, "exitThread", "(I)I");
	if (exitThreadID == 0)
		return E_FAIL;

	breakpointID = env->GetMethodID(cls, "breakpoint", "(Lorg/eclipse/cdt/windows/debug/core/IDebugBreakpoint;)I");
	if (breakpointID == 0)
		return E_FAIL;
	
	env->SetLongField(obj, pID, (jlong)new DebugEventCallbacks(env, obj));
	
	return S_OK; 
}

DebugEventCallbacks::DebugEventCallbacks(JNIEnv * env, jobject obj)
{
	ref = env->NewGlobalRef(obj);
}

DebugEventCallbacks::~DebugEventCallbacks()
{
	// env->DeleteGlobalRef(obj);
}

ULONG __stdcall DebugEventCallbacks::AddRef() {
	return 1;
}

ULONG __stdcall DebugEventCallbacks::Release() {
	return 1;
}

HRESULT __stdcall DebugEventCallbacks::GetInterestMask(ULONG * Mask) {
	VMENV(env)
	
	*Mask = env->CallIntMethod(ref, getInterestMaskID);
	return S_OK;
}

HRESULT __stdcall DebugEventCallbacks::CreateProcess(
		ULONG64 ImageFileHandle,
		ULONG64 Handle,
		ULONG64 BaseOffset,
		ULONG ModuleSize,
		PCWSTR ModuleName,
		PCWSTR ImageName,
		ULONG CheckSum,
		ULONG TimeDateStamp,
		ULONG64 InitialThreadHandle,
		ULONG64 ThreadDataOffset,
		ULONG64 StartOffset) {
	VMENV(env)

	jstring moduleName = env->NewString((const jchar *)ModuleName, wcslen(ModuleName));
	jstring imageName = env->NewString((const jchar *)ImageName, wcslen(ImageName));
	
	return env->CallIntMethod(ref, createProcessID, ImageFileHandle, Handle,
		BaseOffset, ModuleSize, moduleName, imageName, CheckSum, TimeDateStamp,
		InitialThreadHandle, ThreadDataOffset, StartOffset); 
}

HRESULT __stdcall DebugEventCallbacks::ExitProcess(ULONG ExitCode) {
	VMENV(env)
	
	return env->CallIntMethod(ref, exitProcessID, ExitCode);
}

HRESULT __stdcall DebugEventCallbacks::CreateThread(
		ULONG64 Handle,
		ULONG64 DataOffset,
		ULONG64 StartOffset) {
	VMENV(env)

	return env->CallIntMethod(ref, createThreadID, Handle, DataOffset, StartOffset);
}

HRESULT __stdcall DebugEventCallbacks::ExitThread(ULONG ExitCode) {
	VMENV(env)
	
	return env->CallIntMethod(ref, exitThreadID, ExitCode);
}

HRESULT __stdcall DebugEventCallbacks::Breakpoint(IDebugBreakpoint2 * Bp) {
	VMENV(env)
	
	jobject bpobj = createBreakpoint(env, Bp);
	return env->CallIntMethod(ref, breakpointID, bpobj);
}
