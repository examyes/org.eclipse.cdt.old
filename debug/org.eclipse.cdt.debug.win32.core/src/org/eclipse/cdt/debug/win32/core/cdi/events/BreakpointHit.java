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
package org.eclipse.cdt.debug.win32.core.cdi.events;

import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;

public class BreakpointHit implements ICDIBreakpointHit {

	private ICDISession session;
	private ICDIBreakpoint breakpoint;
	
	public BreakpointHit(ICDISession session, ICDIBreakpoint breakpoint) {
		this.session = session;
		this.breakpoint = breakpoint;
	}

	public ICDIBreakpoint getBreakpoint() {
		return breakpoint;
	}

	public ICDISession getSession() {
		return session;
	}

}
