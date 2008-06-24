package org.eclipse.cdt.msw.debug.core.model;

import org.eclipse.cdt.msw.debug.dbgeng.DebugEvent;
import org.eclipse.cdt.msw.debug.dbgeng.DebugStatus;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugEventCallbacks;

public class WinDebugEventCallbacks extends IDebugEventCallbacks {

	private final WinDebugTarget target;
	
	public WinDebugEventCallbacks(WinDebugTarget target) {
		this.target = target;
	}

	@Override
	protected int getInterestMask() {
		return DebugEvent.EXIT_PROCESS;
	}
	
	@Override
	protected int createProcess(long imageFileHandle, long handle,
			long baseOffset, int moduleSize, String moduleName,
			String imageName, int checkSum, int timeDateStamp,
			long initialThreadHandle, long threadDataOffset, long startOffset) {
		return DebugStatus.NO_CHANGE;
	}
	
	@Override
	protected int exitProcess(int exitCode) {
		target.exitProcess(exitCode);
		return DebugStatus.NO_CHANGE;
	}
	
}
