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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.win32.core.cdi.events.BreakpointHit;
import org.eclipse.cdt.debug.win32.core.cdi.events.CreatedEvent;
import org.eclipse.cdt.debug.win32.core.cdi.events.ExitedEvent;
import org.eclipse.cdt.debug.win32.core.cdi.events.ResumedEvent;
import org.eclipse.cdt.debug.win32.core.cdi.events.SuspendedEvent;
import org.eclipse.core.resources.IFile;

public class WinDbgTarget implements ICDITarget, Runnable {
	
	private WinDbgProcess process;
	private WinDbgSession session;
	private WinDbgThread[] threads = new WinDbgThread[0];
	private WinDbgThread currentThread;
	private Thread debugThread;
	String cmdline;
	String dir;

	public WinDbgTarget(WinDbgSession session, IFile exe) {
		this.session = session;
		cmdline = exe.getLocation().toOSString();
		dir = exe.getProject().getLocation().toOSString().toLowerCase();
		debugThread = new Thread(this);
		init(cmdline);
	}

	String getDir() {
		return dir;
	}
	
	// Native interface
	private long p;
	private static native void initNative();
	static {
		initNative();
	}
	private native void init(String cmd);
	
	public synchronized void start() {
		debugThread.start();
		try {
			// Wait for the process to get spawned
			wait();
		} catch (InterruptedException e) {
		}
	}
	
	private boolean resume = false;
	
	// Called by debug thread to wait at an exception/breakpoint
	private synchronized void waitForResume() {
		while (!resume)
			try {
				wait();
			} catch (InterruptedException e) {
			}
		resume= false;
		
		getEventManager().fireEvent(new ICDIEvent[] { new ResumedEvent(this, ICDIResumedEvent.CONTINUE) });
	}

	private WinDbgEventManager getEventManager() {
		return (WinDbgEventManager)session.getEventManager();
	}
	
	//
	// Debug loop
	//
	
	private native void debugLoop();

	public void run() {
		process = new WinDbgProcess(cmdline, dir);

		// Let everyone know the process has been created
		synchronized(this) { notify(); }

		// Off we go
		debugLoop();
	}
	
	// handlers called from debug loop
	private void handleExitProcess(int exitCode) {
		getEventManager().fireEvent(new ICDIEvent[] { new ExitedEvent(this, exitCode) });
	}
	
	//
	// Symbol/Breakpoint support
	//
	
	private native long getFunctionAddress(String function);
	private native long getLineAddress(String file, int lineNumber);
	
	void resolveLocation(ICDILocation location) {
		long address = 0;
		
		if (location.getFunction() != null)
			address = getFunctionAddress(location.getFunction());
		else if (location.getFile() != null)
			address = getLineAddress(dir + "\\" + location.getFile(), location.getLineNumber());
		
		((WinDbgLocation)location).setAddress(address);
	}
	
	native void setBreakpoint(long address, boolean temporary);
	
	public ICDISession getSession() {
		return session;
	}

	private void handleBreakpoint(long address) {
		getEventManager().fireEvent(new ICDIEvent[] {
				new SuspendedEvent(this, new BreakpointHit(session, null))
			});
		waitForResume();
	}
	
	public Process getProcess() {
		return process;
	}

	void createThread(int threadId, long processHandle, long threadHandle) {
		WinDbgThread thread = new WinDbgThread(this, threadId, processHandle, threadHandle);
		WinDbgThread[] newThreads = new WinDbgThread[threads.length + 1];
		System.arraycopy(threads, 0, newThreads, 0, threads.length);
		newThreads[threads.length] = thread;
		threads = newThreads;
		currentThread = thread;
		
		getEventManager().fireEvent(new ICDIEvent[] { new CreatedEvent(thread) });
		// Default is for thread to come up suspended, wait for the resume
		waitForResume();
	}

	public ICDIThread[] getThreads() throws CDIException {
		return threads;
	}

	public void setCurrentThread(ICDIThread current) throws CDIException {
		currentThread = (WinDbgThread)current;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#evaluateExpressionToString(java.lang.String)
	 */
	public String evaluateExpressionToString(String expressionText)
			throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isTerminated()
	 */
	public boolean isTerminated() {
		// TODO Auto-generated method stub
		return false;
	}

	public void terminate() throws CDIException {
		process.destroy();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isDisconnected()
	 */
	public boolean isDisconnected() {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#disconnect()
	 */
	public void disconnect() throws CDIException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#restart()
	 */
	public void restart() throws CDIException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#isSuspended()
	 */
	public boolean isSuspended() {
		// TODO Auto-generated method stub
		return false;
	}

	public synchronized void resume() throws CDIException {
		resume = true;
		// Clear everyone's stack frame
		for (int i = 0; i < threads.length; ++i)
			threads[i].clearStackFrames();
		notify();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#suspend()
	 */
	public void suspend() throws CDIException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepReturn()
	 */
	public void stepReturn() throws CDIException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepReturn(boolean)
	 */
	public void stepReturn(boolean execute) throws CDIException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepOver()
	 */
	public void stepOver() throws CDIException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepInto()
	 */
	public void stepInto() throws CDIException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepOverInstruction()
	 */
	public void stepOverInstruction() throws CDIException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepIntoInstruction()
	 */
	public void stepIntoInstruction() throws CDIException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#runUntil(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public void runUntil(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#jump(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public void jump(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#signal()
	 */
	public void signal() throws CDIException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#signal(org.eclipse.cdt.debug.core.cdi.model.ICDISignal)
	 */
	public void signal(ICDISignal signal) throws CDIException {
		// TODO Auto-generated method stub
	}

	public ICDIThread getCurrentThread() throws CDIException {
		return currentThread;
	}

	public ICDITarget getTarget() {
		return this;
	}
}
