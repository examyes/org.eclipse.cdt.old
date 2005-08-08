/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.debug.win32.core.cdi;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.win32.core.Win32Debugger;
import org.eclipse.cdt.debug.win32.core.dbgeng.DEBUG_PROCESS_CREATE;
import org.eclipse.cdt.debug.win32.core.dbgeng.IDebugClient;
import org.eclipse.cdt.debug.win32.core.dbgeng.IDebugControl;
import org.eclipse.cdt.debug.win32.core.dbgeng.WinDbgOutputCallbacks;
import org.eclipse.debug.core.ILaunch;

public class WinDbgProcess extends Process {

	private String commandLine;
	private WinDbgOutputStream stdin = new WinDbgOutputStream();
	private WinDbgInputStream stdout = new WinDbgInputStream();
	private WinDbgInputStream stderr = new WinDbgInputStream();
	private WinDbgOutputCallbacks outputCallbacks;
	boolean terminated = false;
	int exitValue;
	
	public WinDbgProcess(ILaunch launch, IBinaryObject exe)
	{
		commandLine = exe.getPath().toOSString();
		outputCallbacks = new WinDbgOutputCallbacks(stdout, stderr);
	}

	public String getCommandLine() {
		return commandLine;
	}
	
	public synchronized int exitValue() {
		if (terminated == false)
			throw new IllegalThreadStateException();
		return exitValue;
	}

	public synchronized int waitFor() throws InterruptedException {
		try {
			wait();
		} catch (InterruptedException e) {
		}
		return exitValue;
	}

	public void destroy() {
		// TODO Auto-generated method stub
	}

	public InputStream getErrorStream() {
		return stderr;
	}

	public InputStream getInputStream() {
		return stdout;
	}

	public OutputStream getOutputStream() {
		return stdin;
	}

	public boolean isTerminated() {
		return terminated;
	}
	
}
