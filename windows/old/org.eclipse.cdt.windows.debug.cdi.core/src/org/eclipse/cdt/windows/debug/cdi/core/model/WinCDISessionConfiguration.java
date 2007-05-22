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

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDISessionConfiguration implements ICDISessionConfiguration {

	private final WinCDISession session;
	
	public WinCDISessionConfiguration(WinCDISession session) {
		this.session = session;
	}
	
	public boolean terminateSessionOnExit() {
		return true;
	}

	public ICDISession getSession() {
		return session;
	}

}
