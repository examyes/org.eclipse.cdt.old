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

	private final boolean created;
	
	private final long processHandle;
	private final OutputStream childStdin;
	private final InputStream childStdout;
	private final InputStream childStderr;
	
	private final Thread createrThread;
	
	private final List<IDebugEventListener> listeners = new LinkedList<IDebugEventListener>();
	
	public DebugProcess(String app, String[] args, String dir, Properties env) {
		long[] handles = new long[4];
//		created = create(app, "", "", handles);
		created = true;
		processHandle = handles[0];
		childStdin = new DebugOutputStream(handles[1]);
		childStdout = new DebugInputStream(handles[2]);
		childStderr = new DebugInputStream(handles[3]);
		createrThread = Thread.currentThread();
	}

	public static native boolean create(String cmdline, String envp, String dir, long[] handles);
	
	public void destroy() {
		// TODO Auto-generated method stub
	}

	private static final int STILL_ACTIVE = 259;
	
	public int exitValue() {
		if (!created)
			return -1;

		int[] exitCode = new int[1];
		if (!GetExitCodeProcess(processHandle, exitCode))
			return -1;
		
		if (exitCode[0] == STILL_ACTIVE)
			throw new IllegalThreadStateException();
		
		return exitCode[0];
	}

	private static native boolean GetExitCodeProcess(long processHandle, int[] exitCode);
	
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
			return -1;
		// TODO Auto-generated method stub
		return 0;
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
		
//		while (Win32Debug.WaitForDebugEvent(debugEvent, Win32Debug.INFINITE)) {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			Iterator<IDebugEventListener> i = listeners.iterator();
			while (i.hasNext()) {
				i.next().handleEvent(debugEvent);
			}
		}
	}
}
