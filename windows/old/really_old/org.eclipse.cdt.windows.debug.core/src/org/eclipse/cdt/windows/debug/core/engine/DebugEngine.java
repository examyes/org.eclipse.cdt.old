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
import java.util.Map;

import org.eclipse.cdt.windows.debug.core.Activator;
import org.eclipse.cdt.windows.debug.core.DebugCreateProcessOptions;
import org.eclipse.cdt.windows.debug.core.HRESULT;
import org.eclipse.cdt.windows.debug.core.IDebugClient;
import org.eclipse.cdt.windows.debug.core.IDebugControl;
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

	private final String commandLine;
	private final String initialDirectory;
	private final Map<String, String> environment;
	
	private final List<IDebugListener> listeners = new LinkedList<IDebugListener>();
	
	private final List<DebugEvent> eventQueue = new LinkedList<DebugEvent>();
	private final List<DebugCommand> commandQueue = new LinkedList<DebugCommand>();
	
	private boolean processStarted = false;
	private boolean go = false;
	
	public DebugEngine(String commandLine,
			String initialDirectory,
			Map<String, String> environment) throws CoreException {
		super("Windows Debug Engine");
		this.commandLine = commandLine;
		this.initialDirectory = initialDirectory;
		this.environment = environment;
	}
	
	public IDebugClient getDebugClient() {
		return debugClient;
	}
	
	public IDebugControl getDebugControl() {
		return debugControl;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			debugClient = new IDebugClient();
			debugControl = new IDebugControl();
			
			debugClient.setEventCallbacks(new EventCallbacks(this));
		} catch (CoreException e) {
			return e.getStatus();
		}
		DebugCreateProcessOptions options = new DebugCreateProcessOptions();
		options.setCreateFlags(DebugCreateProcessOptions.DEBUG_ONLY_THIS_PROCESS);

		if (HRESULT.FAILED(debugClient.createProcess2(
				0, commandLine, options, initialDirectory, environment)))
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to launch: " + commandLine);

		// my internal command queue
		List<DebugCommand> commands = new LinkedList<DebugCommand>();
		
		// Event loop
		while (true) {
			int hr = debugControl.waitForEvent(0, IDebugControl.INFINITE);
			if (hr == HRESULT.E_UNEXPECTED)
				break;
			if (HRESULT.FAILED(hr))
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						"Failed waiting for event");
			
			// Dispatch any events that have been queued up in the callbacks
			synchronized (eventQueue) {
				while (eventQueue.size() > 0) {
					DebugEvent debugEvent = eventQueue.remove(0);
					Iterator<IDebugListener> i = listeners.iterator();
					while (i.hasNext()) {
						i.next().handleDebugEvent(debugEvent);
					}
				}
			}
			
			// Execute any queued up commands
			synchronized (this) {
				if (!commandQueue.isEmpty()) {
					commands.addAll(commandQueue);
					commandQueue.clear();
				}
			}
			while (!commands.isEmpty()) {
				DebugCommand command = commands.remove(0);
				command.run(this);
			}
			
			// Wait for a continue command
			waitForGo();
		}

		return Status.OK_STATUS;
	}
	
	private synchronized void waitForGo() {
		if (!go)
			try {
				wait();
			} catch (InterruptedException e) {
			}
		go = false;
	}
	
	synchronized void go() {
		go = true;
		notifyAll();
	}
	
	synchronized void processStarted() {
		processStarted = true;
	}
	
	public void addListener(IDebugListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public void removeListener(IDebugListener listener) {
		listeners.remove(listener);
	}
	
	public void scheduleCommand(DebugCommand command) {
		synchronized (this) {
			if (!processStarted) {
				// queue up the command
				commandQueue.add(command);
				return;
			}
		}

		// run it
		command.run(this);
	}
	
}
