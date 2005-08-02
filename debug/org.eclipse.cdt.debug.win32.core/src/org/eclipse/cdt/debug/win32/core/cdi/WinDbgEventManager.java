/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.debug.win32.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;

public class WinDbgEventManager implements ICDIEventManager {

	private WinDbgSession session;
	
	public WinDbgEventManager(WinDbgSession session) {
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
