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

package org.eclipse.cdt.windows.debug.core;


/**
 * @author Doug Schaefer
 *
 */
public class IDebugControl {
	
	@SuppressWarnings("unused")
	private long p;
	
	public static final int DEBUG_STATUS_NO_CHANGE				=  0;
	public static final int DEBUG_STATUS_GO						=  1;
	public static final int DEBUG_STATUS_GO_HANDLED				=  2;
	public static final int DEBUG_STATUS_GO_NOT_HANDLED			=  3;
	public static final int DEBUG_STATUS_STEP_OVER				=  4;
	public static final int DEBUG_STATUS_STEP_INTO				=  5;
	public static final int DEBUG_STATUS_BREAK					=  6;
	public static final int DEBUG_STATUS_NO_DEBUGGEE			=  7;
	public static final int DEBUG_STATUS_STEP_BRANCH			=  8;
	public static final int DEBUG_STATUS_IGNORE_EVENT			=  9;
	public static final int DEBUG_STATUS_RESTART_REQUESTED		= 10;
	public static final int DEBUG_STATUS_REVERSE_GO				= 11;
	public static final int DEBUG_STATUS_REVERSE_STEP_BRANCH	= 12;
	public static final int DEBUG_STATUS_REVERSE_STEP_OVER		= 13;
	public static final int DEBUG_STATUS_REVERSE_STEP_INTO		= 14;

	public static final int DEBUG_STATUS_MASK					= 0xf;

	public static final long DEBUG_STATUS_INSIDE_WAIT	= 0x100000000L;
	public static final long DEBUG_STATUS_WAIT_TIMEOUT	= 0x200000000L;

	public static final int DEBUG_OUTCTL_THIS_CLIENT		= 0x00000000;
	public static final int DEBUG_OUTCTL_ALL_CLIENTS		= 0x00000001;
	public static final int DEBUG_OUTCTL_ALL_OTHER_CLIENTS	= 0x00000002;
	public static final int DEBUG_OUTCTL_IGNORE				= 0x00000003;
	public static final int DEBUG_OUTCTL_LOG_ONLY			= 0x00000004;
	public static final int DEBUG_OUTCTL_SEND_MASK			= 0x00000007;
	public static final int DEBUG_OUTCTL_NOT_LOGGED			= 0x00000008;
	public static final int DEBUG_OUTCTL_OVERRIDE_MASK		= 0x00000010;
	public static final int DEBUG_OUTCTL_DML				= 0x00000020;

	public static final int DEBUG_OUTCTL_AMBIENT_DML		= 0xfffffffe;
	public static final int DEBUG_OUTCTL_AMBIENT_TEXT		= 0xffffffff;
	public static final int DEBUG_OUTCTL_AMBIENT			= DEBUG_OUTCTL_AMBIENT_TEXT;

	public static final int DEBUG_INTERRUPT_ACTIVE	= 0;
	public static final int DEBUG_INTERRUPT_PASSIVE	= 1;
	public static final int DEBUG_INTERRUPT_EXIT	= 2;

	public static final int DEBUG_CURRENT_DEFAULT		= 0x0000000f;
	public static final int DEBUG_CURRENT_SYMBOL		= 0x00000001;
	public static final int DEBUG_CURRENT_DISASM		= 0x00000002;
	public static final int DEBUG_CURRENT_REGISTERS		= 0x00000004;
	public static final int DEBUG_CURRENT_SOURCE_LINE	= 0x00000008;

	public static final int DEBUG_DISASM_EFFECTIVE_ADDRESS	= 0x00000001;
	public static final int DEBUG_DISASM_MATCHING_SYMBOLS	= 0x00000002;
	public static final int DEBUG_DISASM_SOURCE_LINE_NUMBER	= 0x00000004;
	public static final int DEBUG_DISASM_SOURCE_FILE_NAME	= 0x00000008;

	public static final int DEBUG_LEVEL_SOURCE		= 0;
	public static final int DEBUG_LEVEL_ASSEMBLY	= 1;

