package org.eclipse.cdt.msw.debug.core.model;

import org.eclipse.cdt.msw.debug.core.controller.WinDebugController;
import org.eclipse.cdt.msw.debug.dbgeng.DebugEvent;
import org.eclipse.cdt.msw.debug.dbgeng.DebugStatus;
import org.eclipse.cdt.msw.debug.dbgeng.HRESULTException;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugEventCallbacks;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;

public class WinDebugEventCallbacks extends IDebugEventCallbacks {

	private ILaunch currentLaunch;
	
	public WinDebugEventCallbacks() {
	}

	@Override
	protected int getInterestMask() {
		return DebugEvent.CREATE_PROCESS
			 | DebugEvent.EXIT_PROCESS;
	}
	
	public void setCurrentLaunch(ILaunch launch) {
		currentLaunch = launch;
	}
	
	@Override
	protected int createProcess(long imageFileHandle, long handle,
			long baseOffset, int moduleSize, String moduleName,
			String imageName, int checkSum, int timeDateStamp,
			long initialThreadHandle, long threadDataOffset, long startOffset) {
		WinProcess process = new WinProcess(imageName, currentLaunch, handle);
		currentLaunch.addProcess(process);
		
		IDebugTarget target = new WinDebugTarget("Windows Debugger", currentLaunch, process);
		currentLaunch.addDebugTarget(target);
		return DebugStatus.NO_CHANGE;
	}
	
	@Override
	protected int exitProcess(int exitCode) {
		WinDebugController controller = WinDebugController.getController(); 
		long currProcess;
		try {
			currProcess = controller.getDebugSystemObjects().getCurrentProcessHandle();
			for (WinDebugTarget target : controller.getTargets())
				if (target.getProcessHandle() == currProcess)
					target.exitProcess(exitCode);
		} catch (HRESULTException e) {
			e.printStackTrace();
		}
		return DebugStatus.NO_CHANGE;
	}
	
}
