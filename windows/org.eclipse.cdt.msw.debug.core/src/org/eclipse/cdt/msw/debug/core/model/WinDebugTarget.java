package org.eclipse.cdt.msw.debug.core.model;

import org.eclipse.cdt.msw.debug.core.Activator;
import org.eclipse.cdt.msw.debug.core.controller.WinDebugController;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;

public class WinDebugTarget extends DebugElement implements IDebugTarget {

	private final String name;
	private final ILaunch launch;
	private final WinProcess process;

	private boolean suspended = false;
	private boolean canSuspend = true;
	private boolean terminated = false;
	
	public WinDebugTarget(String name, ILaunch launch, WinProcess process) {
		super(null);
		this.name = name;
		this.launch = launch;
		this.process = process;
		WinDebugController.getController().addTarget(this);
	}

	@Override
	public IDebugTarget getDebugTarget() {
		return this;
	}

	@Override
	public ILaunch getLaunch() {
		return launch;
	}
	
	@Override
	public String getName() throws DebugException {
		return name;
	}

	@Override
	public IProcess getProcess() {
		return process;
	}

	@Override
	public IThread[] getThreads() throws DebugException {
		// TODO Auto-generated method stub
		return new IThread[0];
	}

	@Override
	public boolean hasThreads() throws DebugException {
		return true;
	}

	@Override
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getModelIdentifier() {
		return Activator.PLUGIN_ID;
	}

	@Override
	public void breakpointAdded(IBreakpoint breakpoint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length)
			throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean supportsStorageRetrieval() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canResume() {
		return suspended;
	}

	@Override
	public boolean canSuspend() {
		return canSuspend;
	}

	@Override
	public boolean isSuspended() {
		return suspended;
	}

	@Override
	public void resume() throws DebugException {
		if (suspended) {
			final WinDebugController controller = WinDebugController.getController();
			controller.enqueueCommand(new Runnable() {
				@Override
				public void run() {
					controller.go(true);
					for (WinDebugTarget target : controller.getTargets())
						target.suspended(false, DebugEvent.CLIENT_REQUEST);
				}
			});
		}
	}

	@Override
	public void suspend() throws DebugException {
		if (canSuspend) {
			canSuspend = false;
			final WinDebugController controller = WinDebugController.getController();
			controller.enqueueCommand(new Runnable() {
				@Override
				public void run() {
					controller.go(false);
					for (WinDebugTarget target : controller.getTargets())
						target.suspended(true, DebugEvent.CLIENT_REQUEST);
				}
			});
		}
	}
	
	public void suspended(boolean suspended, int detail) {
		this.suspended = suspended;
		canSuspend = !suspended;
		if (suspended)
			fireSuspendEvent(detail);
		else
			fireResumeEvent(detail);
	}

	@Override
	public boolean canTerminate() {
		return !terminated;
	}

	@Override
	public boolean isTerminated() {
		return terminated;
	}

	@Override
	public void terminate() throws DebugException {
		process.terminate();
	}

	@Override
	public boolean canDisconnect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void disconnect() throws DebugException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDisconnected() {
		// TODO Auto-generated method stub
		return false;
	}

	public void exitProcess(int exitCode) {
		terminated = true;
		process.terminated(exitCode);
		WinDebugController.getController().removeTarget(this);
		fireTerminateEvent();
	}
	
}
