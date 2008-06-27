package org.eclipse.cdt.msw.debug.core.model;

import org.eclipse.cdt.msw.debug.core.Activator;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IStackFrame getTopStackFrame() throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasStackFrames() throws DebugException {
		// TODO Auto-generated method stub
		return false;
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

	public void exitThread(int exitCode) {
		terminated = true;
		target.removeThread(this);
		fireTerminateEvent();
	}
	
}
