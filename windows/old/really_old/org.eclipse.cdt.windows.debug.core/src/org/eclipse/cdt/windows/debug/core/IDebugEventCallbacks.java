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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public abstract class IDebugEventCallbacks {

	@SuppressWarnings("unused")
	private long p;
	
	private native int init();

	protected IDebugEventCallbacks() throws CoreException {
		if (HRESULT.FAILED(init()))
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to init"));
	}

	//	 Interest mask bits.
	public static final int DEBUG_EVENT_BREAKPOINT = 0x00000001;
	public static final int DEBUG_EVENT_EXCEPTION = 0x00000002;
	public static final int DEBUG_EVENT_CREATE_THREAD = 0x00000004;
	public static final int DEBUG_EVENT_EXIT_THREAD = 0x00000008;
	public static final int DEBUG_EVENT_CREATE_PROCESS = 0x00000010;
	public static final int DEBUG_EVENT_EXIT_PROCESS = 0x00000020;
	public static final int DEBUG_EVENT_LOAD_MODULE = 0x00000040;
	public static final int DEBUG_EVENT_UNLOAD_MODULE = 0x00000080;
	public static final int DEBUG_EVENT_SYSTEM_ERROR = 0x00000100;
	public static final int DEBUG_EVENT_SESSION_STATUS = 0x00000200;
	public static final int DEBUG_EVENT_CHANGE_DEBUGGEE_STATE = 0x00000400;
	public static final int DEBUG_EVENT_CHANGE_ENGINE_STATE = 0x00000800;
	public static final int DEBUG_EVENT_CHANGE_SYMBOL_STATE = 0x00001000;
	
	protected int getInterestMask() {
		return 0;
	}
	
	// Execution status codes used for waiting, for returning current status
	// and for event method return values.
	public static final int DEBUG_STATUS_NO_CHANGE = 0;
	public static final int DEBUG_STATUS_GO = 1;
	public static final int DEBUG_STATUS_GO_HANDLED = 2;
	public static final int DEBUG_STATUS_GO_NOT_HANDLED = 3;
	public static final int DEBUG_STATUS_STEP_OVER = 4;
	public static final int DEBUG_STATUS_STEP_INTO = 5;
	public static final int DEBUG_STATUS_BREAK = 6;
	public static final int DEBUG_STATUS_NO_DEBUGGEE = 7;
	public static final int DEBUG_STATUS_STEP_BRANCH = 8;
	public static final int DEBUG_STATUS_IGNORE_EVENT = 9;
	public static final int DEBUG_STATUS_RESTART_REQUESTED = 10;
	public static final int DEBUG_STATUS_REVERSE_GO = 11;
	public static final int DEBUG_STATUS_REVERSE_STEP_BRANCH = 12;
	public static final int DEBUG_STATUS_REVERSE_STEP_OVER = 13;
	public static final int DEBUG_STATUS_REVERSE_STEP_INTO = 14;
	
	protected int createProcess(
			long imageFileHandle,
		    long handle,
		    long baseOffset,
		    int moduleSize,
		    String moduleName,
		    String imageName,
		    int checkSum,
		    int timeDateStamp,
		    long initialThreadHandle,
		    long threadDataOffset,
		    long startOffset) {
		return DEBUG_STATUS_NO_CHANGE;
	}

	protected int exitProcess(int exitCode) {
		return DEBUG_STATUS_NO_CHANGE;
	}
	
	protected int createThread(
			long handle,
			long dataOffset,
			long startOffset) {
		return DEBUG_STATUS_NO_CHANGE;
	}
	
	protected int exitThread(int exitCode) {
		return DEBUG_STATUS_NO_CHANGE;
	}
	
}
