/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.debug.win32.core.cdi;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.debug.core.ILaunch;

public class WinDbgSession implements ICDISession {

	private ICDITarget[] targets = new ICDITarget[1];
	private WinDbgEventManager eventManager;
	
	public WinDbgSession(ILaunch launch, IBinaryObject exe) {
		targets[0] = new WinDbgTarget(this, launch, exe);
		eventManager = new WinDbgEventManager(this);
	}

	public ICDITarget[] getTargets() {
		return targets;
	}

	public void setAttribute(String key, String value) {
		// TODO Auto-generated method stub

	}

	public String getAttribute(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIEventManager getEventManager() {
		return eventManager;
	}

	public ICDISessionConfiguration getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	public void terminate() throws CDIException {
		// TODO Auto-generated method stub

	}

	public Process getSessionProcess() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

}
