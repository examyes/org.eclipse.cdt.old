/**********************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.windows.debug.cdi.core.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.core.runtime.IPath;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDISession implements ICDISession {

	private WinCDITarget[] targets;
	private final WinCDISessionConfiguration configuration;
	private final Map<String, String> attributes = new HashMap<String, String>();
	final WinCDIEventManager eventManager;
	
	public WinCDISession(IPath executable) {
		targets = new WinCDITarget[] {
			new WinCDITarget(this, executable)
		};
		
		configuration = new WinCDISessionConfiguration(this);
		eventManager = new WinCDIEventManager(this);
	}
	
	public String getAttribute(String key) {
		return attributes.get(key);
	}

	public ICDISessionConfiguration getConfiguration() {
		return configuration;
	}

	public ICDIEventManager getEventManager() {
		return eventManager;
	}

	public Process getSessionProcess() throws CDIException {
		// Our debugger isn't running in a process
		return null;
	}

	public ICDITarget[] getTargets() {
		return targets;
	}

	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	public void terminate() throws CDIException {
		targets[0].terminate();
	}

}
