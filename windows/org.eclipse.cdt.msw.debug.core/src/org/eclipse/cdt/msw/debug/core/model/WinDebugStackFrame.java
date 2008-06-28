package org.eclipse.cdt.msw.debug.core.model;

import org.eclipse.cdt.msw.debug.core.Activator;
import org.eclipse.cdt.msw.debug.dbgeng.DebugStackFrame;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;

public class WinDebugStackFrame extends DebugElement implements IStackFrame {

	private final WinDebugThread thread;
	private final DebugStackFrame frame;
	
	public WinDebugStackFrame(WinDebugThread thread, DebugStackFrame frame) {
		super(thread.getDebugTarget());
		this.thread = thread;
		this.frame = frame;
		thread.addStackFrame(this);
		fireCreationEvent();
	}
	
	@Override
	public int getCharEnd() throws DebugException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCharStart() throws DebugException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getLineNumber() throws DebugException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() throws DebugException {
		return "Instruction Offset: 0x" + Long.toHexString(frame.getInstructionOffset());
	}

	@Override
	public IRegisterGroup[] getRegisterGroups() throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IThread getThread() {
		return thread;
	}

	@Override
	public IVariable[] getVariables() throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasRegisterGroups() throws DebugException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasVariables() throws DebugException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getModelIdentifier() {
		return Activator.PLUGIN_ID;
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
	public boolean canResume() {
		return thread.canResume();
	}

	@Override
	public boolean canSuspend() {
		return thread.canSuspend();
	}

	@Override
	public boolean isSuspended() {
		return thread.isSuspended();
	}

	@Override
	public void resume() throws DebugException {
		thread.resume();
	}

	@Override
	public void suspend() throws DebugException {
		thread.suspend();
	}

	@Override
	public boolean canTerminate() {
		return thread.canTerminate();
	}

	@Override
	public boolean isTerminated() {
		return thread.isTerminated();
	}

	@Override
	public void terminate() throws DebugException {
		thread.terminate();
	}

}
