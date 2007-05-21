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

	static {
		System.loadLibrary("cdtwindbg"); //$NON-NLS-1$
	}
	
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

}
