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

static jmethodID addVariableID;

#define NATIVE(type, name) extern "C" JNIEXPORT type JNICALL Java_org_eclipse_cdt_debug_win32_core_cdi_WinDbgStackFrame_##name

NATIVE(void, initNative)(JNIEnv * env, jclass cls)
{
	addVariableID = env->GetMethodID(cls, "addVariable", "(ZLjava/lang/String;I)V");
}

static JNIEnv * env;
static jobject obj;
static DWORD64 framePtr;
static HANDLE process;

// TODO fix the user context to point back at our object to get this info or something
// Pass in the pointer to the stack
BOOL CALLBACK enumSymbolsProc(SYMBOL_INFO * symbol, ULONG symbolSize, PVOID userContext)
{
	DWORD64 addr = framePtr + symbol->Address;
	DWORD data;
	DWORD nread;
	if (!ReadProcessMemory(process, (void *)addr, &data, sizeof(data), &nread)) {
		printf("Failed to read memory %x\n", (DWORD)addr);
		return TRUE;
	}

	env->CallVoidMethod(obj, addVariableID,
		(symbol->Flags & IMAGEHLP_SYMBOL_INFO_PARAMETER) ? JNI_TRUE : JNI_FALSE,
		env->NewStringUTF(symbol->Name),
		data);
		
	return TRUE;
}

NATIVE(void, npopulateVariables)(JNIEnv * env, jobject obj, jlong handle, jlong pc, jlong frame)
{
	::env = env;
	::obj = obj;
	::process = (HANDLE)handle;
	
	IMAGEHLP_STACK_FRAME sf;
	ZeroMemory(&sf, sizeof(sf));
	sf.InstructionOffset = pc;
	if (SymSetContext(process, &sf, 0)) {
		framePtr = frame;
		if (!SymEnumSymbols(process, 0, NULL, enumSymbolsProc, NULL)) {
			printf("    (no symbols available %x)\n", GetLastError());
		}
	} else {
			printf("    (no context info)\n");
	}
	
	fflush(stdout);
}
