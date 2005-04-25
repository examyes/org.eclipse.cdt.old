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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.model.ICDIAddressBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;

public class WinDbgBreakpointManager implements ICDIEventListener {

	private ICDISession session;
	boolean processCreated = false;
	List bpQueue = Collections.synchronizedList(new LinkedList());
	List deferredQueue = Collections.synchronizedList(new LinkedList());
	
	WinDbgBreakpointManager(ICDISession session) {
		this.session = session;
		session.getEventManager().addEventListener(this);
	}
	
	public ICDIBreakpoint[] getBreakpoints(WinDbgTarget target) throws CDIException {
		ICDIBreakpoint[] bps = (ICDIBreakpoint[]) bpQueue.toArray(new ICDIBreakpoint[bpQueue.size()]);
		List list = new LinkedList();
		for (int i = 0; i < bps.length; i++) {
			if (bps[i].getTarget().equals(target)) {
				list.add(bps[i]);
			}
		}
		return (ICDIBreakpoint[]) list.toArray(new ICDIBreakpoint[list.size()]);
	}

	public void deleteBreakpoint(ICDIBreakpoint breakpoint) throws CDIException {
		// TODO Auto-generated method stub
	}

	public void deleteBreakpoints(ICDIBreakpoint[] breakpoints)
			throws CDIException {
		// TODO Auto-generated method stub
	}

	public void deleteAllBreakpoints() throws CDIException {
		// TODO Auto-generated method stub
	}

	public synchronized ICDILineBreakpoint setLineBreakpoint(WinDbgTarget wTarget, int type,
			ICDILineLocation location, ICDICondition condition, boolean deferred)
			throws CDIException {
		
		WinDbgLineBreakpoint bp = new WinDbgLineBreakpoint(
				type, 
				condition, 
				wTarget,
				location);

		setLocationBreakpoint(bp);		
		return bp;
	}
	
	public synchronized ICDIFunctionBreakpoint setFunctionBreakpoint(WinDbgTarget wTarget, int type,
			ICDIFunctionLocation location, ICDICondition condition, boolean deferred)
			throws CDIException {
		
		WinDbgFunctionBreakpoint bp = new WinDbgFunctionBreakpoint(
				type, 
				condition, 
				wTarget,
				location);

		setLocationBreakpoint(bp);		
		return bp;
	}

	public synchronized ICDIAddressBreakpoint setAddressBreakpoint(WinDbgTarget wTarget, int type,
			ICDIAddressLocation location, ICDICondition condition, boolean deferred)
			throws CDIException {
		
		WinDbgAddressBreakpoint bp = new WinDbgAddressBreakpoint(
				type, 
				condition, 
				wTarget,
				location);

		setLocationBreakpoint(bp);		
		return bp;
	}

	public synchronized void setLocationBreakpoint(WinDbgLocationBreakpoint bp) throws CDIException {
		
		WinDbgTarget wTarget = (WinDbgTarget)bp.getTarget();
		if (processCreated) {
			wTarget.resolveLocation(bp.getLocator());
			wTarget.setBreakpoint(bp.getLocator().getAddress(), bp.isTemporary());
			bpQueue.add(bp);
		} else {
			deferredQueue.add(bp);
		}		
	}

	public ICDIWatchpoint setWatchpoint(int type, int watchType,
			String expression, ICDICondition condition) throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public void allowProgramInterruption(boolean allow) {
		// TODO Auto-generated method stub
	}

	public void setAutoUpdate(boolean update) {
		// TODO Auto-generated method stub
	}

	public boolean isAutoUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	public void update() throws CDIException {
		// TODO Auto-generated method stub
	}

	public synchronized void handleDebugEvents(ICDIEvent[] event) {
		for (int i = 0; i < event.length; ++i)
			if (event[i] instanceof ICDICreatedEvent) {
				while (!deferredQueue.isEmpty()) {
					ICDILocationBreakpoint bp = (ICDILocationBreakpoint)deferredQueue.remove(0);
					WinDbgTarget wTarget = (WinDbgTarget)bp.getTarget();
					wTarget.resolveLocation(bp.getLocator());
					wTarget.setBreakpoint(bp.getLocator().getAddress(), bp.isTemporary());
					bpQueue.add(bp);
				}
				processCreated = true;
			}
	}

	public ICDISession getSession() {
		return session;
	}
}
