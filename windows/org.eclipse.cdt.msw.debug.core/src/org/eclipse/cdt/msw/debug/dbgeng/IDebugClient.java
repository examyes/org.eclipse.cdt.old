package org.eclipse.cdt.msw.debug.dbgeng;

public class IDebugClient extends DebugObject {

	IDebugClient(long p) {
		super(p);
	}

	public void createProcess(long server, String commandLine, int createFlags)
			throws HRESULTException {
		nativeCreateProcess(p, server, commandLine, createFlags);
	}
	
	private static native void nativeCreateProcess(long p, long server, String commandLine,
			int createFlags) throws HRESULTException;

	public void setEventCallbacks(IDebugEventCallbacks callbacks) throws HRESULTException {
		nativeSetEventCallbacks(p, callbacks.p);
	}
	
	private static native void nativeSetEventCallbacks(long p, long callbacks) throws HRESULTException;
	
	public void terminateCurrentProcess() throws HRESULTException {
		nativeTerminateCurrentProcess(p);
	}
	
	private static native void nativeTerminateCurrentProcess(long p) throws HRESULTException;
}
