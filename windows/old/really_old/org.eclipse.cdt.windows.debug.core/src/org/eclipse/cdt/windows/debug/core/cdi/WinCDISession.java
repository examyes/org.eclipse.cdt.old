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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDISession implements ICDISession {

	private Map<String, String> attributes = new HashMap<String, String>();
	
	// We only have on target, I think...
	private WinCDITarget target = new WinCDITarget(this);
	
	private WinCDIEventManager eventManager = new WinCDIEventManager(this);
	
	public String getAttribute(String key) {
		return attributes.get(key);
	}

	public void setAttribute(String key, String value) {
		attributes.put(key, value);
	}

	public ICDISessionConfiguration getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIEventManager getEventManager() {
		return eventManager;
	}

	public Process getSessionProcess() throws CDIException {
		// there is no underlying debugger process
		return null;
	}

	public ICDITarget[] getTargets() {
		// TODO create the targets
		return new ICDITarget[] { target };
	}

	public void terminate() throws CDIException {
		// TODO Auto-generated method stub

	}

}
