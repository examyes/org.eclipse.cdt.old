package org.eclipse.cdt.msw.debug.dbgeng;

public class IDebugSystemObjects extends DebugObject {

	IDebugSystemObjects(long p) {
		super(p);
	}

	public int getProcessIdByHandle(long handle) throws HRESULTException {
		return nativeGetProcessIdByHandle(p, handle);
	}
	
	private static native int nativeGetProcessIdByHandle(long p, long handle) throws HRESULTException;
	
	public void setCurrentProcessId(int id) throws HRESULTException  {
		nativeSetCurrentProcessId(p, id);
	}
	
	private static native void nativeSetCurrentProcessId(long p, int id) throws HRESULTException;

	public long getCurrentProcessHandle() throws HRESULTException {
		return nativeGetCurrentProcessHandle(p);
	}
	
	private static native long nativeGetCurrentProcessHandle(long p) throws HRESULTException;
}
