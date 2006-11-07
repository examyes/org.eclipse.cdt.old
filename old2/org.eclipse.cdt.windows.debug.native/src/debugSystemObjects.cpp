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

#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_IDebugSystemObjects_## name
#define JNISTDMETHOD(name, ...) extern "C" JNIEXPORT jint JNINAME(name)(JNIEnv * env, jobject obj, __VA_ARGS__ ) { \
	try { IDebugSystemObjects4 * systemObjects = getObject(env, obj);
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

static IDebugSystemObjects4 * getObject(JNIEnv * env, jobject obj) {
	IDebugSystemObjects4 * systemObjects = (IDebugSystemObjects4 *)env->GetLongField(obj, getPID(env, obj));
	checkNull(env, systemObjects);
	return systemObjects;
}

void setObject(JNIEnv * env, jobject obj, IDebugSystemObjects4 * systemObjects) {
	env->SetLongField(obj, getPID(env, obj), (jlong)systemObjects);
}

// IDebugSystemObjects.

//	public native int getEventThread(DebugInt id);
//	public native int getEventProcess(DebugInt id);
//	public native int getCurrentThreadId(DebugInt id);
//	public native int setCurrentThreadId(int id);
//	public native int getCurrentProcessId(DebugInt id);
//	public native int setCurrentProcessId(int id);
//	public native int getNumberThreads(DebugInt number);
//	public native int getTotalNumberThreads(DebugInt total,
//			DebugInt largestProcess);
//	public native int getThreadIdsByIndex(int start,
//			int[] ids, int[] sysIds);
//	public native int getThreadIdByProcessor(int processor, DebugInt id);
//	public native int getCurrentThreadDataOffset(DebugLong offset);
//	public native int getThreadIdByDataOffset(long offset, DebugInt id);
//	public native int getCurrentThreadTeb(DebugLong offset);
//	public native int getThreadIdByTeb(long offset, DebugInt id);
//	public native int getCurrentThreadSystemId(DebugInt sysId);
//	public native int getThreadIdBySystemId(int sysId, DebugInt id);
//	public native int getCurrentThreadHandle(DebugLong handle);
//	public native int getThreadIdByHandle(long handle, DebugInt id);
//	public native int getNumberProcesses(DebugInt number);
//	public native int getProcessIdsByIndex(int start,
//			int[] ids, int[] sysIds);
//	public native int getCurrentProcessDataOffset(DebugLong offset);
//	public native int getProcessIdByDataOffset(long offset, DebugInt id);
//	public native int getCurrentProcessPeb(DebugLong offset);
//	public native int getProcessIdByPeb(int offset, DebugInt id);
//	public native int getCurrentProcessSystemId(DebugInt sysId);
//	public native int getProcessIdBySystemId(int sysId, DebugInt id);
//	public native int getCurrentProcessHandle(DebugLong handle);
//	public native int getProcessIdByHandle(long handle, DebugInt id);
//	public native int getCurrentProcessExecutableName(DebugString exe);

// IDebugSystemObjects2.

//	public native int getCurrentProcessUpTime(DebugInt upTime);
//	public native int getImplicitThreadDataOffset(DebugLong offset);
//	public native int setImplicitThreadDataOffset(DebugLong offset);
//	public native int getImplicitProcessDataOffset(DebugLong offset);
//	public native int setImplicitProcessDataOffset(long offset);

// IDebugSystemObjects3.

//	public native int getEventSystem(DebugInt id);
//	public native int getCurrentSystemId(DebugInt id);
//	public native int setCurrentSystemId(int id);
//	public native int getNumberSystems(DebugInt number);
//	public native int getSystemIdsByIndex(int start, int[] ids);
//	public native int getTotalNumberThreadsAndProcesses(
//			DebugInt totalThreads, DebugInt totalProcesses,
//			DebugInt largestProcessThreads,
//			DebugInt largestSystemThreads,
//			DebugInt largestSystemProcesses);
//	public native int getCurrentSystemServer(DebugLong server);
//	public native int getSystemByServer(long server, DebugInt id);
//	public native int getCurrentSystemServerName(DebugString name);
