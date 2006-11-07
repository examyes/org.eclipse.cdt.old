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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.windows.debug.core.engine.BreakpointEvent;
import org.eclipse.cdt.windows.debug.core.engine.DebugEvent;
import org.eclipse.cdt.windows.debug.core.engine.ExitProcessEvent;
import org.eclipse.cdt.windows.debug.core.engine.IDebugListener;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDIEventManager implements ICDIEventManager, IDebugListener {

	private final WinCDISession session;
	private final WinCDITarget target;
	
	private List<ICDIEventListener> listeners = new LinkedList<ICDIEventListener>();
	
	public WinCDIEventManager(WinCDISession session, WinCDITarget target) {
		this.session = session;
		this.target = target;
		target.getDebugEngine().addListener(this);
	}
	
	public void addEventListener(ICDIEventListener listener) {
		synchronized (listeners) {
			if (!listeners.contains(listener))
				listeners.add(listener);
		}
	}

	public void removeEventListener(ICDIEventListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	public ICDISession getSession() {
		return session;
	}

	public void fireEvents(ICDIEvent[] events) {
		ICDIEventListener[] list; 
		synchronized (listeners) {
			list = listeners.toArray(new ICDIEventListener[listeners.size()]);
		}
		
		for (int i = 0; i < list.length; ++i)
			list[i].handleDebugEvents(events);
	}
	
	public void handleDebugEvent(DebugEvent event) {
		switch (event.getType()) {
		case DebugEvent.BREAKPOINT:
			// Create the breakpoint event
			BreakpointEvent bpEvent = (BreakpointEvent)event;
			ICDIBreakpoint bp = target.getBreakpoint(bpEvent.getBreakpoint());
			WinCDIBreakpointHit bpHit = new WinCDIBreakpointHit(session, bp);
			WinCDISuspendedEvent susEvent = new WinCDISuspendedEvent(bpHit, target);
			fireEvents(new ICDIEvent[] { susEvent });
			break;
		case DebugEvent.EXIT_PROCESS:
			ExitProcessEvent epEvent = (ExitProcessEvent)event;
			target.terminated(epEvent.getExitCode());
			WinCDIExitedEvent exitedEvent = new WinCDIExitedEvent(target);
			WinCDIDestroyedEvent destroyedEvent = new WinCDIDestroyedEvent();
			fireEvents(new ICDIEvent[] { exitedEvent, destroyedEvent });
			break;
		}
	}
	
}
