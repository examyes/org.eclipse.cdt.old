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

package org.eclipse.cdt.windows.debug.core.engine;

import org.eclipse.cdt.windows.debug.core.IDebugBreakpoint;

/**
 * @author Doug Schaefer
 *
 */
public class BreakpointEvent extends DebugEvent {

	private final IDebugBreakpoint bp;
	
	public BreakpointEvent(IDebugBreakpoint bp) {
		this.bp = bp;
	}
	
	public IDebugBreakpoint getBreakpoint() {
		return bp;
	}

	@Override
	public int getType() {
		return BREAKPOINT;
	}
	
}
