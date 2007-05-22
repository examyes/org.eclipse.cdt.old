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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDIEventManager implements ICDIEventManager {

	private final WinCDISession session;
	private final List<ICDIEventListener> listeners = new ArrayList<ICDIEventListener>();
	
	public WinCDIEventManager(WinCDISession session) {
		this.session = session;
	}
	
	public synchronized void addEventListener(ICDIEventListener listener) {
		// TODO look for duplicate
		listeners.add(listener);
	}

	public synchronized void removeEventListener(ICDIEventListener listener) {
		listeners.remove(listener);
	}

	public ICDISession getSession() {
		return session;
	}

	public void dispatchEvent(ICDIEvent[] event) {
		// Need to copy the list since some listeners remove themselves during the
		// handle event.
		ICDIEventListener[] localListeners;
		synchronized (this) {
			localListeners = listeners.toArray(new ICDIEventListener[listeners.size()]);
		}
		
		for (int i = 0; i < localListeners.length; ++i)
			localListeners[i].handleDebugEvents(event);
	}
	
	public void dispatchEvent(ICDIEvent event) {
		dispatchEvent(new ICDIEvent[] { event });
	}
}