	public static final int DEBUG_ENGOPT_IGNORE_DBGHELP_VERSION		= 0x00000001;
	public static final int DEBUG_ENGOPT_IGNORE_EXTENSION_VERSIONS	= 0x00000002;
	public static final int DEBUG_ENGOPT_ALLOW_NETWORK_PATHS		= 0x00000004;
	public static final int DEBUG_ENGOPT_DISALLOW_NETWORK_PATHS		= 0x00000008;
	public static final int DEBUG_ENGOPT_NETWORK_PATHS				= (0x00000004 | 0x00000008);
	public static final int DEBUG_ENGOPT_IGNORE_LOADER_EXCEPTIONS	= 0x00000010;
	public static final int DEBUG_ENGOPT_INITIAL_BREAK				= 0x00000020;
	public static final int DEBUG_ENGOPT_INITIAL_MODULE_BREAK		= 0x00000040;
	public static final int DEBUG_ENGOPT_FINAL_BREAK				= 0x00000080;
	public static final int DEBUG_ENGOPT_NO_EXECUTE_REPEAT			= 0x00000100;
	public static final int DEBUG_ENGOPT_FAIL_INCOMPLETE_INFORMATION	= 0x00000200;
	public static final int DEBUG_ENGOPT_ALLOW_READ_ONLY_BREAKPOINTS	= 0x00000400;
	public static final int DEBUG_ENGOPT_SYNCHRONIZE_BREAKPOINTS		= 0x00000800;
	public static final int DEBUG_ENGOPT_DISALLOW_SHELL_COMMANDS		= 0x00001000;
	public static final int DEBUG_ENGOPT_KD_QUIET_MODE					= 0x00002000;
	public static final int DEBUG_ENGOPT_DISABLE_MANAGED_SUPPORT		= 0x00004000;
	public static final int DEBUG_ENGOPT_DISABLE_MODULE_SYMBOL_LOAD		= 0x00008000;
	public static final int DEBUG_ENGOPT_DISABLE_EXECUTION_COMMANDS		= 0x00010000;
	public static final int DEBUG_ENGOPT_DISALLOW_IMAGE_FILE_MAPPING	= 0x00020000;
	public static final int DEBUG_ENGOPT_PREFER_DML						= 0x00040000;
	public static final int DEBUG_ENGOPT_ALL							= 0x0007FFFF;

	public static final int DEBUG_ANY_ID = 0xffffffff;

	public static final int DEBUG_STACK_ARGUMENTS				= 0x00000001;
	public static final int DEBUG_STACK_FUNCTION_INFO			= 0x00000002;
	public static final int DEBUG_STACK_SOURCE_LINE				= 0x00000004;
	public static final int DEBUG_STACK_FRAME_ADDRESSES			= 0x00000008;
	public static final int DEBUG_STACK_COLUMN_NAMES			= 0x00000010;
	public static final int DEBUG_STACK_NONVOLATILE_REGISTERS	= 0x00000020;
	public static final int DEBUG_STACK_FRAME_NUMBERS			= 0x00000040;
	public static final int DEBUG_STACK_PARAMETERS				= 0x00000080;
	public static final int DEBUG_STACK_FRAME_ADDRESSES_RA_ONLY	= 0x00000100;
	public static final int DEBUG_STACK_FRAME_MEMORY_USAGE		= 0x00000200;
	public static final int DEBUG_STACK_PARAMETERS_NEWLINE		= 0x00000400;
	public static final int DEBUG_STACK_DML						= 0x00000800;

	public static final int DEBUG_CLASS_UNINITIALIZED 	= 0;
	public static final int DEBUG_CLASS_KERNEL			= 1;
	public static final int DEBUG_CLASS_USER_WINDOWS	= 2;
	public static final int DEBUG_CLASS_IMAGE_FILE		= 3;

	public static final int DEBUG_DUMP_SMALL		= 1024;
	public static final int DEBUG_DUMP_DEFAULT		= 1025;
	public static final int DEBUG_DUMP_FULL			= 1026;
	public static final int DEBUG_DUMP_IMAGE_FILE	= 1027;
	public static final int DEBUG_DUMP_TRACE_LOG	= 1028;
	public static final int DEBUG_DUMP_WINDOWS_CE	= 1029;

	public static final int DEBUG_KERNEL_CONNECTION		= 0;
	public static final int DEBUG_KERNEL_LOCAL			= 1;
	public static final int DEBUG_KERNEL_EXDI_DRIVER	= 2;

	public static final int DEBUG_KERNEL_SMALL_DUMP	= DEBUG_DUMP_SMALL;
	public static final int DEBUG_KERNEL_DUMP		= DEBUG_DUMP_DEFAULT;
	public static final int DEBUG_KERNEL_FULL_DUMP	= DEBUG_DUMP_FULL;

	public static final int DEBUG_KERNEL_TRACE_LOG	= DEBUG_DUMP_TRACE_LOG;

