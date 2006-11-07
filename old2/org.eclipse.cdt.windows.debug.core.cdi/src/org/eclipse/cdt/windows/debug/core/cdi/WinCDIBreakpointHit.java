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

import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDIBreakpointHit implements ICDIBreakpointHit {

	private final ICDISession session;
	private final ICDIBreakpoint bp;
	
	public WinCDIBreakpointHit(ICDISession session, ICDIBreakpoint bp) {
		this.session = session;
		this.bp = bp;
	}
	
	public ICDIBreakpoint getBreakpoint() {
		return bp;
	}

	public ICDISession getSession() {
		return session;
	}

}
