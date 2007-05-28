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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.windows.debug.core.sdk.Breakpoint;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDIBreakpoint extends WinCDIObject implements ICDIBreakpoint {

	private final Breakpoint breakpoint;
	
	public WinCDIBreakpoint(WinCDITarget target, Breakpoint breakpoint) {
		super(target);
		this.breakpoint = breakpoint;
	}
	
	public Breakpoint getBreakpoint() {
		return breakpoint;
	}
	
	public ICDICondition getCondition() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isEnabled() throws CDIException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isHardware() {
		// We don't support hardware breakpoints
		return false;
	}

	public boolean isTemporary() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setCondition(ICDICondition condition) throws CDIException {
		// TODO Auto-generated method stub
	}

	public void setEnabled(boolean enabled) throws CDIException {
		// TODO Auto-generated method stub
	}

}
