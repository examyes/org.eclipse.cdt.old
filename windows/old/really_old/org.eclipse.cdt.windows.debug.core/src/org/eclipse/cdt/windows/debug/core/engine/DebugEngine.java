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

	static private IDebugClient masterClient;
	static {
		masterClient = IDebugClient.create();
		if (masterClient == null)
			Activator.getDefault().getLog().log(new Status(
					IStatus.ERROR, Activator.PLUGIN_ID, "Failed to create master client"));
	}
	
	private IDebugClient debugClient = new IDebugClient();
	private IDebugControl debugControl = new IDebugControl();

	private final String commandLine;
	private final String initialDirectory;
	private final Map<String, String> environment;
	
	private final List<IDebugListener> listeners = new LinkedList<IDebugListener>();
	
	private final List<DebugEvent> eventQueue = new LinkedList<DebugEvent>();
	private final List<DebugCommand> commandQueue = new LinkedList<DebugCommand>();
	
	private boolean processRunning = true;
	
	public DebugEngine(String commandLine,
			String initialDirectory,
			Map<String, String> environment) throws CoreException {
		super("Windows Debug Engine");
		this.commandLine = commandLine;
		this.initialDirectory = initialDirectory;
		this.environment = environment;
	}
	
	private String getEnvironmentString() {
		// TODO - walk the map and create the string
		return null;
	};
	
	public IDebugClient getDebugClient() {
		return debugClient;
	}
	
	public IDebugControl getDebugControl() {
		return debugControl;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// Set up the debug interface
		try {
			if (HRESULT.FAILED(masterClient.createClient(debugClient)))
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to create client");
			if (HRESULT.FAILED(debugClient.createControl(debugControl)))
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to create control");
			if (HRESULT.FAILED(debugClient.setEventCallbacks(new EventCallbacks(this))))
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to register callbacks");
		} catch (CoreException e) {
			return e.getStatus();
		}
		
		// Create the process
		DebugCreateProcessOptions options = new DebugCreateProcessOptions();
		options.setCreateFlags(DebugCreateProcessOptions.DEBUG_ONLY_THIS_PROCESS);
		if (HRESULT.FAILED(debugClient.createProcess2(
				0L, commandLine, options, initialDirectory, getEnvironmentString())))
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to launch: " + commandLine);

		// Insert the resume command into the beginning of the queue
		synchronized (commandQueue) {
			commandQueue.add(0, new ResumeCommand());
		}
		
		// Command processing loop
		while (processRunning) {
			// Execute next command
			DebugCommand command = null;
			synchronized (commandQueue) {
				if (commandQueue.isEmpty())
					try {
						commandQueue.wait();
					} catch (InterruptedException e) {
						continue;
					}
				command = commandQueue.remove(0);
			}
			int hr = command.run(this);
			if (hr == HRESULT.E_UNEXPECTED)
				processRunning = false;
			
			// Dispatch any events that have been queued up
			while (true) {
				DebugEvent event;
				synchronized (eventQueue) {
					if (eventQueue.isEmpty())
						break;
					event = eventQueue.remove(0);
				}

				Iterator<IDebugListener> i = listeners.iterator();
				while (i.hasNext())
					i.next().handleDebugEvent(event);
			}
		}
		
		return Status.OK_STATUS;
	}
	
	public void addListener(IDebugListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public void removeListener(IDebugListener listener) {
		listeners.remove(listener);
	}
	
	public void scheduleCommand(DebugCommand command) {
		// TODO - might need to interrupt the debugger
		synchronized (commandQueue) {
			commandQueue.add(command);
			commandQueue.notifyAll();
		}
	}
	
	public void fireEvent(DebugEvent event) {
		synchronized (eventQueue) {
			eventQueue.add(event);
		}
	}
	
}
