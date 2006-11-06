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

#include "debugRegisters.h"
#include "util.h"

#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_IDebugRegisters_## name
#define JNISTDMETHOD(name, ...) extern "C" JNIEXPORT jint JNINAME(name)(JNIEnv * env, jobject obj, __VA_ARGS__ ) { \
	try { IDebugRegisters2 * registers = getObject(env, obj);
#define JNISTDEND } catch (jobject e) { env->Throw((jthrowable)e); return E_FAIL; } }

static jfieldID pID = NULL;

static jfieldID getPID(JNIEnv * env, jobject obj) {
	if (pID == NULL) {
		jclass cls = env->GetObjectClass(obj);
		pID = env->GetFieldID(cls, "p", "J");
		checkNull(env, pID);
	}
	return pID;
}

static IDebugRegisters2 * getObject(JNIEnv * env, jobject obj) {
	IDebugRegisters2 * control = (IDebugRegisters2 *)env->GetLongField(obj, getPID(env, obj));
	checkNull(env, control);
	return control;
}

void setObject(JNIEnv * env, jobject obj, IDebugRegisters2 * registers) {
	env->SetLongField(obj, getPID(env, obj), (jlong)registers);
}

// IDebugRegisters.

//	public native int getNumberRegisters(DebugInt number);
//	public native int getDescription(int register, DebugString name,
//			DebugRegisterDescription desc);
//	public native int getIndexByName(String name, DebugInt index);
//	public native int getValue(int register, DebugValue value);
//	public native int setValue(int register, DebugValue value);
//	public native int getValues(int[] indices, int start, DebugValue[] values);
//	public native int setValues(int[] indices, int start, DebugValue[] values);
//	public native int outputRegisters(int outputControl, int flags);

//	public native int getInstructionOffset(DebugLong offset);
JNISTDMETHOD(getInstructionOffset, jobject offset)
	ULONG64 _offset;
	HRESULT hr = registers->GetInstructionOffset(&_offset);
	if (FAILED(hr))
		return hr;
	setObject(env, offset, (jlong)_offset);
	return hr;
JNISTDEND

//	public native int getStackOffset(DebugLong offset);
//	public native int getFrameOffset(DebugLong offset);

    // IDebugRegisters2.

//	public native int getNumberPseudoRegisters(DebugInt number);
//	public native int getPseudoDescription(int register, DebugString name,
//			DebugLong typeModule, DebugInt typeId);
//	public native int getPseudoIndexByName(String name, DebugInt index);
//	public native int getPseudoValues(int source, int[] indices, int start,
//			DebugValue[] values);
//	public native int setPseudoValues(int source, int[] indices, int start,
//			DebugValue[] values);
//	public native int getValues2(int source, int[] indices, int start,
//			DebugValue[] values);
//	public native int setValues2(int source, int[] indices, int start,
//			DebugValue[] values);
//	public native int outputRegisters2(int outputControl, int source, int flags);

//	public native int getInstructionOffset2(int source, DebugLong offset);
JNISTDMETHOD(getInstructionOffset2, jint source, jobject offset);
	ULONG64 _offset;
	HRESULT hr = registers->GetInstructionOffset2(source, &_offset);
	if (FAILED(hr))
		return hr;
	setObject(env, offset, (jlong)_offset);
	return hr;
JNISTDEND

//	public native int getStackOffset2(int source, DebugLong offset);
//	public native int getFrameOffset2(int source, DebugLong offset);
