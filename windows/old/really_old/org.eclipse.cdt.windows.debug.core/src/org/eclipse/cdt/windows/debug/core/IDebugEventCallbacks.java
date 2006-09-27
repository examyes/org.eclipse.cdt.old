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
public abstract class IDebugEventCallbacks {

	@SuppressWarnings("unused")
	private long p;
	
	private native long init();

	protected IDebugEventCallbacks() {
		p = init();
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
	
	protected abstract int getInterestMask() throws HRESULTFailure;
	
}
