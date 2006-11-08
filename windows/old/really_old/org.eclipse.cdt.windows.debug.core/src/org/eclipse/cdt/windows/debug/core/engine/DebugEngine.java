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
import org.eclipse.cdt.windows.debug.core.DebugCreateProcessOptions;
import org.eclipse.cdt.windows.debug.core.DebugInt;
import org.eclipse.cdt.windows.debug.core.DebugLong;
import org.eclipse.cdt.windows.debug.core.DebugStackFrame;
import org.eclipse.cdt.windows.debug.core.DebugString;
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

	private IDebugClient client;
	private IDebugControl control;
	private IDebugSymbols symbols;
	private IDebugRegisters registers;

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
	
	public IDebugControl getDebugControl() {
		return control;
	}
	
	public IDebugSymbols getDebugSymbols() {
		return symbols;
	}
	
	public IDebugRegisters getDebugRegisters() {
		return registers;
	}
	
	public boolean isDebug() {
		return debug;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// Set up the debug interface
		try {
			client = IDebugClient.create();
			if (client == null)
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to create client");
			if (HRESULT.FAILED(client.setEventCallbacks(new EventCallbacks(this))))
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to register callbacks");
			
			control = new IDebugControl();
			if (HRESULT.FAILED(client.createControl(control)))
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to create control");
			if (HRESULT.FAILED(control.setCodeLevel(IDebugControl.DEBUG_LEVEL_SOURCE)))
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to set code level");
			
			symbols = new IDebugSymbols();
			if (HRESULT.FAILED(client.createSymbols(symbols)))
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to create symbols interface");
			
			registers = new IDebugRegisters();
			if (HRESULT.FAILED(client.createRegisters(registers)))
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

	private void debugDumpStack(DebugStackFrame[] frames, int len) {
		System.out.println("Stack:");
		for (int i = 0; i < len; ++i) {
			DebugString _name = new DebugString();
			symbols.getNameByOffset(frames[i].getInstructionOffset(), _name, null);
			System.out.println("    " + _name.getString());
		}
	}
	
	public int createProcess(String commandLine, String initialDirectory,
			String environment) {
		// Create the process
		DebugCreateProcessOptions options = new DebugCreateProcessOptions();
		options.setCreateFlags(DebugCreateProcessOptions.DEBUG_ONLY_THIS_PROCESS);
		int hr = client.createProcess2(
				0L, commandLine, options,
				initialDirectory, environment);
		if (HRESULT.FAILED(hr) | processRunning)
			return hr;
		
		// Run to create the process
		return control.waitForEvent(0, IDebugControl.INFINITE);
	}
	
	public int resume() {
		int hr;
		if (HRESULT.FAILED(hr = control.setExecutionStatus(IDebugControl.DEBUG_STATUS_GO)))
			return hr;
		return control.waitForEvent(0, IDebugControl.INFINITE);
	}
	
	public int stepReturn() {
		DebugStackFrame[] frames = new DebugStackFrame[10];
		DebugInt framesFilled = new DebugInt();
		int hr = control.getStrackTrace(0, 0, 0, frames, framesFilled);
		if (HRESULT.FAILED(hr))
			return hr;
		int level = framesFilled.getInt();
		if (debug)
			debugDumpStack(frames, level);
		hr = control.setExecutionStatus(IDebugControl.DEBUG_STATUS_STEP_OVER);
		if (HRESULT.FAILED(hr))
			return hr;
		while (framesFilled.getInt() >= level) {
			hr = control.waitForEvent(0, IDebugControl.INFINITE);
			if (HRESULT.FAILED(hr))
				return hr;
			if (HRESULT.FAILED(hr = control.getStrackTrace(0, 0, 0, frames, framesFilled)))
				return hr;
		}
		return hr;
	}

	public int stepInto() {
		return step(IDebugControl.DEBUG_STATUS_STEP_INTO);
	}
	
	public int stepOver() {
		return step(IDebugControl.DEBUG_STATUS_STEP_OVER);
	}

	private int step(int executionStatus) {
		while (true) {
			int hr;
			if (HRESULT.FAILED(hr = control.setExecutionStatus(executionStatus)))
				return hr;
		
			if (HRESULT.FAILED(hr = control.waitForEvent(0, IDebugControl.INFINITE)))
				return hr;

			DebugLong _offset = new DebugLong();
			if (HRESULT.FAILED(hr = registers.getInstructionOffset(_offset)))
				return hr;

			DebugString _file = new DebugString();
			DebugInt _line = new DebugInt();
			if (!HRESULT.FAILED(hr = symbols.getLineByOffset(_offset.getLong(),
					_line, _file, null)))
				// continue loop until we get a source line we know
				return hr;
		}
	}

}
