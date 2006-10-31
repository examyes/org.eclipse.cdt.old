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
 * This is actually IDebugClient5.
 * 
 */
public class IDebugClient {

	// pointer to real object used only by native code
	@SuppressWarnings("unused")
	private long p;
	
	// Create the client using the CreateClient function
	public static native IDebugClient create();
	
	public static final int DEBUG_ATTACH_KERNEL_CONNECTION	= 0x00000000;
	public static final int DEBUG_ATTACH_LOCAL_KERNEL		= 0x00000001;
	public static final int DEBUG_ATTACH_EXDI_DRIVER		= 0x00000002;

	public static final int DEBUG_GET_PROC_DEFAULT		= 0x00000000;
	public static final int DEBUG_GET_PROC_FULL_MATCH	= 0x00000001;
	public static final int DEBUG_GET_PROC_ONLY_MATCH	= 0x00000002;
	public static final int DEBUG_GET_PROC_SERVICE_NAME	= 0x00000004;

	public static final int DEBUG_PROC_DESC_DEFAULT			= 0x00000000;
	public static final int DEBUG_PROC_DESC_NO_PATHS		= 0x00000001;
	public static final int DEBUG_PROC_DESC_NO_SERVICES		= 0x00000002;
	public static final int DEBUG_PROC_DESC_NO_MTS_PACKAGES	= 0x00000004;
	public static final int DEBUG_PROC_DESC_NO_COMMAND_LINE	= 0x00000008;
	public static final int DEBUG_PROC_DESC_NO_SESSION_ID	= 0x00000010;
	public static final int DEBUG_PROC_DESC_NO_USER_NAME	= 0x00000020;

	public static final int DEBUG_ATTACH_DEFAULT					= 0x00000000;
	public static final int DEBUG_ATTACH_NONINVASIVE				= 0x00000001;
	public static final int DEBUG_ATTACH_EXISTING					= 0x00000002;
	public static final int DEBUG_ATTACH_NONINVASIVE_NO_SUSPEND		= 0x00000004;
	public static final int DEBUG_ATTACH_INVASIVE_NO_INITIAL_BREAK	= 0x00000008;
	public static final int DEBUG_ATTACH_INVASIVE_RESUME_PROCESS	= 0x00000010;
	public static final int DEBUG_ATTACH_NONINVASIVE_ALLOW_PARTIAL	= 0x00000020;

	public static final int DEBUG_PROCESS_DETACH_ON_EXIT	= 0x00000001;
	public static final int DEBUG_PROCESS_ONLY_THIS_PROCESS	= 0x00000002;
	
	public static final int DEBUG_CONNECT_SESSION_DEFAULT		= 0x00000000;
	public static final int DEBUG_CONNECT_SESSION_NO_VERSION	= 0x00000001;
	public static final int DEBUG_CONNECT_SESSION_NO_ANNOUNCE	= 0x00000002;

	public static final int DEBUG_SERVERS_DEBUGGER	= 0x00000001;
	public static final int DEBUG_SERVERS_PROCESS	= 0x00000002;
	public static final int DEBUG_SERVERS_ALL		= 0x00000003;

	public static final int DEBUG_END_PASSIVE			= 0x00000000;
	public static final int DEBUG_END_ACTIVE_TERMINATE	= 0x00000001;
	public static final int DEBUG_END_ACTIVE_DETACH		= 0x00000002;
	public static final int DEBUG_END_REENTRANT			= 0x00000003;
	public static final int DEBUG_END_DISCONNECT		= 0x00000004;

