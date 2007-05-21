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
	
	public DebugProcess(String app, String[] args, String dir, Properties env) {
		long[] handles = new long[4];
		created = create(app, "", "", handles);
		processHandle = handles[0];
		childStdin = new DebugOutputStream(handles[1]);
		childStdout = new DebugInputStream(handles[2]);
		childStderr = new DebugInputStream(handles[3]);
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

}
