/**********************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.windows.debug.core.sdk;

/**
 * @author Doug Schaefer
 *
 */
public class Win32Debug {

	public static native boolean test();
	
	// ContinueDebugEvent
	
	public static final int DBG_CONTINUE = 0x00010002;
	public static final int DBG_EXCEPTION_NOT_HANDLED = 0x80010001;
	
	public static native boolean ContinueDebugEvent(
			int processId,
			int threadId,
			int continueStatus);

	// DebugBreakProcess
	
	public static native boolean DebugBreakProcess(long processHandle);
	
	// ReadProcessMemory
	
	public static native boolean ReadProcessMemory(
			long processHandle,
			long baseAddress,
			byte[] buffer,
			long[] numberOfBytesRead);

	// WaitForDebugEvent
	
	public static final int INFINITE = 0xFFFFFFFF;
	
	public static boolean WaitForDebugEvent(DebugEvent debugEvent, int milliseconds) {
		return WaitForDebugEvent(debugEvent.p, milliseconds);
	}
	
	private static native boolean WaitForDebugEvent(long debugEvent, int milliseconds);
	
	// WriteProcessMemory
	
	public static native boolean WriteProcessMemory(
			long processHandle,
			long baseAddress,
			byte[] buffer,
			long[] numberOfBytesWritten);

	// CreateProcess
	
	public static native boolean CreateProcess(String cmdline, String envp, String dir, long[] handles);

	// GetExitCodeProcess
	
	public static native boolean GetExitCodeProcess(long processHandle, int[] exitCode);
	
	// WaitForSingleObject
	
	public static final int WAIT_ABANDONED = 0x80;
	public static final int WAIT_OBJECT_0 = 0;
	public static final int WAIT_TIMEOUT = 0x102;
	
	public static native int WaitForSingleObject(long handle, int milliseconds);
	
	// TerminateProcess
	
	public static native boolean TerminateProcess(long processHandle, int exitCode);
	
	// GenerateConsoleCtrlEvent
	
	public static final int CTRL_C_EVENT = 0;
	public static final int CTRL_BREAK_EVENT = 1;
	
	public static native boolean GenerateConsoleCtrlEvent(int ctrlEvent, int processGroupId);
	
	// GetProcessId
	
	public static native int GetProcessId(long processHandle);
	
	// ReadFile
	
	public static native boolean ReadFile(long handle, byte[] buffer, int[] numberOfBytesRead);

	// WriteFile
	
	public static native boolean WriteFile(long handle, byte[] buffer, int numberOfBytesToWrite,
			int[] numberOfBytesWritten);

}
