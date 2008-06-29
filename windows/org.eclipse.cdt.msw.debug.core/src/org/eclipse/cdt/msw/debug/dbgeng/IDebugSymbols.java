package org.eclipse.cdt.msw.debug.dbgeng;

public class IDebugSymbols extends DebugObject {

	IDebugSymbols(long p) {
		super(p);
	}

	public String getNameByOffset(long offset) throws HRESULTException {
		return nativeGetNameByOffset(p, offset);
	}
	
	private static native String nativeGetNameByOffset(long p, long offset) throws HRESULTException;

	public int getSymbolOptions() throws HRESULTException {
		return nativeGetSymbolOptions(p);
	}
	
	private static native int nativeGetSymbolOptions(long p) throws HRESULTException;
	
	public void setSymbolOptions(int options) throws HRESULTException {
		nativeSetSymbolOptions(p, options);
	}
	
	private static native void nativeSetSymbolOptions(long p, int options) throws HRESULTException;
	
	public DebugModuleAndId getSymbolEntryByOffset(long offset, int flags) throws HRESULTException {
		DebugModuleAndId id = new DebugModuleAndId();
		return nativeGetSymbolEntryByOffset(p, offset, flags, id.p) ? id : null;
	}
	
	private static native boolean nativeGetSymbolEntryByOffset(long p, long offset, int flags, byte[] id) throws HRESULTException;
	
	public String getSymbolEntryString(DebugModuleAndId id, int which) throws HRESULTException {
		return nativeGetSymbolEntryString(p, id.p, which);
	}
	
	private static native String nativeGetSymbolEntryString(long p, byte[] id, int which) throws HRESULTException;
	
	public void setImagePath(String path) throws HRESULTException {
		nativeSetImagePath(p, path);
	}
	
	private static native void nativeSetImagePath(long p, String path) throws HRESULTException;

	public void setSymbolPath(String path) throws HRESULTException {
		nativeSetSymbolPath(p, path);
	}
	
	private static native void nativeSetSymbolPath(long p, String path) throws HRESULTException;

	public String getFileByOffset(long offset) throws HRESULTException {
		return nativeGetFileByOffset(p, offset);
	}
	
	private static native String nativeGetFileByOffset(long p, long offset) throws HRESULTException;
	
	public int getLineByOffset(long offset) throws HRESULTException {
		return nativeGetLineByOffset(p, offset);
	}
	
	private static native int nativeGetLineByOffset(long p, long offset) throws HRESULTException;
	
}
