package org.eclipse.cdt.msw.debug.dbgeng;

public class IDebugControl extends DebugObject {

	public static final int INFINITE = 0xFFFFFFFF;
	
	public IDebugControl(long p) {
		super(p);
	}

	public void waitForEvent(int flags, int timeout) throws HRESULTException {
		nativeWaitForEvent(p, flags, timeout);
	}
	
	private static native void nativeWaitForEvent(long p, int flags, int timeout) throws HRESULTException;
	
	public void setExecutionStatus(int status) throws HRESULTException {
		nativeSetExecutionStatus(p, status);
	}
	
	private static native void nativeSetExecutionStatus(long p, int status) throws HRESULTException;
}
