/**********************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.windows.debug.core.sdk;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * @author Doug Schaefer
 *
 */
public class DebugProcess extends Process {

	private final long processHandle;
	private final OutputStream childStdin;
	private final InputStream childStdout;
	private final InputStream childStderr;
	
	private final Thread createrThread;
	private boolean created = false;
	private int exitCode = STILL_ACTIVE;
	
	private final List<IDebugEventListener> listeners = new LinkedList<IDebugEventListener>();
	
	public DebugProcess(String app, String[] args, String dir, Properties env) {
		long[] handles = new long[4];
		created = Win32Debug.CreateProcess(app, null, null, handles);
		processHandle = handles[0];
		childStdin = new DebugOutputStream(handles[1]);
		childStdout = new DebugInputStream(handles[2]);
		childStderr = new DebugInputStream(handles[3]);
		createrThread = Thread.currentThread();
	}

	public void destroy() {
		Win32Debug.TerminateProcess(processHandle, 0);
	}

	private static final int STILL_ACTIVE = 259;
	
	public int exitValue() {
		if (!created)
			return exitCode;

		if (exitCode == STILL_ACTIVE)
			throw new IllegalThreadStateException();
		
		return exitCode;
	}

	public InputStream getErrorStream() {
		return childStderr;
	}

	public InputStream getInputStream() {
		return childStdout;
	}

	public OutputStream getOutputStream() {
		return childStdin;
	}

	public int waitFor() throws InterruptedException {
		if (!created)
			return 0;
		
		if (Win32Debug.WaitForSingleObject(processHandle, Win32Debug.INFINITE) != Win32Debug.WAIT_OBJECT_0)
			return -1;
		
		created = false;
		
		return exitValue();
	}

	public void addListener(IDebugEventListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(IDebugEventListener listener) {
		listeners.remove(listener);
	}
	
	public void eventLoop() {
		if (createrThread != Thread.currentThread())
			throw new IllegalArgumentException();
		
		DebugEvent debugEvent = new DebugEvent();
		
		while (Win32Debug.WaitForDebugEvent(debugEvent, Win32Debug.INFINITE)) {
			boolean handled = false;
			Iterator<IDebugEventListener> i = listeners.iterator();
			while (i.hasNext()) {
				if (i.next().handleEvent(debugEvent))
					handled = true;
			}
			
			// My own handling
			int continueStatus = Win32Debug.DBG_CONTINUE;
			
			switch (debugEvent.getDebugEventCode()) {
			case DebugEvent.EXIT_PROCESS_DEBUG_EVENT:
				exitCode = debugEvent.getExitProcessDebugInfo().getExitCode();
				return;
			case DebugEvent.EXCEPTION_DEBUG_EVENT:
				if (!handled)
					continueStatus = Win32Debug.DBG_EXCEPTION_NOT_HANDLED;
				break;
			}
			
			// TODO - If thread suspended, don't call this
			Win32Debug.ContinueDebugEvent(debugEvent.getProcessId(), debugEvent.getThreadId(), continueStatus);
		}
	}
}
