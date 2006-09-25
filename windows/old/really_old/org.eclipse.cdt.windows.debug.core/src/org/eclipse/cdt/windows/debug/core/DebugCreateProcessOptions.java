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
public class DebugCreateProcessOptions {
	@SuppressWarnings("unused")
	private int createFlags;
	@SuppressWarnings("unused")
	private int engCreateFlags;
	@SuppressWarnings("unused")
	private int verifierFlags;
	
	// Create flags
	public static final int DEBUG_PROCESS = 0x1;
	public static final int DEBUG_ONLY_THIS_PROCESS = 0x2;
	public static final int CREATE_SUSPENDED = 0x4;
	public static final int DEBUG_CREATE_PROCESS_NO_DEBUG_HEAP = 0x400;
	public static final int DEBUG_CREATE_PROCESS_THROUGH_RTL = 0x10000;
	
	public void setCreateFlags(int createFlags) {
		this.createFlags = createFlags;
	}
	
	// Engine Specific Create flags
	public static final int DEBUG_ECREATE_PROCESS_INHERIT_HANDLES = 0x1;
	public static final int DEBUG_ECREATE_PROCESS_USE_VERIFIER_FLAGS = 0x2;
	public static final int DEBUG_ECREATE_PROCESS_USE_IMPLICIT_COMMAND_LINE = 0x4;
	
	public void setEngCreateFlags(int engCreateFlags) {
		this.engCreateFlags = engCreateFlags;
	}
	
	// Verifier flags
	public void setVerifierFlags(int verifierFlags) {
		this.verifierFlags = verifierFlags;
	}
	
}
