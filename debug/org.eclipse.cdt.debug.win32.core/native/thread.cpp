/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 ******************************************************************************/
#include <windows.h>
#include <dbghelp.h>
#include <stdio.h>
#include <jni.h>

#define NATIVE(type, name) extern "C" JNIEXPORT type JNICALL Java_org_eclipse_cdt_debug_win32_core_cdi_WinDbgThread_##name

static jfieldID processHandleID;
static jfieldID threadHandleID;
static jmethodID createStackFrameID_name;
static jmethodID createStackFrameID_address;

NATIVE(void, initNative)(JNIEnv * env, jclass cls)
{
	processHandleID = env->GetFieldID(cls, "processHandle", "J");
	threadHandleID = env->GetFieldID(cls, "threadHandle", "J");
	createStackFrameID_name = env->GetMethodID(cls, "createStackFrame",
		"(Ljava/lang/String;Ljava/lang/String;IJJ)V");
	createStackFrameID_address = env->GetMethodID(cls, "createStackFrame", "(J)V");
}

NATIVE(void, populateStackFrames)(JNIEnv * env, jobject obj)
{
	HANDLE processHandle = (HANDLE)env->GetLongField(obj, processHandleID);
	HANDLE threadHandle = (HANDLE)env->GetLongField(obj, threadHandleID);
	CONTEXT context;
	ZeroMemory(&context, sizeof(context));
	context.ContextFlags = CONTEXT_FULL;
	if (!GetThreadContext(threadHandle, &context)) {
		printf("Failed to get context %d\n", threadHandle);
		fflush(stdout);
	}
	
	long bpAddress = context.Eip - 1;
	
	STACKFRAME64 frame;
	ZeroMemory(&frame, sizeof(frame));
	frame.AddrPC.Offset = bpAddress;
	frame.AddrPC.Mode = AddrModeFlat;
	frame.AddrFrame.Offset = context.Ebp;
	frame.AddrFrame.Mode = AddrModeFlat;
	while (StackWalk64(
		IMAGE_FILE_MACHINE_I386,
		processHandle,
		threadHandle,
		&frame,
		NULL,
		0,
		SymFunctionTableAccess64,
		SymGetModuleBase64,
		NULL))
	{
		BYTE buffer[1024];
		SYMBOL_INFO * symbol = (SYMBOL_INFO *)buffer;
		ZeroMemory(buffer, sizeof(buffer));
		symbol->SizeOfStruct = sizeof(SYMBOL_INFO);
		symbol->MaxNameLen = sizeof(buffer) - sizeof(SYMBOL_INFO) + 1;
		
		DWORD64 offset;
		if (SymFromAddr(processHandle, frame.AddrPC.Offset, &offset, symbol)) {
			jstring function = env->NewStringUTF(symbol->Name);
			jstring file = NULL;
			jint lineno = 0;
			
			IMAGEHLP_LINE64 line;
			ZeroMemory(&line, sizeof(line));
			line.SizeOfStruct = sizeof(line);
			DWORD displacement;
			if (SymGetLineFromAddr64(processHandle, frame.AddrPC.Offset, &displacement, &line)) {
				file = env->NewStringUTF(line.FileName);
				lineno = line.LineNumber;
			}

			env->CallVoidMethod(obj, createStackFrameID_name,
				file, function, lineno,
				(jlong)frame.AddrPC.Offset,
				(jlong)frame.AddrFrame.Offset);
		} else {
			env->CallVoidMethod(obj, createStackFrameID_address, (jlong)bpAddress);
		}
	}
}
