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

	public String getAttribute(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDISessionConfiguration getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDIEventManager getEventManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public Process getSessionProcess() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public ICDITarget[] getTargets() {
		// TODO Auto-generated method stub
		return new ICDITarget[0];
	}

	public void setAttribute(String key, String value) {
		// TODO Auto-generated method stub
	}

	public void terminate() throws CDIException {
		// TODO Auto-generated method stub
	}

}
