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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;

public class WinDbgEventManager implements ICDIEventManager {

	private WinDbgSession session;
	private List listeners;
	
	WinDbgEventManager(WinDbgSession session) {
		this.session = session;
	}
	
	public void addEventListener(ICDIEventListener listener) {
		if (listeners == null)
			listeners = new ArrayList();
		listeners.add(listener);
	}

	public void removeEventListener(ICDIEventListener listener) {
		listeners.remove(listener);
	}

	public ICDISession getSession() {
		return session;
	}
	
	public void fireEvent(ICDIEvent[] events) {
		if (listeners == null)
			return;

		// Listeners may get added during the firing
		Object [] currListeners = listeners.toArray();
		int n = currListeners.length;
		for (int i = 0; i < n; ++i) {
			((ICDIEventListener)currListeners[i]).handleDebugEvents(events);
		}
	}

}
