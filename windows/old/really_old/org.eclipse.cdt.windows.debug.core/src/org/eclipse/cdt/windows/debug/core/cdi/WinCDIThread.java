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
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorage;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorageDescriptor;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDIThread implements ICDIThread {

	private final WinCDITarget target;
	
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

	public int getStackFrameCount() throws CDIException {
		// TODO Auto-generated method stub
		return 0;
	}

	public ICDIStackFrame[] getStackFrames() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIStackFrame[] getStackFrames(int fromIndex, int len)
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

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
		return true;
	}

	public void suspend() throws CDIException {
		// TODO Auto-generated method stub

	}

	public ICDITarget getTarget() {
		return target;
	}

}