	public static final int DEBUG_USER_WINDOWS_PROCESS	= 0;
	public static final int DEBUG_USER_WINDOWS_PROCESS_SERVER	= 1;
	public static final int DEBUG_USER_WINDOWS_IDNA				= 2;
	public static final int DEBUG_USER_WINDOWS_SMALL_DUMP		= DEBUG_DUMP_SMALL;
	public static final int DEBUG_USER_WINDOWS_DUMP				= DEBUG_DUMP_DEFAULT;
	public static final int DEBUG_USER_WINDOWS_DUMP_WINDOWS_CE	= DEBUG_DUMP_WINDOWS_CE;

	public static final int DEBUG_EXTENSION_AT_ENGINE = 0x00000000;

	public static final int DEBUG_EXECUTE_DEFAULT		= 0x00000000;
	public static final int DEBUG_EXECUTE_ECHO			= 0x00000001;
	public static final int DEBUG_EXECUTE_NOT_LOGGED	= 0x00000002;
	public static final int DEBUG_EXECUTE_NO_REPEAT		= 0x00000004;

	public static final int DEBUG_FILTER_CREATE_THREAD			= 0x00000000;
	public static final int DEBUG_FILTER_EXIT_THREAD			= 0x00000001;
	public static final int DEBUG_FILTER_CREATE_PROCESS			= 0x00000002;
	public static final int DEBUG_FILTER_EXIT_PROCESS			= 0x00000003;
	public static final int DEBUG_FILTER_LOAD_MODULE			= 0x00000004;
	public static final int DEBUG_FILTER_UNLOAD_MODULE			= 0x00000005;
	public static final int DEBUG_FILTER_SYSTEM_ERROR			= 0x00000006;
	public static final int DEBUG_FILTER_INITIAL_BREAKPOINT		= 0x00000007;
	public static final int DEBUG_FILTER_INITIAL_MODULE_LOAD	= 0x00000008;
	public static final int DEBUG_FILTER_DEBUGGEE_OUTPUT		= 0x00000009;

	public static final int DEBUG_FILTER_BREAK					= 0x00000000;
	public static final int DEBUG_FILTER_SECOND_CHANCE_BREAK	= 0x00000001;
	public static final int DEBUG_FILTER_OUTPUT					= 0x00000002;
	public static final int DEBUG_FILTER_IGNORE					= 0x00000003;
	public static final int DEBUG_FILTER_REMOVE					= 0x00000004;

	public static final int DEBUG_FILTER_GO_HANDLED				= 0x00000000;
	public static final int DEBUG_FILTER_GO_NOT_HANDLED			= 0x00000001;

	public static final int DEBUG_WAIT_DEFAULT = 0x00000000;

	public static final int DEBUG_VALUE_INVALID		=  0;
	public static final int DEBUG_VALUE_INT8		=  1;
	public static final int DEBUG_VALUE_INT16		=  2;
	public static final int DEBUG_VALUE_INT32		=  3;
	public static final int DEBUG_VALUE_INT64		=  4;
	public static final int DEBUG_VALUE_FLOAT32		=  5;
	public static final int DEBUG_VALUE_FLOAT64		=  6;
	public static final int DEBUG_VALUE_FLOAT80		=  7;
	public static final int DEBUG_VALUE_FLOAT82		=  8;
	public static final int DEBUG_VALUE_FLOAT128	=  9;
	public static final int DEBUG_VALUE_VECTOR64	= 10;
	public static final int DEBUG_VALUE_VECTOR128	= 11;
	
	public static final int DEBUG_VALUE_TYPES		= 12;
	
	public static final int DEBUG_OUT_TEXT_REPL_DEFAULT = 0x00000000;
	
	public static final int DEBUG_ASMOPT_DEFAULT				= 0x00000000;
	public static final int DEBUG_ASMOPT_VERBOSE				= 0x00000001;
	public static final int DEBUG_ASMOPT_NO_CODE_BYTES			= 0x00000002;
	public static final int DEBUG_ASMOPT_IGNORE_OUTPUT_WIDTH	= 0x00000004;
	public static final int DEBUG_ASMOPT_SOURCE_LINE_NUMBER		= 0x00000008;

	public static final int DEBUG_EXPR_MASM			= 0x00000000;
	public static final int DEBUG_EXPR_CPLUSPLUS	= 0x00000001;

	public static final int DEBUG_EINDEX_NAME	= 0x00000000;

	public static final int DEBUG_EINDEX_FROM_START	= 0x00000000;
	public static final int DEBUG_EINDEX_FROM_END	= 0x00000001;
	public static final int DEBUG_EINDEX_FROM_CURRENT = 0x00000002;

	public static final int DEBUG_LOG_DEFAULT	= 0x00000000;
	public static final int DEBUG_LOG_APPEND	= 0x00000001;
	public static final int DEBUG_LOG_UNICODE	= 0x00000002;
	public static final int DEBUG_LOG_DML		= 0x00000004;

