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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
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
		BigInteger address = BigInteger.ZERO;
		
		if (location.getFunction() != null) {
			long addr = getFunctionAddress(location.getFunction());
			address = new BigInteger(Long.toString(addr));
		} else if (location.getFile() != null) {
			long addr = getLineAddress(dir + "\\" + location.getFile(), location.getLineNumber());
			address = new BigInteger(Long.toString(addr));
		}
		
		((WinDbgLocation)location).setAddress(address);
	}
	
	native void setBreakpoint(long address, boolean temporary);
	
	void setBreakpoint(BigInteger address, boolean temporary) {
		setBreakpoint(address.longValue(), temporary);
	}

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

	public synchronized void resume(boolean passSignal) throws CDIException {
		resume = true;
		// Clear everyone's stack frame
		for (int i = 0; i < threads.length; ++i)
			threads[i].clearStackFrames();
		notify();
	}
	public synchronized void resume() throws CDIException {
		resume(false);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#signal()
	 */
	public void signal() throws CDIException {
		resume(false);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(boolean)
	 */
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#jump(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public void jump(ICDILocation location) throws CDIException {
		resume(location);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public void resume(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#signal(org.eclipse.cdt.debug.core.cdi.model.ICDISignal)
	 */
	public void signal(ICDISignal signal) throws CDIException {
		resume(signal);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(org.eclipse.cdt.debug.core.cdi.model.ICDISignal)
	 */
	public void resume(ICDISignal signal) throws CDIException {
		// TODO Auto-generated method stub
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
		getTarget().getCurrentThread().getCurrentStackFrame().stepReturn();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepOver()
	 */
	public void stepOver() throws CDIException {
		stepOver(1);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepOver(int)
	 */
	public void stepOver(int count) throws CDIException {
		// TODO Auto-generated method stub
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepInto()
	 */
	public void stepInto() throws CDIException {
		stepInto(1);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepInto(int)
	 */
	public void stepInto(int count) throws CDIException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepOverInstruction()
	 */
	public void stepOverInstruction() throws CDIException {
		stepOverInstruction(1);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepOverInstruction(int)
	 */
	public void stepOverInstruction(int count) throws CDIException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#stepIntoInstruction()
	 */
	public void stepIntoInstruction() throws CDIException {
		stepIntoInstruction(1);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepIntoInstruction(int)
	 */
	public void stepIntoInstruction(int count) throws CDIException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#runUntil(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public void runUntil(ICDILocation location) throws CDIException {
		stepUntil(location);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepUntil(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public void stepUntil(ICDILocation location) throws CDIException {
		// TODO Auto-generated method stub
	}


	public ICDIThread getCurrentThread() throws CDIException {
		return currentThread;
	}

	public ICDITarget getTarget() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#getBreakpoints()
	 */
	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#setLocationBreakpoint(int, org.eclipse.cdt.debug.core.cdi.ICDILocation, org.eclipse.cdt.debug.core.cdi.ICDICondition, boolean)
	 */
	public ICDILocationBreakpoint setLocationBreakpoint(int type, ICDILocation location, ICDICondition condition, boolean deferred) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#setWatchpoint(int, int, java.lang.String, org.eclipse.cdt.debug.core.cdi.ICDICondition)
	 */
	public ICDIWatchpoint setWatchpoint(int type, int watchType, String expression, ICDICondition condition) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#deleteBreakpoints(org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint[])
	 */
	public void deleteBreakpoints(ICDIBreakpoint[] breakpoints) throws CDIException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#deleteAllBreakpoints()
	 */
	public void deleteAllBreakpoints() throws CDIException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createCondition(int, java.lang.String)
	 */
	public ICDICondition createCondition(int ignoreCount, String expression) {
		return createCondition(ignoreCount, expression, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createCondition(int, java.lang.String, java.lang.String[])
	 */
	public ICDICondition createCondition(int ignoreCount, String expression, String[] threadIds) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createLocation(java.lang.String, java.lang.String, int)
	 */
	public ICDILocation createLocation(String file, String function, int line) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDITarget#createLocation(long)
	 */
	public ICDILocation createLocation(BigInteger address) {
		// TODO Auto-generated method stub
		return null;
	}


}
