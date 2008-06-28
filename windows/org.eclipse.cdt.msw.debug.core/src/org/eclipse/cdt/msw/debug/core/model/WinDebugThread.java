package org.eclipse.cdt.msw.debug.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.msw.debug.core.Activator;
import org.eclipse.cdt.msw.debug.core.controller.WinDebugController;
import org.eclipse.cdt.msw.debug.dbgeng.DebugStackFrame;
import org.eclipse.cdt.msw.debug.dbgeng.HRESULTException;
import org.eclipse.cdt.msw.debug.dbgeng.IDebugControl;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;

public class WinDebugThread extends DebugElement implements IThread {

	private final WinDebugTarget target;
	private final long handle;
	private final int id;
	
	private boolean terminated = false;
	private List<WinDebugStackFrame> stackFrames;
	
	public WinDebugThread(WinDebugTarget target, long handle, int id) {
		super(target);
		this.target = target;
		this.handle = handle;
		this.id = id;
		target.addThread(this);
		fireCreationEvent();
	}
	
	public long getHandle() {
		return handle;
	}
	
	@Override
	public IBreakpoint[] getBreakpoints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() throws DebugException {
		return "Thread " + id;
	}

	@Override
	public int getPriority() throws DebugException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IStackFrame[] getStackFrames() throws DebugException {
		if (!target.isSuspended())
			return new IStackFrame[0];
		buildStackFrames();
		return stackFrames.toArray(new IStackFrame[stackFrames.size()]);
	}

	@Override
	public IStackFrame getTopStackFrame() throws DebugException {
		if (!target.isSuspended())
			return null;
		buildStackFrames();
		return stackFrames.isEmpty() ? null : stackFrames.get(0);
	}

	private synchronized void buildStackFrames() {
		if (stackFrames != null)
			return;
		stackFrames = new ArrayList<WinDebugStackFrame>();
		final WinDebugController controller = WinDebugController.getController();
		synchronized (stackFrames) {
			controller.enqueueCommand(new Runnable() {
				@Override
				public void run() {
					IDebugControl control = controller.getDebugControl();
					try {
						DebugStackFrame[] frames = control.getStackTrace(0, 0, 0);
						for (DebugStackFrame frame : frames)
							new WinDebugStackFrame(WinDebugThread.this, frame);
					} catch (HRESULTException e) {
						e.printStackTrace();
					}
					synchronized (stackFrames) {
						stackFrames.notify();
					}					
				}
			});
			try {
				stackFrames.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean hasStackFrames() throws DebugException {
		if (!target.isSuspended())
			// Can't show stack frame unless we're suspended 
			return false;
		buildStackFrames();
		return !stackFrames.isEmpty();
	}

	void addStackFrame(WinDebugStackFrame stackFrame) {
		stackFrames.add(stackFrame);
	}
	
	@Override
	public String getModelIdentifier() {
		return Activator.PLUGIN_ID;
	}

	@Override
	public boolean canResume() {
		return target.canResume();
	}

	@Override
	public boolean canSuspend() {
		return target.canSuspend();
	}

	@Override
	public boolean isSuspended() {
		return target.isSuspended();
	}

	@Override
	public void resume() throws DebugException {
		target.resume();
	}

	@Override
	public void suspend() throws DebugException {
		target.suspend();
	}

	@Override
	public boolean canStepInto() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canStepOver() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canStepReturn() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStepping() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stepInto() throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stepOver() throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stepReturn() throws DebugException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canTerminate() {
		return target.canTerminate();
	}

	@Override
	public boolean isTerminated() {
		return terminated;
	}

	@Override
	public void terminate() throws DebugException {
		// TODO do we want to be able to terminate individual threads?
		target.terminate();
	}

	public void suspended(boolean suspended, int detail) {
		if (!suspended)
			stackFrames = null;
	}
	
	public void exitThread(int exitCode) {
		terminated = true;
		target.removeThread(this);
		fireTerminateEvent();
	}
	
}
