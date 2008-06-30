package org.eclipse.cdt.msw.debug.dbgeng;

public class IDebugSymbolGroup extends DebugObject {

	IDebugSymbolGroup(long p) {
		super(p);
	}

	public int getNumberSymbols() throws HRESULTException {
		return nativeGetNumberSymbols(p);
	}
	
	private static native int nativeGetNumberSymbols(long p) throws HRESULTException;

	public String getSymbolName(int index) throws HRESULTException {
		return nativeGetSymbolName(p, index);
	}
	
	private static native String nativeGetSymbolName(long p, int index) throws HRESULTException;
	
	public String getSymbolValueText(int index) throws HRESULTException {
		return nativeGetSymbolValueText(p, index);
	}
	
	private static native String nativeGetSymbolValueText(long p, int index) throws HRESULTException;
	
}