	public static final int DEBUG_OUTPUT_NORMAL				= 0x00000001;
	public static final int DEBUG_OUTPUT_ERROR				= 0x00000002;
	public static final int DEBUG_OUTPUT_WARNING			= 0x00000004;
	public static final int DEBUG_OUTPUT_VERBOSE			= 0x00000008;
	public static final int DEBUG_OUTPUT_PROMPT				= 0x00000010;
	public static final int DEBUG_OUTPUT_PROMPT_REGISTERS	= 0x00000020;
	public static final int DEBUG_OUTPUT_EXTENSION_WARNING	= 0x00000040;
	public static final int DEBUG_OUTPUT_DEBUGGEE			= 0x00000080;
	public static final int DEBUG_OUTPUT_DEBUGGEE_PROMPT	= 0x00000100;
	public static final int DEBUG_OUTPUT_SYMBOLS			= 0x00000200;

	public static final int DEBUG_IOUTPUT_KD_PROTOCOL	= 0x80000000;
	public static final int DEBUG_IOUTPUT_REMOTING		= 0x40000000;
	public static final int DEBUG_IOUTPUT_BREAKPOINT	= 0x20000000;
	public static final int DEBUG_IOUTPUT_EVENT			= 0x10000000;

	public static final int DEBUG_OUTPUT_IDENTITY_DEFAULT = 0x00000000;
	
	public static final int DEBUG_FORMAT_DEFAULT				= 0x00000000;
	public static final int DEBUG_FORMAT_WRITE_CAB				= 0x20000000;
	public static final int DEBUG_FORMAT_CAB_SECONDARY_FILES	= 0x40000000;
	public static final int DEBUG_FORMAT_NO_OVERWRITE			= 0x80000000;

	public static final int DEBUG_FORMAT_USER_SMALL_FULL_MEMORY					= 0x00000001;
	public static final int DEBUG_FORMAT_USER_SMALL_HANDLE_DATA					= 0x00000002;
	public static final int DEBUG_FORMAT_USER_SMALL_UNLOADED_MODULES			= 0x00000004;
	public static final int DEBUG_FORMAT_USER_SMALL_INDIRECT_MEMORY				= 0x00000008;
	public static final int DEBUG_FORMAT_USER_SMALL_DATA_SEGMENTS				= 0x00000010;
	public static final int DEBUG_FORMAT_USER_SMALL_FILTER_MEMORY 				= 0x00000020;
	public static final int DEBUG_FORMAT_USER_SMALL_FILTER_PATHS				= 0x00000040;
	public static final int DEBUG_FORMAT_USER_SMALL_PROCESS_THREAD_DATA			= 0x00000080;
	public static final int DEBUG_FORMAT_USER_SMALL_PRIVATE_READ_WRITE_MEMORY	= 0x00000100;
	public static final int DEBUG_FORMAT_USER_SMALL_NO_OPTIONAL_DATA			= 0x00000200;
	public static final int DEBUG_FORMAT_USER_SMALL_FULL_MEMORY_INFO			= 0x00000400;
	public static final int DEBUG_FORMAT_USER_SMALL_THREAD_INFO					= 0x00000800;
	public static final int DEBUG_FORMAT_USER_SMALL_CODE_SEGMENTS				= 0x00001000;
	public static final int DEBUG_FORMAT_USER_SMALL_NO_AUXILIARY_STATE			= 0x00002000;
	public static final int DEBUG_FORMAT_USER_SMALL_FULL_AUXILIARY_STATE		= 0x00004000;

	public static final int DEBUG_DUMP_FILE_BASE				= 0xffffffff;
	public static final int DEBUG_DUMP_FILE_PAGE_FILE_DUMP		= 0x00000000;

	public static final int DEBUG_DUMP_FILE_LOAD_FAILED_INDEX	= 0xffffffff;
	public static final int DEBUG_DUMP_FILE_ORIGINAL_CAB_INDEX	= 0xfffffffe;

