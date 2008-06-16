package org.eclipse.cdt.msw.debug.dbgeng;

public class IDebugEventCallbacks {

	final long p;
	
	private native long nativeInit();
	
	public IDebugEventCallbacks() {
		p = nativeInit();
	}
	
	private static native void nativeDelete(long p);
	
	@Override
	protected void finalize() throws Throwable {
		nativeDelete(p);
		super.finalize();
	}
	
	protected int getInterestMask() {
		return 0;
	}
	
	@SuppressWarnings("unused")
	private int nativeBreakpoint(long breakpoint) {
		return breakpoint(new IDebugBreakpoint(breakpoint));
	}
	
	protected int breakpoint(IDebugBreakpoint breakpoint) {
		return DebugStatus.NO_CHANGE;
	}

	@SuppressWarnings("unused")
	private int nativeException(long exception, int firstChance) {
		return exception(new ExceptionRecord64(exception), firstChance);
	}
	
    protected int exception(ExceptionRecord64 exception, int firstChance) {
    	return DebugStatus.NO_CHANGE;
    }
    
    protected int createThread(long handle, long dataOffset, long startOffset) {
    	return DebugStatus.NO_CHANGE;
    }
    
    protected int exitThread(int exitCode) {
    	return DebugStatus.NO_CHANGE;
    }

    protected int createProcess(long imageFileHandle, long handle, long baseOffset,
    		int moduleSize, String moduleName, String imageName,
    		int checkSum, int timeDateStamp,
    		long initialThreadHandle, long threadDataOffset, long startOffset) {
    	return DebugStatus.NO_CHANGE;
    }
    
    protected int exitProcess(int exitCode) {
    	return DebugStatus.NO_CHANGE;
    }

    protected int loadModule(long imageFileHandle, long baseOffset, int moduleSize,
    		String moduleName, String imageName, int checkSum, int timeDateStamp) {
    	return DebugStatus.NO_CHANGE;
    }

    protected int unloadModule(String imageBaseName, long baseOffset) {
    	return DebugStatus.NO_CHANGE;
    }

    protected int systemError(int error, int level) {
    	return DebugStatus.NO_CHANGE;
    }

    protected int sessionStatus(int status) {
    	return DebugStatus.NO_CHANGE;
    }

    protected int changeDebuggeeState(int flags, long argument) {
		return DebugObjectFactory.S_OK;
	}
	
    protected int changeEngineState(int flags, long argument) {
		return DebugObjectFactory.S_OK;
	}

    protected int changeSymbolState(int flags, long argument) {
		return DebugObjectFactory.S_OK;
	}

}
