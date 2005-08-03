/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.debug.win32.core.dbgeng;

public class DEBUG_OUTPUT {

	public static final int DEBUG_OUTPUT_NORMAL            = 0x00000001;
	public static final int DEBUG_OUTPUT_ERROR             = 0x00000002;
	public static final int DEBUG_OUTPUT_WARNING           = 0x00000004;
	public static final int DEBUG_OUTPUT_VERBOSE           = 0x00000008;
	public static final int DEBUG_OUTPUT_PROMPT            = 0x00000010;
	public static final int DEBUG_OUTPUT_PROMPT_REGISTERS  = 0x00000020;
	public static final int DEBUG_OUTPUT_EXTENSION_WARNING = 0x00000040;
	public static final int DEBUG_OUTPUT_DEBUGGEE          = 0x00000080;
	public static final int DEBUG_OUTPUT_DEBUGGEE_PROMPT   = 0x00000100;
	public static final int DEBUG_OUTPUT_SYMBOLS           = 0x00000200;

}
