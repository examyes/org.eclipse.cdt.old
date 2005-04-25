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

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

public abstract class WinDbgLocationBreakpoint extends WinDbgBreakpoint
		implements ICDILocationBreakpoint {

	private ICDILocation fLocation;
	
	WinDbgLocationBreakpoint(
			int type,
			ICDICondition condition,
			ICDITarget target,
			ICDILocation location) {
		super(type, condition, target);
		fLocation = location;
	}

	public int getLineNumber() {
		if (fLocation instanceof ICDILineLocation) {
			return ((ICDILineLocation)fLocation).getLineNumber();
		}
		return 0;
	}

	public String getFile() {
		if (fLocation instanceof ICDILineLocation) {
			return ((ICDILineLocation)fLocation).getFile();
		} else if (fLocation instanceof ICDIFunctionLocation) {
			return ((ICDIFunctionLocation)fLocation).getFile();
		}
		return null;
	}

	public BigInteger getAddress() {
		if (fLocation instanceof ICDIAddressLocation) {
			return ((ICDIAddressLocation)fLocation).getAddress();
		}
		return null;
	}

	public String getFunction() {
		if (fLocation instanceof ICDIFunctionLocation) {
			return ((ICDIFunctionLocation)fLocation).getFunction();
		}
		return null;
	}

	public ICDILocator getLocator() {
		return new WinDbgLocator(getFile(), getFunction(), getLineNumber(), getAddress());
	}

}
