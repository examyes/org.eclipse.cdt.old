/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.debug.win32.core.dbgeng;

import java.util.LinkedList;

import org.eclipse.cdt.debug.win32.core.cdi.WinDbgProcess;

public class WinDbgEngine extends Thread {
	
	private LinkedList newProcessQueue = new LinkedList();
	private IDebugClient debugClient = new IDebugClient();
	private IDebugControl debugControl = new IDebugControl();
	
	public WinDbgEngine() {
		super("Windows Debugger");
	}
	
	public void newProcess(WinDbgProcess process) {
		synchronized (newProcessQueue) {
			newProcessQueue.addLast(process);
			newProcessQueue.notifyAll();
		}
	}
	
	public static final int S_OK = 0;
	public static final int S_FALSE = 1;
	
	public void run() {
		while (true) {
			// Create new processes if any
			synchronized (newProcessQueue) {
				while (!newProcessQueue.isEmpty()) {
					WinDbgProcess process = (WinDbgProcess)newProcessQueue.removeFirst();
					int flags = DEBUG_PROCESS_CREATE.DEBUG_PROCESS;
					debugClient.createProcess(process.getCommandLine(), flags);
				}
			}

			// Wait for event
			int hr = debugControl.waitForEvent(500);
			
			// If not timeout or ok, and no processes waiting, wait on queue
			if (hr != S_OK && hr != S_FALSE) {
				synchronized (newProcessQueue) {
					if (newProcessQueue.isEmpty())
						try {
							newProcessQueue.wait();
						} catch (InterruptedException e) {
						}
				}
			}
		}
	}

}
