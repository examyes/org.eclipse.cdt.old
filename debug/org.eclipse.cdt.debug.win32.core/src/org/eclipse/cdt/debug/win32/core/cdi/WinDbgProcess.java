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

import java.io.InputStream;
import java.io.OutputStream;

public class WinDbgProcess extends Process {
	
	private WinDbgOutputStream in = new WinDbgOutputStream(this, 0);
	private WinDbgInputStream out = new WinDbgInputStream(this, 1);
	private WinDbgInputStream err = new WinDbgInputStream(this, 2);

	// The native interface
	private long p;
	private static native void initNative();
	static {
		initNative();
	}
	
	public WinDbgProcess(String cmdline, String dir) {
		spawn(cmdline, dir);
	}

	private native void spawn(String cmdline, String dir);
	
	public int exitValue() {
		int exitCode = exitCode();
		if (exitCode == 259)
			throw new IllegalThreadStateException();
		return exitCode;
	}
	
	private native int exitCode();

	public native int waitFor() throws InterruptedException;
	
	public native void destroy();
	
	public InputStream getErrorStream() {
		return err;
	}

	public InputStream getInputStream() {
		return out;
	}

	public OutputStream getOutputStream() {
		return in;
	}
	
	// read and write byte methods for streams
	native int read(int fd);
	native void write(int fd, int b);
}
