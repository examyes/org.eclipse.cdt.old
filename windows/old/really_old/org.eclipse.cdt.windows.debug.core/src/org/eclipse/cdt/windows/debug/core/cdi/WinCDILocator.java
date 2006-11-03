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

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;

/**
 * @author Doug Schaefer
 *
 * Merges all the location types.
 */
public class WinCDILocator implements ICDILocator {
	
	private String file;
	private int lineNumber;
	private String function;
	private BigInteger address;

	public WinCDILocator() {
	}
	
	public WinCDILocator(ICDIFunctionLocation floc) {
		file = floc.getFile();
		function = floc.getFunction();
	}
	
	public WinCDILocator(ICDILineLocation lloc) {
		file = lloc.getFile();
		lineNumber = lloc.getLineNumber();
	}
	
	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public BigInteger getAddress() {
		return address;
	}

	public void setAddress(BigInteger address) {
		this.address = address;
	}

	public boolean equals(ICDILocation location) {
		// TODO
		return false;
	}

}
