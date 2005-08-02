/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.debug.win32.core.dbgeng;

public class DEBUG_PROCESS_CREATE_OPTIONS {

	// Creation flags
	public static final int DEBUG_PROCESS                     = 0x00000001;
	public static final int DEBUG_ONLY_THIS_PROCESS           = 0x00000002;
	public static final int CREATE_SUSPENDED                  = 0x00000004;
	public static final int DETACHED_PROCESS                  = 0x00000008;
	public static final int CREATE_NEW_CONSOLE                = 0x00000010;
	public static final int NORMAL_PRIORITY_CLASS             = 0x00000020;
	public static final int IDLE_PRIORITY_CLASS               = 0x00000040;
	public static final int HIGH_PRIORITY_CLASS               = 0x00000080;
	public static final int REALTIME_PRIORITY_CLASS           = 0x00000100;
	public static final int CREATE_NEW_PROCESS_GROUP          = 0x00000200;
	public static final int CREATE_UNICODE_ENVIRONMENT        = 0x00000400;
	public static final int CREATE_SEPARATE_WOW_VDM           = 0x00000800;
	public static final int CREATE_SHARED_WOW_VDM             = 0x00001000;
	public static final int CREATE_FORCEDOS                   = 0x00002000;
	public static final int BELOW_NORMAL_PRIORITY_CLASS       = 0x00004000;
	public static final int ABOVE_NORMAL_PRIORITY_CLASS       = 0x00008000;
	public static final int STACK_SIZE_PARAM_IS_A_RESERVATION = 0x00010000;
	public static final int CREATE_BREAKAWAY_FROM_JOB         = 0x01000000;
	public static final int CREATE_PRESERVE_CODE_AUTHZ_LEVEL  = 0x02000000;
	public static final int CREATE_DEFAULT_ERROR_MODE         = 0x04000000;
	public static final int CREATE_NO_WINDOW                  = 0x08000000;
	public static final int PROFILE_USER                      = 0x10000000;
	public static final int PROFILE_KERNEL                    = 0x20000000;
	public static final int PROFILE_SERVER                    = 0x40000000;
	public static final int CREATE_IGNORE_SYSTEM_DEFAULT      = 0x80000000;

}
