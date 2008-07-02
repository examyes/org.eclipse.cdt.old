package org.eclipse.cdt.msw.debug.dbgeng;

public class IDebugBreakpoint extends DebugObject {

	IDebugBreakpoint(long p) {
		super(p);
	}
	
	public int getId() throws HRESULTException {
		return nativeGetId(p);
	}
	
	private static native int nativeGetId(long p) throws HRESULTException;
	
	public void setOffsetExpression(String expression) throws HRESULTException {
		nativeSetOffsetExpression(p, expression);
	}
	
	private static native void nativeSetOffsetExpression(long p, String expression) throws HRESULTException;
	
	public void addFlags(int flags) throws HRESULTException {
		nativeAddFlags(p, flags);
	}
	
	private static native void nativeAddFlags(long p, int flags) throws HRESULTException;
}
