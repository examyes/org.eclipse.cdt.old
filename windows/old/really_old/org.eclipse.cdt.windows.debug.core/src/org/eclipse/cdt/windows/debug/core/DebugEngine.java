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

package org.eclipse.cdt.windows.debug.core;

import java.util.Map;

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

	public final String commandLine;
	public final String initialDirectory;
	public final Map<String, String> environment;
	
	public DebugEngine(String commandLine,
			String initialDirectory,
			Map<String, String> environment) {
		super("Windows Debug Engine");
		this.commandLine = commandLine;
		this.initialDirectory = initialDirectory;
		this.environment = environment;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			IDebugClient debugClient = new IDebugClient();
			IDebugControl debugControl = new IDebugControl();
			DebugCreateProcessOptions options = new DebugCreateProcessOptions();
			options.setCreateFlags(DebugCreateProcessOptions.DEBUG_ONLY_THIS_PROCESS);

			if (HRESULT.FAILED(debugClient.createProcess2(
					0, commandLine, options, initialDirectory, environment)))
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						"Failed to launch: " + commandLine);

			// Event loop
			while (true) {
				int hr = debugControl.waitForEvent(0, IDebugControl.INFINITE);
				if (hr == HRESULT.E_UNEXPECTED)
					break;
				if (HRESULT.FAILED(hr))
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							"Failed waiting for event");
			}

			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}
	
}
