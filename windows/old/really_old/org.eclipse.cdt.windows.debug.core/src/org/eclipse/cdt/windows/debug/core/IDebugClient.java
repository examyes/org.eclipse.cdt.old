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
	
	public IDebugClient() {
	}
	
	public static native IDebugClient create();
	
	// QueryInterface shortcuts
	public native int createControl(IDebugControl control);
	
	// IDebugClient
	public native int attachKernel(int flags, String connectOptions);
	public native int getKernelConnectionOptions(IDebugString options);
	public native int setKernelConnectionOptions(String options);
	public native int startProcessServer(int flags, String options);
	public native int connectProcessServer(String remoteOptions, IDebugLong server);
	public native int disconnectProcessServer(long server);
	public native int getRunningProcessSystemIds(long server, IDebugIntArray ids);
	public native int getRunningProcessSystemIdByExecutableName(
			long server, String exeName, int flags, IDebugInt id);
	public native int getRunningProcessDescription(
			long server, int systemId, int flags,
			IDebugString exeName, IDebugString description);
	public native int attachProcess(long server, int processId, int attachFlags);
	public native int createProcess(long server, String commandLine, int createFlags);
	public native int createProcessAndAttach(long server, String commandLine,
			int createFlags, int processId, int attachFlags);
	public native int getProcessOptions(IDebugInt options);
	public native int addProcessOptions(int options);
	public native int removeProcessOptions(int options);
	public native int setProcessOptions(int options);
	public native int openDumpFile(String dumpFile);
	public native int writeDumpFile(String dumpFile, int qualifier);
	public native int connectSession(int flags, int historyLimit);
	public native int startServer(String options);
	public native int outputServers(int outputControl, String machine, int flags);
	public native int terminateProcesses();
	public native int detachProcesses();
	public native int endSession(int flags);
	public native int getExitCode(IDebugInt code);
	public native int dispatchCallbacks(int timeout);
	public native int exitDispatch(IDebugClient client);
	public native int createClient(IDebugClient client);
	public native int getInputCallbacks(IDebugInputCallbacks callbacks);
	public native int setInputCallbacks(IDebugInputCallbacks callbacks);
	public native int getOutputCallbacks(IDebugOutputCallbacks callbacks);
	public native int setOutputCallbacks(IDebugOutputCallbacks callbacks);
	public native int getOutputMask(IDebugInt mask);
	public native int setOutputMask(int mask);
	public native int getOtherOutputMask(IDebugClient client, IDebugInt mask);
	public native int setOtherOutputMask(IDebugClient client, int mask);
	public native int getOutputWidth(IDebugInt columns);
	public native int setOutputWidth(int columns);
	public native int getOutputLinePrefix(IDebugString prefix);
	public native int setOutputLinePrefix(String prefix);
	public native int getIdentity(IDebugString identity);
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
	public native int getNumberDumpFiles(IDebugInt number);
	public native int getDumpFile(int index, IDebugString dumpFile,
			IDebugInt nameSize, IDebugLong handle, int type);
	public native int createProcess2(long server, String commandLine,
			DebugCreateProcessOptions options, String initialDirectory,
			String environment);
	public native int createProcessAndAttach2(long server, String commandLine,
			DebugCreateProcessOptions options, String initialDirectory,
			String enviornment, int processId, int attachFlags);
	public native int pushOutputLinePrefix(String newPrefix, IDebugLong handle);
	public native int popOutputLinePrefix(IDebugLong handle);
	public native int getNumberInputCallbacks(IDebugInt count);
	public native int getNumberOutputCallbacks(IDebugInt count);
	public native int getNumberEventCallbacks(int eventFlags, IDebugInt count);
	public native int getQuitLockString(IDebugString string);
	public native int setQuitLockString(String string);
	
}