	// IDebugClient
	public native int attachKernel(int flags, String connectOptions);
	public native int getKernelConnectionOptions(DebugString options);
	public native int setKernelConnectionOptions(String options);
	public native int startProcessServer(int flags, String options);
	public native int connectProcessServer(String remoteOptions, DebugLong server);
	public native int disconnectProcessServer(long server);
	public native int getRunningProcessSystemIds(long server, DebugIntArray ids);
	public native int getRunningProcessSystemIdByExecutableName(
			long server, String exeName, int flags, DebugInt id);
	public native int getRunningProcessDescription(
			long server, int systemId, int flags,
			DebugString exeName, DebugString description);
	public native int attachProcess(long server, int processId, int attachFlags);
	public native int createProcess(long server, String commandLine, int createFlags);
	public native int createProcessAndAttach(long server, String commandLine,
			int createFlags, int processId, int attachFlags);
	public native int getProcessOptions(DebugInt options);
	public native int addProcessOptions(int options);
	public native int removeProcessOptions(int options);
	public native int setProcessOptions(int options);
	public native int openDumpFile(String dumpFile);
	
	// use writeDumpFile2 since there is no UNICODE version of this one
//	public native int writeDumpFile(String dumpFile, int qualifier);
	public native int connectSession(int flags, int historyLimit);
	public native int startServer(String options);
	public native int outputServers(int outputControl, String machine, int flags);
	public native int terminateProcesses();
	public native int detachProcesses();
	public native int endSession(int flags);
	public native int getExitCode(DebugInt code);
	public native int dispatchCallbacks(int timeout);
	public native int exitDispatch(IDebugClient client);
	public native int createClient(IDebugClient client);
	public native int getInputCallbacks(IDebugInputCallbacks callbacks);
	public native int setInputCallbacks(IDebugInputCallbacks callbacks);
	public native int getOutputCallbacks(IDebugOutputCallbacks callbacks);
	public native int setOutputCallbacks(IDebugOutputCallbacks callbacks);
	public native int getOutputMask(DebugInt mask);
	public native int setOutputMask(int mask);
	public native int getOtherOutputMask(IDebugClient client, DebugInt mask);
	public native int setOtherOutputMask(IDebugClient client, int mask);
	public native int getOutputWidth(DebugInt columns);
	public native int setOutputWidth(int columns);
	public native int getOutputLinePrefix(DebugString prefix);
	public native int setOutputLinePrefix(String prefix);
	public native int getIdentity(DebugString identity);
	public native int outputIdentity(int outputControl, int flags, String format);
	public native int getEventCallbacks(IDebugEventCallbacks callbacks);
	public native int setEventCallbacks(IDebugEventCallbacks callbacks);
	public native int flushCallbacks();
    // IDebugClient2
	public native int writeDumpFile2(String dumpFile, int qualifier, int formatFlags,
			String comment);
	public native int addDumpInformationFile(String infoFile, int type);
	public native int endProcessServer(long server);
	public native int waitForProcessServerEnd(int timeout);
	public native int isKernelDebuggerEnabled();
	public native int terminateCurrentProcess();
	public native int detachCurrentProcess();
	public native int abandonCurrentProcess();
	// IDebugClient3
	// - unicode versions of above, which we are actually using anyway
	// IDebugClient4
	// - more unicode
	public native int getNumberDumpFiles(DebugInt number);
	public native int getDumpFile(int index, DebugString dumpFile,
			DebugInt nameSize, DebugLong handle, int type);
	public native int createProcess2(long server, String commandLine,
			DebugCreateProcessOptions options, String initialDirectory,
			String environment);
	public native int createProcessAndAttach2(long server, String commandLine,
			DebugCreateProcessOptions options, String initialDirectory,
			String enviornment, int processId, int attachFlags);
	public native int pushOutputLinePrefix(String newPrefix, DebugLong handle);
	public native int popOutputLinePrefix(DebugLong handle);
	public native int getNumberInputCallbacks(DebugInt count);
	public native int getNumberOutputCallbacks(DebugInt count);
	public native int getNumberEventCallbacks(int eventFlags, DebugInt count);
	public native int getQuitLockString(DebugString string);
	public native int setQuitLockString(String string);
	
	// QueryInterface shortcuts
	public native int createControl(IDebugControl control);

}
