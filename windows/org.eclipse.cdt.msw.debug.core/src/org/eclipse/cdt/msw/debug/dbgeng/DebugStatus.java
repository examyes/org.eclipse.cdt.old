package org.eclipse.cdt.msw.debug.dbgeng;

public interface DebugStatus {

	public static final int NO_CHANGE = 0;
	public static final int GO = 1;
	public static final int GO_HANDLED = 2;
	public static final int GO_NOT_HANDLED = 3;
	public static final int STEP_OVER = 4;
	public static final int STEP_INTO = 5;
	public static final int BREAK = 6;
	public static final int NO_DEBUGGEE = 7;
	public static final int STEP_BRANCH = 8;
	public static final int IGNORE_EVENT = 9;
	public static final int RESTART_REQUESTED = 10;

}
