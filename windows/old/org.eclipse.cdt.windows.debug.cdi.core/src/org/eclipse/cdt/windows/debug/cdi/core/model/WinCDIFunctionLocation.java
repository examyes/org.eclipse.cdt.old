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

import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDIFunctionLocation extends WinCDIFileLocation implements ICDIFunctionLocation {

	private final String function;
	
	public WinCDIFunctionLocation(String file, String function) {
		super(file);
		this.function = function;
	}
	
	public String getFunction() {
		return function;
	}
	
	@Override
	public long getAddress() throws LocationNotFound {
		// TODO Auto-generated method stub
//		throw new LocationNotFound();
		return 0;
	}

	public boolean equals(ICDILocation location) {
		if (location == this)
			return true;
		if (location == null)
			return false;
		if (!(location instanceof WinCDIFunctionLocation))
			return false;
		if (!function.equals(((WinCDIFunctionLocation)location).getFunction()))
			return false;
		return super.equals(location);
	}

}