	public static final int DEBUG_SYSVERSTR_SERVICE_PACK	= 0x00000000;
	public static final int DEBUG_SYSVERSTR_BUILD			= 0x00000001;

	public static final int DEBUG_MANAGED_DISABLED		= 0x00000000;
	public static final int DEBUG_MANAGED_ALLOWED		= 0x00000001;
	public static final int DEBUG_MANAGED_DLL_LOADED	= 0x00000002;

	public static final int DEBUG_MANSTR_NONE				= 0x00000000;
	public static final int DEBUG_MANSTR_LOADED_SUPPORT_DLL	= 0x00000001;
	public static final int DEBUG_MANSTR_LOAD_STATUS		= 0x00000002;

	public static final int DEBUG_MANRESET_DEFAULT	= 0x00000000;
	public static final int DEBUG_MANRESET_LOAD_DLL	= 0x00000001;

	public static final int INFINITE = 0xffffffff;

	public static final int DEBUG_BREAKPOINT_CODE = 0;
	public static final int DEBUG_BREAKPOINT_DATA = 1;
	
    // IDebugControl.

	public native int getInterrupt();
	public native int setInterrupt(int flags);
	public native int getInterruptTimeout(DebugInt seconds);
	public native int setInterruptTimeout(int seconds);
	public native int getLogFile(DebugString file, DebugBoolean append);
	public native int openLogFile(String file, boolean append);
	public native int closeLogFile();
	public native int getLogMask(DebugInt mask);
	public native int setLogMask(int mask);
	public native int input(DebugString buffer);
	public native int returnInput(String buffer);
	public native int output(int mask, String buffer);
	public native int controlledOutput(int outputControl, int mask, String buffer);
	public native int outputPrompt(int outputControl, String buffer);
	public native int getPromptText(DebugString text);
	public native int outputCurrentState(int outputControl, int flags);
	public native int outputVersionInformation(int outputControl);
	public native int getNotifyEventHandle(DebugLong handle);
	public native int setNotifyEventHandle(long handle);
	public native int assemble(long offset, String instr, DebugLong endOffset);
	public native int disassemble(long offset, int flags, DebugString buffer, DebugLong endOffset);
	public native int getDisassembleEffectiveOffset(DebugLong offset);
	public native int outputDisassembly(int outputControl, long offset,
			int flags, DebugLong endOffset);
    public native int outputDisassemblyLines(int outputControl,
    		int previousLines, long offset, int flags,
    		DebugInt offsetLine, DebugLong startOffset,
    		DebugLong endoffset, long[] lineOffsets);
    public native int getNearInstruction(long offset, int delta,
    		DebugLong nearOffset);
    public native int getStrackTrace(long frameOffset, long stackOffset,
    		long instructionOffset, DebugStackFrame[] frames,
    		DebugInt framesFilled);
    public native int getReturnOffset(DebugLong offset);
    public native int outputStackTrace(int outputControl,
    		DebugStackFrame[] frames, int flags);
    public native int getDebuggeeType(DebugInt cls, DebugInt qualifier);
    public native int getActualProcessorType(DebugInt type);
    public native int getExecutingProcessorType(DebugInt type);
    public native int getNumberPossibleExecutingProcessorTypes(DebugInt number);
    public native int getPossibleExecutingProcessorTypes(int start, int[] types);
    public native int getNumberProcessors(DebugInt number);
    public native int getSystemVersion(DebugInt platformId,
    		DebugInt major, DebugInt minor,
    		DebugString servicePackString, int servicePackStringSize,
    		DebugBoolean servicePackStringUsed,
    		DebugInt servicePackNumber,
    		DebugString buildString, int buildStringSize,
    		DebugBoolean buildStringUsed);
    public native int getPageSize(DebugInt size);
    public native int isPointer64Bit();
    public native int readBugCheckData(DebugInt code,
    		DebugLong arg1, DebugLong arg2, DebugLong arg3, DebugLong arg4);
    public native int getNumberSupportedProcessorTypes(DebugInt number);
    public native int getSupportedProcessorTypes(int start, int[] types);
    public native int getProcessorTypeNames(int type,
    		DebugString fullName, DebugString abbrevName);
    public native int getEffectiveProcessorType(DebugInt type);
    public native int setEffectiveProcessorType(int type);
    public native int getExecutionStatus(DebugInt status);
    public native int setExecutionStatus(int status);
    public native int getCodeLevel(DebugInt level);
    public native int setCodeLevel(int level);
    public native int getEngineOptions(DebugInt options);
    public native int addEngineOptions(int options);
    public native int removeEngineOptions(int options);
    public native int setEngineOptions(int options);
    public native int getSystemErrorControl(DebugInt outputLevel, DebugInt breakLevel);
    public native int setSystemErrorControl(int outputLevel, int breakLevel);
    public native int getTextMacro(int slot, DebugString macro);
    public native int setTextMacro(int slot, String macro);
    public native int getRadix(DebugInt radix);
    public native int setRadix(int radix);
    public native int evaluate(String expression, int desiredType,
    		DebugValue value, DebugInt remainderIndex);
    public native int coerceValue(DebugValue in, int outType, DebugValue out);
    public native int coerceValues(DebugValue[] in, int[] outTypes, DebugValue[] out);
    public native int execute(int outputControl, String command, int flags);
    public native int executeCommandFile(int outputControl, String commandFile, int flags);
    public native int getNumberBreakpoints(DebugInt number);
    public native int getBreakpointByIndex(int index, IDebugBreakpoint bp);
    public native int getBreakpointById(int id, IDebugBreakpoint bp);
    public native int getBreakpointParameters(int[] ids, int start,
    		DebugBreakpointParameters[] params);
    public native int addBreakpoint(int type, int desiredId, IDebugBreakpoint bp);
    public native int removeBreakpoint(IDebugBreakpoint bp);
    public native int addExtension(String path, int flags, DebugLong handle);
    public native int removeExtension(long handle);
    public native int getExtensionByPath(String path, DebugLong handle);
    public native int callExtension(long handle, String function, String arguments);
    public native int getNumberEventFilters(DebugInt specificEvents,
    		DebugInt specificExceptions, DebugInt arbitraryExceptions);
    public native int getEventFilterText(int index, DebugString text);
    public native int getEventFilterCommand(int index, DebugString command);
    public native int setEventFilterCommand(int index, String command);
    public native int getSpecificFilterParameters(int start, DebugSpecificFilterParameters[] params);
    public native int setSpecificFilterParameters(int start, DebugSpecificFilterParameters[] params);
    public native int getSpecificFilterArgument(int index, DebugString argument);
    public native int setSpecificFilterArgument(int index, String argument);
    public native int getExceptionFilterParameters(int[] codes, int start, DebugExceptionFilterParameters[] params);
    public native int setExceptionFilterParameters(DebugExceptionFilterParameters[] params);
    public native int getExceptionFilterSecondCommand(int index, DebugString command);
    public native int setExceptionFilterSecondCommand(int index, String command);
    public native int waitForEvent(int flags, int timeout);
    public native int getLastEventInformation(DebugInt type,
    		DebugInt processId, DebugInt threadId,
    		DebugValue extraInformation, DebugString description);

