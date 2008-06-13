package org.eclipse.cdt.msw.debug.dbgeng;

public class HRESULTException extends Exception {

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
