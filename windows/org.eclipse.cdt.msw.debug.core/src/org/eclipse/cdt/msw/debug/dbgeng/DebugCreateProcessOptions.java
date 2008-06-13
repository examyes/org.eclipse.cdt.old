package org.eclipse.cdt.msw.debug.dbgeng;

public class DebugCreateProcessOptions {

	// Process Creation Flags
	public static int CREATE_BREAKAWAY_FROM_JOB = 0x01000000;
	public static int CREATE_DEFAULT_ERROR_MODE = 0x04000000;
	public static int CREATE_NEW_CONSOLE = 0x00000010;
	public static int CREATE_NEW_PROCESS_GROUP = 0x00000200;
	public static int CREATE_NO_WINDOW = 0x08000000;
	public static int CREATE_PROTECTED_PROCESS = 0x00040000;
	public static int CREATE_PRESERVE_CODE_AUTHZ_LEVEL = 0x02000000;
	public static int CREATE_SEPARATE_WOW_VDM = 0x00000800;
	public static int CREATE_SHARED_WOW_VDM = 0x00001000;
	public static int CREATE_SUSPENDED = 0x00000004;
	public static int CREATE_UNICODE_ENVIRONMENT = 0x00000400;
	public static int DEBUG_ONLY_THIS_PROCESS = 0x00000002;
	public static int DEBUG_PROCESS = 0x00000001;
	public static int DETACHED_PROCESS = 0x00000008;
	public static int EXTENDED_STARTUPINFO_PRESENT = 0x00080000;
	public static int DEBUG_CREATE_PROCESS_NO_DEBUG_HEAP = CREATE_UNICODE_ENVIRONMENT;
	public static int DEBUG_CREATE_PROCESS_THROUGH_RTL = 0x00010000;
	
}
