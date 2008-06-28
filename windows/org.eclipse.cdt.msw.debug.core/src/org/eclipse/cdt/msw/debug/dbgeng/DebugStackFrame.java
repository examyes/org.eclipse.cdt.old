package org.eclipse.cdt.msw.debug.dbgeng;

public class DebugStackFrame {

	public static class FrameChunk extends DebugObject {
		public FrameChunk() {
			super(nativeInit());
		}
		
		@Override
		protected void finalize() throws Throwable {
			nativeDelete(p);
		}
		
		public DebugStackFrame[] getFrames(int size) {
			DebugStackFrame[] frames = new DebugStackFrame[size];
			for (int i = 0; i < size; ++i)
				frames[i] = new DebugStackFrame(this, i);
			return frames;
		}
	}
	
	private static native long nativeInit();
	private static native void nativeDelete(long p);

	private final FrameChunk chunk;
	private final int index;
	
	private DebugStackFrame(FrameChunk chunk, int index) {
		this.chunk = chunk;
		this.index = index;
	}

	public long getInstructionOffset() {
		return nativeGetInstructionOffset(chunk.p, index);
	}
	
    private static native long nativeGetInstructionOffset(long p, int i);
    
    public long getReturnOffset() {
    	return nativeGetReturnOffset(chunk.p, index);
    }
    
    private static native long nativeGetReturnOffset(long p, int i);
    
    public long getFrameOffset() {
    	return nativeGetFrameOffset(chunk.p, index);
    }
    
    private static native long nativeGetFrameOffset(long p, int i);
    
    public long getStackOffset() {
    	return nativeGetStackOffset(chunk.p, index);
    }
    
    private static native long nativeGetStackOffset(long p, int i);
    
    public long getFuncTableEntry() {
    	return nativeGetFuncTableEntry(chunk.p, index);
    }
    
    private static native long nativeGetFuncTableEntry(long p, int i);
    
    public long[] getParams() {
    	long[] params = new long[4];
    	nativeGetParams(chunk.p, index, params);
    	return params;
    }
    
    private static native void nativeGetParams(long p, int i, long params[/*4*/]);
    
    public boolean getVirtual() {
    	return nativeGetVirtual(chunk.p, index);
    }
    
    private static native boolean nativeGetVirtual(long p, int i);
    
    public int getFrameNumber() {
    	return nativeGetFrameNumber(chunk.p, index);
    }
    
    private static native int nativeGetFrameNumber(long p, int i);
    
}
