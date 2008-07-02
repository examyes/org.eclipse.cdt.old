package org.eclipse.cdt.msw.debug.dbgeng;

public interface DebugConstants {

	public static final int DEBUG_ANY_ID = 0xffffffff;

	public static final int DEBUG_BREAKPOINT_CODE = 0;
	public static final int DEBUG_BREAKPOINT_DATA = 1;

	public static final int DEBUG_BREAKPOINT_GO_ONLY = 0x00000001;
	public static final int DEBUG_BREAKPOINT_DEFERRED = 0x00000002;
	public static final int DEBUG_BREAKPOINT_ENABLED = 0x00000004;
	public static final int DEBUG_BREAKPOINT_ADDER_ONLY = 0x00000008;
	public static final int DEBUG_BREAKPOINT_ONE_SHOT = 0x00000010;

}
