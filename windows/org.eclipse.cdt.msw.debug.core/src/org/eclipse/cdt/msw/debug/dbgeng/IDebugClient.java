package org.eclipse.cdt.msw.debug.dbgeng;

public class IDebugClient extends DebugObject {

	IDebugClient(long p) {
		super(p);
	}

	public void CreateProcess(long server, String commandLine, int createFlags)
			throws HRESULTException {
		nativeCreateProcess(p, server, commandLine, createFlags);
	}
	
	private static native void nativeCreateProcess(long p, long server, String commandLine,
			int createFlags) throws HRESULTException;

}
