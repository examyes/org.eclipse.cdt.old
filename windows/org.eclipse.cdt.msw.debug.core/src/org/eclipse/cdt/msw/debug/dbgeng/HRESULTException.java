package org.eclipse.cdt.msw.debug.dbgeng;

public class HRESULTException extends Exception {

	public static final int S_OK = 0;
	public static final int S_FALSE = 1;
	public static final int E_PENDING = 0x8000000a;
	public static final int E_FAIL = 0x80004005;
	public static final int E_UNEXPECTED = 0x8000ffff;
	
	private static final long serialVersionUID = 1L;
	
	private int hresult;
	
	public HRESULTException(int hresult) {
		super(Integer.toHexString(hresult));
		this.hresult = hresult;
	}
	
	public int getHRESULT() {
		return hresult;
	}
	
}
