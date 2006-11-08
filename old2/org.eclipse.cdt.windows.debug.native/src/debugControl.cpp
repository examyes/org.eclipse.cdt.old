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

#include "debugControl.h"
#include "util.h"
#include "debugBreakpoint.h"

// Class IDebugControl

#define JNINAME(name) Java_org_eclipse_cdt_windows_debug_core_IDebugControl_## name
#define JNISTDMETHOD(name, ...) extern "C" JNIEXPORT jint JNINAME(name)(JNIEnv * env, jobject obj, __VA_ARGS__ ) { \
	try { IDebugControl4 * control = getObject(env, obj);
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

static IDebugControl4 * getObject(JNIEnv * env, jobject obj) {
	IDebugControl4 * control = (IDebugControl4 *)env->GetLongField(obj, getPID(env, obj));
	checkNull(env, control);
	return control;
}

void setObject(JNIEnv * env, jobject obj, IDebugControl4 * debugControl) {
	env->SetLongField(obj, getPID(env, obj), (jlong)debugControl);
}

// IDebugControl.

//	public native int getInterrupt();
//	public native int setInterrupt(int flags);
//	public native int getInterruptTimeout(DebugInt seconds);
//	public native int setInterruptTimeout(int seconds);
//	public native int getLogFile(DebugString file, DebugBoolean append);
//	public native int openLogFile(String file, boolean append);
//	public native int closeLogFile();
//	public native int getLogMask(DebugInt mask);
//	public native int setLogMask(int mask);
//	public native int input(DebugString buffer);
//	public native int returnInput(String buffer);
//	public native int output(int mask, String buffer);
//	public native int controlledOutput(int outputControl, int mask, String buffer);
//	public native int outputPrompt(int outputControl, String buffer);
//	public native int getPromptText(DebugString text);
//	public native int outputCurrentState(int outputControl, int flags);
//	public native int outputVersionInformation(int outputControl);
//	public native int getNotifyEventHandle(DebugLong handle);
//	public native int setNotifyEventHandle(long handle);
//	public native int assemble(long offset, String instr, DebugLong endOffset);
//	public native int disassemble(long offset, int flags, DebugString buffer, DebugLong endOffset);
//	public native int getDisassembleEffectiveOffset(DebugLong offset);
//	public native int outputDisassembly(int outputControl, long offset,
//			int flags, DebugLong endOffset);
//    public native int outputDisassemblyLines(int outputControl,
//    		int previousLines, long offset, int flags,
//    		DebugInt offsetLine, DebugLong startOffset,
//    		DebugLong endoffset, long[] lineOffsets);
//    public native int getNearInstruction(long offset, int delta,
//    		DebugLong nearOffset);

//	public native int getStrackTrace(long frameOffset, long stackOffset,
//			long instructionOffset, DebugStackFrame[] frames);
JNISTDMETHOD(getStrackTrace, jlong frameOffset, jlong stackOffset, jlong instructionOffset,
		jobjectArray frames, jobject framesFilled)
	jsize framesSize = env->GetArrayLength(frames);
	DEBUG_STACK_FRAME * _frames = new DEBUG_STACK_FRAME[framesSize];
	ULONG _framesFilled;
	HRESULT hr = control->GetStackTrace(frameOffset, stackOffset, instructionOffset,
			_frames, framesSize, &_framesFilled);
	if (!FAILED(hr)) {
		for (int i = 0; i < _framesFilled; ++i) {
			env->SetObjectArrayElement(frames, i, createObject(env, _frames[i]));
		}
	}
	delete[] _frames;
	if (framesFilled != NULL)
		setObject(env, framesFilled, (jint)_framesFilled);
	return hr;
JNISTDEND

//    public native int getReturnOffset(DebugLong offset);
//    public native int outputStackTrace(int outputControl,
//    		DebugStackFrame[] frames, int flags);
//    public native int getDebuggeeType(DebugInt cls, DebugInt qualifier);
//    public native int getActualProcessorType(DebugInt type);
//    public native int getExecutingProcessorType(DebugInt type);
//    public native int getNumberPossibleExecutingProcessorTypes(DebugInt number);
//    public native int getPossibleExecutingProcessorTypes(int start, int[] types);
//    public native int getNumberProcessors(DebugInt number);
//    public native int getSystemVersion(DebugInt platformId,
//    		DebugInt major, DebugInt minor,
//    		DebugString servicePackString, int servicePackStringSize,
//    		DebugBoolean servicePackStringUsed,
//    		DebugInt servicePackNumber,
//    		DebugString buildString, int buildStringSize,
//    		DebugBoolean buildStringUsed);
//    public native int getPageSize(DebugInt size);
//    public native int isPointer64Bit();
//    public native int readBugCheckData(DebugInt code,
//    		DebugLong arg1, DebugLong arg2, DebugLong arg3, DebugLong arg4);
//    public native int getNumberSupportedProcessorTypes(DebugInt number);
//    public native int getSupportedProcessorTypes(int start, int[] types);
//    public native int getProcessorTypeNames(int type,
//    		DebugString fullName, DebugString abbrevName);
//    public native int getEffectiveProcessorType(DebugInt type);
//    public native int setEffectiveProcessorType(int type);
//    public native int getExecutionStatus(DebugInt status);

//    public native int setExecutionStatus(int status);
JNISTDMETHOD(setExecutionStatus, jint status)
	return control->SetExecutionStatus(status);
JNISTDEND

//    public native int getCodeLevel(DebugInt level);
JNISTDMETHOD(getCodeLevel, jobject level)
	ULONG _level;
	HRESULT hr = control->GetCodeLevel(&_level);
	if (FAILED(hr))
		return hr;
	setObject(env, level, (jint)_level);
JNISTDEND

//    public native int setCodeLevel(int level);
JNISTDMETHOD(setCodeLevel, jint level)
	return control->SetCodeLevel(level);
JNISTDEND

//    public native int getEngineOptions(DebugInt options);
//    public native int addEngineOptions(int options);
//    public native int removeEngineOptions(int options);
//    public native int setEngineOptions(int options);
//    public native int getSystemErrorControl(DebugInt outputLevel, DebugInt breakLevel);
//    public native int setSystemErrorControl(int outputLevel, int breakLevel);
//    public native int getTextMacro(int slot, DebugString macro);
//    public native int setTextMacro(int slot, String macro);
//    public native int getRadix(DebugInt radix);
//    public native int setRadix(int radix);
//    public native int evaluate(String expression, int desiredType,
//    		DebugValue value, DebugInt remainderIndex);
//    public native int coerceValue(DebugValue in, int outType, DebugValue out);
//    public native int coerceValues(DebugValue[] in, int[] outTypes, DebugValue[] out);
//    public native int execute(int outputControl, String command, int flags);
//    public native int executeCommandFile(int outputControl, String commandFile, int flags);
//    public native int getNumberBreakpoints(DebugInt number);
//    public native int getBreakpointByIndex(int index, IDebugBreakpoint bp);
//    public native int getBreakpointById(int id, IDebugBreakpoint bp);
//    public native int getBreakpointParameters(int[] ids, int start,
//    		DebugBreakpointParameters[] params);

//	public native int addBreakpoint(int type, int desiredId, IDebugBreakpoint bp);
JNISTDMETHOD(addBreakpoint, jint type, jint desiredId, jobject bp)
	IDebugBreakpoint2 * bpcom;
	HRESULT hr = control->AddBreakpoint2(type, desiredId, &bpcom);
	if (FAILED(hr))
		return hr;

	setObject(env, bp, bpcom);
	return hr;
JNISTDEND

//    public native int removeBreakpoint(IDebugBreakpoint bp);
//    public native int addExtension(String path, int flags, DebugLong handle);
//    public native int removeExtension(long handle);
//    public native int getExtensionByPath(String path, DebugLong handle);
//    public native int callExtension(long handle, String function, String arguments);
//    public native int getNumberEventFilters(DebugInt specificEvents,
//    		DebugInt specificExceptions, DebugInt arbitraryExceptions);
//    public native int getEventFilterText(int index, DebugString text);
//    public native int getEventFilterCommand(int index, DebugString command);
//    public native int setEventFilterCommand(int index, String command);
//    public native int getSpecificFilterParameters(int start, DebugSpecificFilterParameters[] params);
//    public native int setSpecificFilterParameters(int start, DebugSpecificFilterParameters[] params);
//    public native int getSpecificFilterArgument(int index, DebugString argument);
//    public native int setSpecificFilterArgument(int index, String argument);
//    public native int getExceptionFilterParameters(int[] codes, int start, DebugExceptionFilterParameters[] params);
//    public native int setExceptionFilterParameters(DebugExceptionFilterParameters[] params);
//    public native int getExceptionFilterSecondCommand(int index, DebugString command);
//    public native int setExceptionFilterSecondCommand(int index, String command);

//	public native int waitForEvent(int flags, int timeout);
JNISTDMETHOD(waitForEvent, jint flags, jint timeout)
	return control->WaitForEvent(flags, timeout);
JNISTDEND

//    public native int getLastEventInformation(DebugInt type,
//    		DebugInt processId, DebugInt threadId,
//    		DebugValue extraInformation, DebugString description);
//
//     IDebugControl2.
//
//    public native int getCurrentTimeDate(DebugInt timeDate);
//    public native int getCurrentSystemUpTime(DebugInt upTime);
//    public native int getDumpFormatFlags(DebugInt formatFlags);
//    public native int getNumberTextReplacements(DebugInt numRepl);
//    public native int getTextReplacement(String srcText, int index,
//    		DebugString src, DebugString dst);
//    public native int setTextReplacement(String src, String dst);
//    public native int removeTextReplacements();
//    public native int outputTextReplacements(int outputControl, int flags);
//
//     IDebugControl3.
//
//    public native int getAssemblyOptions(DebugInt options);
//    public native int addAssemblyOptions(int options);
//    public native int removeAssemblyOptions(int options);
//    public native int setAssemblyOptions(int options);
//    public native int getExpressionSyntax(DebugInt flags);
//    public native int setExpressionSyntax(int flags);
//    public native int setExpressionSyntaxByName(String abbrevName);
//    public native int getNumberExpressionSyntaxes(DebugInt number);
//    public native int getExpressionSyntaxNames(int index,
//    		DebugString fullName, DebugString abbrevName);
//    public native int getNumberEvents(DebugInt events);
//    public native int getEventIndexDescription(int index, int which,
//    		DebugString desc);
//    public native int getCurrentEventIndex(DebugInt index);
//    public native int setNextEventIndex(int relation, int value,
//    		DebugInt nextIndex);
//
    // IDebugControl4.
//
//    public native int getLogFile2(DebugString file, DebugInt flags);
//    public native int openLogFile2(String file, int flags);
//    public native int getSystemVersionValues(DebugInt platformId,
//    		DebugInt win32Major, DebugInt win32Minor,
//    		DebugInt kdMajor, DebugInt kdMinor);
//    public native int getSystemVersionString(int which, DebugString string);
//    public native int getManagedStatus(DebugInt flags, int whichString,
//    		DebugString string, int stringSize, DebugBoolean stringNeeded);
//    public native int resetManagedStatus(int flags);