    // IDebugControl2.

    public native int getCurrentTimeDate(DebugInt timeDate);
    public native int getCurrentSystemUpTime(DebugInt upTime);
    public native int getDumpFormatFlags(DebugInt formatFlags);
    public native int getNumberTextReplacements(DebugInt numRepl);
    public native int getTextReplacement(String srcText, int index,
    		DebugString src, DebugString dst);
    public native int setTextReplacement(String src, String dst);
    public native int removeTextReplacements();
    public native int outputTextReplacements(int outputControl, int flags);

    // IDebugControl3.

    public native int getAssemblyOptions(DebugInt options);
    public native int addAssemblyOptions(int options);
    public native int removeAssemblyOptions(int options);
    public native int setAssemblyOptions(int options);
    public native int getExpressionSyntax(DebugInt flags);
    public native int setExpressionSyntax(int flags);
    public native int setExpressionSyntaxByName(String abbrevName);
    public native int getNumberExpressionSyntaxes(DebugInt number);
    public native int getExpressionSyntaxNames(int index,
    		DebugString fullName, DebugString abbrevName);
    public native int getNumberEvents(DebugInt events);
    public native int getEventIndexDescription(int index, int which,
    		DebugString desc);
    public native int getCurrentEventIndex(DebugInt index);
    public native int setNextEventIndex(int relation, int value,
    		DebugInt nextIndex);

    // IDebugControl4.

    public native int getLogFile2(DebugString file, DebugInt flags);
    public native int openLogFile2(String file, int flags);
    public native int getSystemVersionValues(DebugInt platformId,
    		DebugInt win32Major, DebugInt win32Minor,
    		DebugInt kdMajor, DebugInt kdMinor);
    public native int getSystemVersionString(int which, DebugString string);
    public native int getManagedStatus(DebugInt flags, int whichString,
    		DebugString string, int stringSize, DebugBoolean stringNeeded);
    public native int resetManagedStatus(int flags);
    
}
