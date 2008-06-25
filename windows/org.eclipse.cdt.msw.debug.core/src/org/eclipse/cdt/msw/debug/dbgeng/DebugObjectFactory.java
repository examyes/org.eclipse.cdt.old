package org.eclipse.cdt.msw.debug.dbgeng;


public class DebugObjectFactory {

	public static IDebugClient createClient() throws HRESULTException {
		return new IDebugClient(nativeCreateClient());
	}

	private static native long nativeCreateClient() throws HRESULTException;
	
	public static IDebugControl createControl() throws HRESULTException {
		return new IDebugControl(nativeCreateControl());
	}
	
	private static native long nativeCreateControl() throws HRESULTException;
	
	public static IDebugSystemObjects createSystemObjects() throws HRESULTException {
		return new IDebugSystemObjects(nativeCreateSystemObjects());
	}
	
	private static native long nativeCreateSystemObjects() throws HRESULTException;
	
	static {
		// Make sure we load in the latest dbgeng and dbghelp from Debugging Tools for Windows
		System.load("C:\\Program Files\\Debugging Tools for Windows (x86)\\dbghelp.dll");
		System.load("C:\\Program Files\\Debugging Tools for Windows (x86)\\dbgeng.dll");
		
		// Find the native library
		String nativePath = System.getProperty("cdt.msw.debug.dll");
		if (nativePath != null)
			System.load(nativePath);
		else
			System.loadLibrary("cdt-mswdebug");
	}
}
