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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Doug Schaefer
 *
 */
public class WinProcess extends Process {

	private final Object waitMutex = new Object();
	private boolean running = true;
	private int exitValue;
	
	public WinProcess() {
		// TODO Auto-generated constructor stub
	}

	public void terminated(int exitValue) {
		synchronized (waitMutex) {
			this.exitValue = exitValue;
			this.running = false;
			waitMutex.notifyAll();
		}
	}

	public boolean isTerminated() {
		return !running;
	}
	
	@Override
	public void destroy() {
		// TODO kill the real process
	}

	@Override
	public int exitValue() {
		synchronized (waitMutex) {
			if (running)
				throw new IllegalThreadStateException();
		}
		return exitValue;
	}

	@Override
	public InputStream getErrorStream() {
		return new ByteArrayInputStream(new byte[0]);
	}

	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(new byte[0]);
	}

	@Override
	public OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
	}

	@Override
	public int waitFor() throws InterruptedException {
		synchronized (waitMutex) {
			waitMutex.wait();
		}
		// TODO exit code
		return exitValue;
	}

}
