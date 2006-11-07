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

package org.eclipse.cdt.windows.debug.core.engine;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.windows.debug.core.Activator;
import org.eclipse.cdt.windows.debug.core.DebugInt;
import org.eclipse.cdt.windows.debug.core.DebugStackFrame;
import org.eclipse.cdt.windows.debug.core.HRESULT;
import org.eclipse.cdt.windows.debug.core.IDebugClient;
import org.eclipse.cdt.windows.debug.core.IDebugControl;
import org.eclipse.cdt.windows.debug.core.IDebugRegisters;
import org.eclipse.cdt.windows.debug.core.IDebugSymbols;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Doug Schaefer
 *
 * This class implements the debug control loop for a Windows
 * debug session.
 */
public class DebugEngine extends Job {

	private IDebugClient debugClient;
	private IDebugControl debugControl;
	private IDebugSymbols debugSymbols;
	private IDebugRegisters debugRegisters;

	private final List<IDebugListener> listeners = new LinkedList<IDebugListener>();
	
	private final List<DebugEvent> eventQueue = new LinkedList<DebugEvent>();
	private final List<DebugCommand> commandQueue = new LinkedList<DebugCommand>();
	
	private boolean processRunning = false;
	
	private static DebugEngine singleton;
	
	private final boolean debug;
	
	public static DebugEngine get() {
		if (singleton == null) {
			singleton = new DebugEngine();
			singleton.setSystem(true); // hide me
			singleton.schedule();
		}
		return singleton;
	}
	
	private DebugEngine() {
		super("Windows Debugger");
		debug = Activator.getDefault().isDebugging();
	}
	
	public IDebugClient getDebugClient() {
		return debugClient;
	}
	
	public IDebugControl getDebugControl() {
		return debugControl;
	}
	
	public IDebugSymbols getDebugSymbols() {
		return debugSymbols;
	}
	
	public IDebugRegisters getDebugRegisters() {
		return debugRegisters;
	}
	
	public boolean isDebug() {
		return debug;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// Set up the debug interface
		try {
			debugClient = IDebugClient.create();
			if (debugClient == null)
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to create client");
			
			debugControl = new IDebugControl();
			if (HRESULT.FAILED(debugClient.createControl(debugControl)))
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to create control");
			if (HRESULT.FAILED(debugClient.setEventCallbacks(new EventCallbacks(this))))
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
				"Failed to register callbacks");
			
			debugSymbols = new IDebugSymbols();
			if (HRESULT.FAILED(debugClient.createSymbols(debugSymbols)))
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to create symbols interface");
			
			debugRegisters = new IDebugRegisters();
			if (HRESULT.FAILED(debugClient.createRegisters(debugRegisters)))
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to create registers interface");
		} catch (CoreException e) {
			return e.getStatus();
		}
		
		// Insert the resume command into the beginning of the queue
		synchronized (commandQueue) {
			commandQueue.add(0, new ResumeCommand());
		}
		
		// Command processing loop
		while (true) {
			// Execute next command
			DebugCommand command = null;
			synchronized (commandQueue) {
				if (commandQueue.isEmpty())
					try {
						if (debug)
							System.out.println("WinDbg waiting");
						commandQueue.wait();
					} catch (InterruptedException e) {
						continue;
					}
				command = commandQueue.remove(0);
			}
			if (debug)
				System.out.println("WinDbg running: " + command);
			int hr = command.run(this);
			processRunning = (hr != HRESULT.E_UNEXPECTED);
			
			// Dispatch any events that have been queued up
			while (true) {
				DebugEvent event;
				synchronized (eventQueue) {
					if (eventQueue.isEmpty())
						break;
					event = eventQueue.remove(0);
				}
				
				if (debug)
					System.out.println("WinDbg dispatching: " + event);

				Iterator<IDebugListener> i = listeners.iterator();
				while (i.hasNext()) {
					i.next().handleDebugEvent(event);
				}
			}
		}
	}
	
	public void addListener(IDebugListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public void removeListener(IDebugListener listener) {
		listeners.remove(listener);
	}
	
	public boolean isProcessRunning() {
		return processRunning;
	}
	
	public void scheduleCommand(DebugCommand command) {
		if (Activator.getDefault().isDebugging())
			System.out.println("WinDbg schedule: " + command);
		// TODO - might need to interrupt the debugger
		synchronized (commandQueue) {
			commandQueue.add(command);
			commandQueue.notifyAll();
		}
	}
	
	public void fireEvent(DebugEvent event) {
		if (Activator.getDefault().isDebugging())
			System.out.println("WinDbg event: " + event);
		synchronized (eventQueue) {
			eventQueue.add(event);
		}
	}

	public int stepReturn(int targetStatus) {
		DebugStackFrame[] frames = new DebugStackFrame[10];
		DebugInt framesFilled = new DebugInt();
		int hr = debugControl.getStrackTrace(0, 0, 0, frames, framesFilled);
		if (HRESULT.FAILED(hr))
			return hr;
		int level = framesFilled.getInt();
		hr = debugControl.setExecutionStatus(IDebugControl.DEBUG_STATUS_STEP_OVER);
		if (HRESULT.FAILED(hr))
			return hr;
		while (framesFilled.getInt() >= level) {
			hr = debugControl.waitForEvent(0, IDebugControl.INFINITE);
			if (HRESULT.FAILED(hr))
				return hr;
			hr = debugControl.getStrackTrace(0, 0, 0, frames, framesFilled);
			if (HRESULT.FAILED(hr))
				return hr;
		}
		debugControl.setExecutionStatus(targetStatus);
		return hr;
	}
	
}
