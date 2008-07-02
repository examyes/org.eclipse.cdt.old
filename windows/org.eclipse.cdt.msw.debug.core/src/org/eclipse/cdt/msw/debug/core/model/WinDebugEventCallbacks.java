package org.eclipse.cdt.msw.debug.core.model;

import org.eclipse.cdt.msw.debug.core.controller.WinDebugController;
import org.eclipse.cdt.msw.debug.dbgeng.DebugEvent;
import org.eclipse.cdt.msw.debug.dbgeng.DebugStatus;
import org.eclipse.cdt.msw.debug.dbgeng.HRESULTException;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugBreakpoint;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugEventCallbacks;
import org.eclipse.debug.core.ILaunch;

public class WinDebugEventCallbacks extends IDebugEventCallbacks {

	private ILaunch currentLaunch;
	
	public WinDebugEventCallbacks() {
	}

	@Override
	protected int getInterestMask() {
		return DebugEvent.CREATE_PROCESS
			 | DebugEvent.EXIT_PROCESS
			 | DebugEvent.CREATE_THREAD
			 | DebugEvent.EXIT_THREAD
			 | DebugEvent.BREAKPOINT;
	}
	
	public void setCurrentLaunch(ILaunch launch) {
		currentLaunch = launch;
	}
	
	private WinDebugTarget getCurrentTarget() throws HRESULTException {
		WinDebugController controller = WinDebugController.getController(); 
		long currProcess = controller.getDebugSystemObjects().getCurrentProcessHandle();
		for (WinDebugTarget target : controller.getTargets())
			if (target.getProcessHandle() == currProcess)
				return target;
		return null;
	}
	
	@Override
	protected int createProcess(long imageFileHandle, long handle,
			long baseOffset, int moduleSize, String moduleName,
			String imageName, int checkSum, int timeDateStamp,
			long initialThreadHandle, long threadDataOffset, long startOffset) {
		WinProcess process = new WinProcess(imageName, currentLaunch, handle);
		currentLaunch.addProcess(process);
		
		WinDebugTarget target = new WinDebugTarget("Windows Debugger", currentLaunch, process);
		currentLaunch.addDebugTarget(target);

		int initialThreadId = 0;
		try {
			initialThreadId = WinDebugController.getController().getDebugSystemObjects().getThreadIdByHandle(initialThreadHandle);
		} catch (HRESULTException e) {
			e.printStackTrace();
		}
		new WinDebugThread(target, initialThreadHandle, initialThreadId);
		
		// Break to set breakpoints
		return DebugStatus.BREAK;
	}
	
	@Override
	protected int exitProcess(int exitCode) {
		try {
			WinDebugTarget target = getCurrentTarget();
			if (target != null)
				target.exitProcess(exitCode);
		} catch (HRESULTException e) {
			e.printStackTrace();
		}
		return DebugStatus.NO_CHANGE;
	}
	
	@Override
	protected int createThread(long handle, long dataOffset, long startOffset) {
		try {
			WinDebugTarget target = getCurrentTarget();
			int id = WinDebugController.getController().getDebugSystemObjects().getThreadIdByHandle(handle);
			new WinDebugThread(target, handle, id);
		} catch (HRESULTException e) {
			e.printStackTrace();
		}
		return DebugStatus.NO_CHANGE;
	}

	@Override
	protected int exitThread(int exitCode) {
		try {
			WinDebugTarget target = getCurrentTarget();
			long threadHandle = WinDebugController.getController().getDebugSystemObjects().getCurrentThreadHandle();
			WinDebugThread thread = target.getThread(threadHandle);
			thread.exitThread(exitCode);
		} catch (HRESULTException e) {
			e.printStackTrace();
		}
		return DebugStatus.NO_CHANGE;
	}
	
	
	@Override
	protected int breakpoint(IDebugBreakpoint breakpoint) {
		WinDebugController.getController().go(false);
		try {
			getCurrentTarget().suspended(true, org.eclipse.debug.core.DebugEvent.BREAKPOINT);
		} catch (HRESULTException e) {
			e.printStackTrace();
		}
		return DebugStatus.BREAK;
	}
	
}
