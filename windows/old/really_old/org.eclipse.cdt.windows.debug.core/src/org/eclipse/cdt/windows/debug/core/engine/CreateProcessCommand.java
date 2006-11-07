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

import java.util.Map;

import org.eclipse.cdt.windows.debug.core.DebugCreateProcessOptions;
import org.eclipse.cdt.windows.debug.core.HRESULT;
import org.eclipse.cdt.windows.debug.core.IDebugClient;
import org.eclipse.cdt.windows.debug.core.IDebugControl;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class CreateProcessCommand extends DebugCommand {

	private final String commandLine;
	private final String initialDirectory;
	private final Map<String, String> environment;
	
	public CreateProcessCommand(String commandLine,
			String initialDirectory,
			Map<String, String> environment) throws CoreException {
		this.commandLine = commandLine;
		this.initialDirectory = initialDirectory;
		this.environment = environment;
	}
	
	@Override
	protected String getName() {
		return "CreateProcess";
	}
	
	private String getEnvironmentString() {
		// TODO - walk the map and create the string
		return null;
	};
	
	@Override
	public int run(DebugEngine engine) {
		IDebugClient client = engine.getDebugClient();
		
		// Create the process
		DebugCreateProcessOptions options = new DebugCreateProcessOptions();
		options.setCreateFlags(DebugCreateProcessOptions.DEBUG_ONLY_THIS_PROCESS);
		int hr = client.createProcess2(
				0L, commandLine, options,
				initialDirectory, getEnvironmentString());
		if (HRESULT.FAILED(hr) | engine.isProcessRunning())
			return hr;
		
		// Run to create the process
		return engine.getDebugControl().waitForEvent(0, IDebugControl.INFINITE);
	}

}
