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

import java.io.IOException;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.win32.core.CorePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.ILaunchConfiguration;

public class WinDbgSession implements ICDISession {
	
	static {
		try {
			CorePlugin.getDefault().loadLibrary("dbghelp"); //$NON-NLS-1$
			CorePlugin.getDefault().loadLibrary("win32cdi"); //$NON-NLS-1$
		} catch (IOException e) {
			System.out.println("Failed to load: " + e); //$NON-NLS-1$
		}
	}
	
	// Objects
	private ICDITarget[] targets;
	private WinDbgSessionConfiguration configuration;
	private WinDbgEventManager eventManager;
	private WinDbgRegisterManager registerManager;
	private WinDbgBreakpointManager breakpointManager;
	private WinDbgVariableManager variableManager;
	
	public WinDbgSession(ILaunchConfiguration config, IFile exe) {
		eventManager = new WinDbgEventManager(this);
		targets = new ICDITarget[1];
		targets[0] = new WinDbgTarget(this, exe);
		configuration = new WinDbgSessionConfiguration(this);
		registerManager = new WinDbgRegisterManager();
		breakpointManager = new WinDbgBreakpointManager(this);
		variableManager = new WinDbgVariableManager();
		
		// O.K. - go
		((WinDbgTarget)targets[0]).start();
	}

	public ICDITarget[] getTargets() {
		return targets;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#setAttribute(java.lang.String, java.lang.String)
	 */
	public void setAttribute(String key, String value) {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISession#getAttribute(java.lang.String)
	 */
	public String getAttribute(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public WinDbgBreakpointManager getBreakpointManager() {
		return breakpointManager;
	}
	
	public WinDbgVariableManager getVariableManager() {
		return variableManager;
	}
	
	public WinDbgRegisterManager getRegisterManager() {
		return registerManager;
	}

	public ICDIEventManager getEventManager() {
		return eventManager;
	}

	public ICDISessionConfiguration getConfiguration() {
		return configuration;
	}
	
	public void terminate() throws CDIException {
		targets[0].terminate();
	}

	public Process getSessionProcess() throws CDIException {
		return targets[0].getProcess();
	}
}
