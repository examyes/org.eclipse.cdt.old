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

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.windows.debug.cdi.core.model.WinCDILocation.LocationNotFound;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDILocator implements ICDILocator {

	private WinCDIFunctionLocation functionLocation;
	
	public WinCDILocator(WinCDIFunctionLocation location) {
		functionLocation = location;
	}
	
	public String getFile() {
		if (functionLocation != null)
			return functionLocation.getFile();
		else
			return null;
	}

	public boolean equals(ICDILocation location) {
		if (location == this)
			return true;
		if (location == null)
			return false;
		
		if (functionLocation != null) {
			return location instanceof WinCDIFunctionLocation ? functionLocation.equals(location) : false;
		} else
			return false;
	}

	public int getLineNumber() {
		if (functionLocation != null) {
			// Is there a way to get this?
			return 0;
		} else
			return 0;
	}

	public String getFunction() {
		if (functionLocation != null)
			return functionLocation.getFunction();
		else
			return null;
	}

	public BigInteger getAddress() {
		try {
			if (functionLocation != null)
				return new BigInteger(String.valueOf(functionLocation.getAddress()));
			else
				return null;
		} catch (LocationNotFound e) {
			return null;
		}
	}

}
