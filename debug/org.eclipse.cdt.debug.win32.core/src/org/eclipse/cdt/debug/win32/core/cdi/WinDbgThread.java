/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.debug.win32.core.cdi;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorage;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorageDescriptor;

public class WinDbgThread implements ICDIThread {

	private int id;
	private long processHandle;
	private long threadHandle;
	private WinDbgTarget target;
	private LinkedList stackFrames = new LinkedList();
	private ICDIStackFrame currentStackFrame;
	
	WinDbgThread(WinDbgTarget target, int id, long processHandle, long threadHandle) {
		this.target = target;
		this.id = id;
		this.processHandle = processHandle;
		this.threadHandle = threadHandle;
	}

	long getProcessHandle() {
		return processHandle;
	}
	
	public String toString() {
		return "ThreadID: " + id; //$NON-NLS-1$
	}
	
	// Native interface to create stack frames
	private static native void initNative();
	static {
		initNative();
	}
	private native void populateStackFrames();
	
	private void createStackFrame(long address) {
		BigInteger big = new BigInteger(Long.toString(address));
		stackFrames.addFirst(new WinDbgStackFrame(this, new WinDbgLocator(null, null, 0, big), 0, 0));
	}
	
	private void createStackFrame(String file, String function, int line, long pc, long frame) {
		String dir = target.getDir();
		String realfile = file;
		if (file.startsWith(dir)) {
			// remove the dir name
			realfile = file.substring(dir.length() + 1);
		}
		stackFrames.addFirst(new WinDbgStackFrame(this, new WinDbgLocator(realfile, function, line, null), pc, frame));
	}
	// Called by target on resume to clear stack frame
	void clearStackFrames() {
		stackFrames.clear();
	}
	
	public ICDIStackFrame[] getStackFrames() throws CDIException {
		int n = getStackFrameCount();
		ICDIStackFrame[] sf = new ICDIStackFrame[n];
		
		Iterator i = stackFrames.iterator();
		while (i.hasNext()) {
			--n;
			WinDbgStackFrame frame = (WinDbgStackFrame)i.next();
			frame.setLevel(n);
			sf[n] = frame;
		}
		return sf;
	}

	public ICDIStackFrame[] getStackFrames(int lowFrame, int highFrame)
			throws CDIException {
		// TODO this is a cheat obviously...
		return getStackFrames();
	}

	public int getStackFrameCount() throws CDIException {
		if (stackFrames.isEmpty())
			populateStackFrames();
		return stackFrames.size();
	}

	public void setCurrentStackFrame(ICDIStackFrame current)
			throws CDIException {
		currentStackFrame = current;
	}

	public void setCurrentStackFrame(ICDIStackFrame frame, boolean doUpdate)
			throws CDIException {
		currentStackFrame = frame;
		// What do you mean update?
	}

	public ICDIStackFrame getCurrentStackFrame() throws CDIException {
		return currentStackFrame;
	}

	public boolean isSuspended() {
		return false;
	}

	public void resume() throws CDIException {
		// Everything is done at the target level
		getTarget().resume(false);
	}

	public void suspend() throws CDIException {
	}

	public void stepReturn() throws CDIException {
	}

	public void stepReturn(boolean execute) throws CDIException {
	}

	public void stepOver() throws CDIException {
		stepOver(1);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepOver(int)
	 */
	public void stepOver(int count) throws CDIException {
		// TODO Auto-generated method stub
	}


	public void stepInto() throws CDIException {
		stepInto(1);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepInto(int)
	 */
	public void stepInto(int count) throws CDIException {
		// TODO Auto-generated method stub
	}


	public void stepOverInstruction() throws CDIException {
		stepOverInstruction(1);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepOverInstruction(int)
	 */
	public void stepOverInstruction(int count) throws CDIException {
		// TODO Auto-generated method stub
	}


	public void stepIntoInstruction() throws CDIException {
		stepIntoInstruction(1);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepIntoInstruction(int)
	 */
	public void stepIntoInstruction(int count) throws CDIException {
		// TODO Auto-generated method stub
	}

	public void runUntil(ICDILocation location) throws CDIException {
		stepUntil(location);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepUntil(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public void stepUntil(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub
	}


	public void jump(ICDILocation location) throws CDIException {
		resume(location);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public void resume(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub
	}

	public void signal() throws CDIException {
		resume(false);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(boolean)
	 */
	public void resume(boolean passSignal) throws CDIException {
		// TODO Auto-generated method stub
	}

	public void signal(ICDISignal signal) throws CDIException {
		resume(signal);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(org.eclipse.cdt.debug.core.cdi.model.ICDISignal)
	 */
	public void resume(ICDISignal signal) throws CDIException {
		// TODO Auto-generated method stub
	}


	public boolean equals(ICDIThread thread) {
		if (thread instanceof WinDbgThread) {
			WinDbgThread wThread = (WinDbgThread)thread;
			return id == wThread.id;
		}
		return false;
	}

	public ICDITarget getTarget() {
		return target;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#getThreadStorageDescriptors()
	 */
	public ICDIThreadStorageDescriptor[] getThreadStorageDescriptors() throws CDIException {
		return null;
	}

	public ICDIThreadStorage createThreadStorage(ICDIThreadStorageDescriptor varDesc) throws CDIException {
		WinDbgVariableManager varMgr = ((WinDbgSession)getTarget().getSession()).getVariableManager();
		return varMgr.createThreadStorage(varDesc);
	}


}
