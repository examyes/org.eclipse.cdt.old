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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.windows.debug.core.IDebugBreakpoint;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDILineBreakpoint implements ICDILineBreakpoint {

	private final ICDILocator locator;
	
	private IDebugBreakpoint bp;
	
	public WinCDILineBreakpoint(ICDILineLocation location) {
		locator = new WinCDILocator(location);
	}
	
	public ICDILocator getLocator() {
		return locator;
	}

	public void setDebugBreakpoint(IDebugBreakpoint bp) {
		this.bp = bp;
	}
	
	public ICDICondition getCondition() throws CDIException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isEnabled() throws CDIException {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isHardware() {
		// TODO Auto-generated method stub
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

	public ICDITarget getTarget() {
		// TODO Auto-generated method stub
		return null;
	}

}
