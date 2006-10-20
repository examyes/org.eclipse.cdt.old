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
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.windows.debug.core.IDebugBreakpoint;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDIFunctionBreakpoint implements ICDIFunctionBreakpoint {

	private final WinCDITarget target;
	private final int type;
	private final ICDIFunctionLocation location;
	private ICDICondition condition;
	
	private boolean enabled = true;
	
	private IDebugBreakpoint bp;
	
	public WinCDIFunctionBreakpoint(WinCDITarget target, int type,
			ICDIFunctionLocation location, ICDICondition condition,
			boolean deferred) {
		this.target = target;
		this.type = type;
		this.location = location;
		this.condition = condition;
		// deferred doesn't seem to matter
	}

	public void setDebugBreakpoint(IDebugBreakpoint bp) {
		this.bp = bp;
	}
	
	public ICDILocator getLocator() {
		return new WinCDILocator(location);
	}

	public ICDICondition getCondition() throws CDIException {
		return condition;
	}

	public boolean isEnabled() throws CDIException {
		return enabled;
	}

	public boolean isHardware() {
		return type == HARDWARE;
	}

	public boolean isTemporary() {
		return type == TEMPORARY;
	}

	public void setCondition(ICDICondition condition) throws CDIException {
		this.condition = condition;
	}

	public void setEnabled(boolean enabled) throws CDIException {
		this.enabled = enabled;
	}

	public ICDITarget getTarget() {
		return target;
	}

}
