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
public class DebugEvent {

	final long p;
	
	public DebugEvent() {
		p = allocateDebugEvent();
	}
	
	private static native long allocateDebugEvent();

	protected void finalize() throws Throwable {
		super.finalize();
		freeDebugEvent(p);
	}

	private static native void freeDebugEvent(long debugEvent);

	// DebugEventCode
	
	public static final int EXCEPTION_DEBUG_EVENT = 1;
	public static final int CREATE_THREAD_DEBUG_EVENT = 2;
	public static final int CREATE_PROCESS_DEBUG_EVENT = 3;
	public static final int EXIT_THREAD_DEBUG_EVENT = 4;
	public static final int EXIT_PROCESS_DEBUG_EVENT = 5;
	public static final int LOAD_DLL_DEBUG_EVENT = 6;
	public static final int UNLOAD_DLL_DEBUG_EVENT = 7;
	public static final int OUTPUT_DEBUG_STRING_EVENT = 8;
	public static final int RIP_EVENT = 9;
	
	public int getDebugEventCode() {
		return getDebugEventCode(p);
	}
	
	private static native int getDebugEventCode(long p);
	
	// ProcessId
	
	public int getProcessId() {
		return getProcessId(p);
	}
	
	private static native int getProcessId(long p);
	
	// ThreadId
	
	public int getThreadId() {
		return getThreadId(p);
	}
	
	private static native int getThreadId(long p);
	
	// ExitProcessDebugInfo
	
	public class ExitProcessDebugInfo {
		public int getExitCode() {
			return getExitProcessExitCode(p);
		}
		
	}
	
	public ExitProcessDebugInfo getExitProcessDebugInfo() {
		if (getDebugEventCode() != EXIT_PROCESS_DEBUG_EVENT)
			throw new IllegalArgumentException();
		
		return new ExitProcessDebugInfo();
	}
	
	public native int getExitProcessExitCode(long p);

}
