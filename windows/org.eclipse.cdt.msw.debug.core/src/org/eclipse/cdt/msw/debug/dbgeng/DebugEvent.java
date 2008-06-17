package org.eclipse.cdt.msw.debug.dbgeng;

public interface DebugEvent {

	public static final int BREAKPOINT = 0x00000001;
	public static final int EXCEPTION = 0x00000002;
	public static final int CREATE_THREAD = 0x00000004;
	public static final int EXIT_THREAD = 0x00000008;
	public static final int CREATE_PROCESS = 0x00000010;
	public static final int EXIT_PROCESS = 0x00000020;
	public static final int LOAD_MODULE = 0x00000040;
	public static final int UNLOAD_MODULE = 0x00000080;
	public static final int SYSTEM_ERROR = 0x00000100;
	public static final int SESSION_STATUS = 0x00000200;
	public static final int CHANGE_DEBUGGEE_STATE = 0x00000400;
	public static final int CHANGE_ENGINE_STATE = 0x00000800;
	public static final int CHANGE_SYMBOL_STATE = 0x00001000;

}
