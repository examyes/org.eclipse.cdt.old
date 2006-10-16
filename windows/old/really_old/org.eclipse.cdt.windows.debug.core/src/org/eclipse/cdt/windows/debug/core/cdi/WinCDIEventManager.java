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

import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDIEventManager implements ICDIEventManager {

	private final WinCDISession session;
	
	public WinCDIEventManager(WinCDISession session) {
		this.session = session;
	}
	
	public void addEventListener(ICDIEventListener listener) {
		// TODO Auto-generated method stub

	}

	public void removeEventListener(ICDIEventListener listener) {
		// TODO Auto-generated method stub

	}

	public ICDISession getSession() {
		return session;
	}

}
