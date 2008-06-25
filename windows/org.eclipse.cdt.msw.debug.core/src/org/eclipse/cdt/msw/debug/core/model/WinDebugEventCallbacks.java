package org.eclipse.cdt.msw.debug.core.model;

import org.eclipse.cdt.msw.debug.dbgeng.DebugEvent;
import org.eclipse.cdt.msw.debug.dbgeng.DebugStatus;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugEventCallbacks;
import org.eclipse.debug.core.ILaunch;

public class WinDebugEventCallbacks extends IDebugEventCallbacks {

	private final ILaunch launch;
	private WinDebugTarget target;
	
	public WinDebugEventCallbacks(ILaunch launch) {
		this.launch = launch;
	}

	@Override
	protected int getInterestMask() {
		return DebugEvent.CREATE_PROCESS
			 | DebugEvent.EXIT_PROCESS;
	}
	
	@Override
	protected int createProcess(long imageFileHandle, long handle,
			long baseOffset, int moduleSize, String moduleName,
			String imageName, int checkSum, int timeDateStamp,
			long initialThreadHandle, long threadDataOffset, long startOffset) {
		WinProcess process = new WinProcess(imageName, launch, handle);
		launch.addProcess(process);
		
		target = new WinDebugTarget("Windows Debugger", launch, process);
		launch.addDebugTarget(target);
		return DebugStatus.NO_CHANGE;
	}
	
	@Override
	protected int exitProcess(int exitCode) {
		target.exitProcess(exitCode);
		return DebugStatus.NO_CHANGE;
	}
	
}
