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
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

public class WinDbgLocationBreakpoint extends WinDbgBreakpoint
		implements ICDILocationBreakpoint {

	private ICDILocation location;
	
	WinDbgLocationBreakpoint(
			int type,
			ICDICondition condition,
			String threadId,
			ICDITarget target,
			ICDILocation location) {
		super(type, condition, threadId, target);
		this.location = location;
	}

	public ICDILocation getLocation() throws CDIException {
		return location;
	}
}
