/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.windows.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorage;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorageDescriptor;
import org.eclipse.cdt.windows.debug.core.DebugStackFrame;
import org.eclipse.cdt.windows.debug.core.HRESULT;
import org.eclipse.cdt.windows.debug.core.engine.DebugEngine;
import org.eclipse.cdt.windows.debug.core.engine.GetStackFramesCommand;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDIThread implements ICDIThread {

	private final WinCDITarget target;
	
	private WinCDIStackFrame[] stackFrames = null;
	private boolean stackFramesRequested = false;
	private Object stackFramesMutex = new Object();

	public WinCDIThread(WinCDITarget target) {
		this.target = target;
	}
	
	public ICDIThreadStorage createThreadStorage(
			ICDIThreadStorageDescriptor varDesc) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean equals(ICDIThread thread) {
		return super.equals(thread);
	}

	private void requestStackFrames() {
		if (stackFramesRequested)
			return;
		stackFramesRequested = true;
		target.getDebugEngine().scheduleCommand(
				new GetStackFramesCommand(new DebugStackFrame[10]) {
					@Override
					public int run(DebugEngine engine) {
						int hr = super.run(engine);
						if (HRESULT.FAILED(hr))
							return hr;
						int n = 0;
						while (n < frames.length)
							if (frames[n] == null)
								break;
							else
								++n;
						synchronized (stackFramesMutex) {
							stackFrames = new WinCDIStackFrame[n - 1];
							// The last stack frame has offset 0
							for (int i = 0; i < n - 1; ++i)
								stackFrames[i] = new WinCDIStackFrame(target, WinCDIThread.this,
										engine, frames[i]);
							stackFramesRequested = false;
							stackFramesMutex.notifyAll();
						}
						return HRESULT.S_OK;
					}
				});
		try {
			stackFramesMutex.wait();
		} catch (InterruptedException e) {
		}
	}
	
	public int getStackFrameCount() throws CDIException {
		synchronized (stackFramesMutex) {
			requestStackFrames();
			return stackFrames.length;
		}
	}

	public ICDIStackFrame[] getStackFrames() throws CDIException {
		synchronized (stackFramesMutex) {
			return stackFrames;
		}
	}

	public ICDIStackFrame[] getStackFrames(int fromIndex, int toIndex)
			throws CDIException {
		return stackFrames;
	}

	public ICDIThreadStorageDescriptor[] getThreadStorageDescriptors()
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public void jump(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void resume() throws CDIException {
		target.resume();
	}

	public void runUntil(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void signal() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void signal(ICDISignal signal) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepInto() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepIntoInstruction() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepOver() throws CDIException {
		target.stepOver();
	}

	public void stepOverInstruction() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepReturn() throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepInto(int count) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepIntoInstruction(int count) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepOver(int count) throws CDIException {
		target.stepOver(count);
	}

	public void stepOverInstruction(int count) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void stepUntil(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub

	}

	public void resume(boolean passSignal) throws CDIException {
		target.resume(passSignal);
	}

	public void resume(ICDILocation location) throws CDIException {
		target.resume(location);
	}

	public void resume(ICDISignal signal) throws CDIException {
		target.resume(signal);
	}

	public boolean isSuspended() {
		// TODO Auto-generated method stub
		return false;
	}

	public void suspend() throws CDIException {
		// TODO Auto-generated method stub

	}

	public ICDITarget getTarget() {
		return target;
	}

}
