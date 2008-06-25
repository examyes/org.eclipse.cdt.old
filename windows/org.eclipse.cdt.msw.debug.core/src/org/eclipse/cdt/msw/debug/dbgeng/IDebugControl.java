package org.eclipse.cdt.msw.debug.dbgeng;

public class IDebugControl extends DebugObject {

	public IDebugControl(long p) {
		super(p);
	}

	public static final int INFINITE = 0xFFFFFFFF;
	
	public void waitForEvent(int flags, int timeout) throws HRESULTException {
		nativeWaitForEvent(p, flags, timeout);
	}
	
	private static native void nativeWaitForEvent(long p, int flags, int timeout) throws HRESULTException;
	
	public void setExecutionStatus(int status) throws HRESULTException {
		nativeSetExecutionStatus(p, status);
	}
	
	private static native void nativeSetExecutionStatus(long p, int status) throws HRESULTException;

	public void setInterrupt(int flags) throws HRESULTException {
		nativeSetInterrupt(p, flags);
	}
	
	private static native void nativeSetInterrupt(long p, int flags) throws HRESULTException;
	
	public int getInterrupt() throws HRESULTException {
		return nativeGetInterrupt(p);
	}
	
	private static native int nativeGetInterrupt(long p) throws HRESULTException;
	
}
