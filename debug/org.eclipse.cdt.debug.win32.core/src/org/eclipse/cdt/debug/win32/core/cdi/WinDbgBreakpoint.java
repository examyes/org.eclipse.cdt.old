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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

public class WinDbgBreakpoint implements ICDIBreakpoint {

	private int type;
	private boolean enabled = true;
	private ICDICondition condition;
	private ICDITarget target;
	
	WinDbgBreakpoint(int type, ICDICondition condition, ICDITarget target) {
		this.type = type;
		this.condition = condition;
		this.target = target;
	}
	
	public boolean isTemporary() {
		return type == ICDIBreakpoint.TEMPORARY;
	}

	public boolean isHardware() {
		return type == ICDIBreakpoint.HARDWARE;
	}

	public boolean isEnabled() throws CDIException {
		return enabled;
	}

	public void setEnabled(boolean enabled) throws CDIException {
		this.enabled = enabled;
	}

	public ICDICondition getCondition() throws CDIException {
		return condition;
	}

	public void setCondition(ICDICondition condition) throws CDIException {
		this.condition = condition;
	}

	public ICDITarget getTarget() {
		return target;
	}
}
