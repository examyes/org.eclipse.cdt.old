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
import org.eclipse.cdt.debug.core.cdi.ICDIFileLocation;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;

public class WinDbgLocation implements ICDILineLocation, ICDIFunctionLocation, ICDIFileLocation, ICDIAddressLocation {

	private BigInteger address;
	private String file;
	private String function;
	private int lineNumber;

	public WinDbgLocation(String file) {
		this(file, null, 0, null);
	}

	public WinDbgLocation(String file, String function) {
		this (file, function, 0, null);
	}

	public WinDbgLocation(String file, int lineNumber) {
		this(file, null);
	}
	
	public WinDbgLocation(BigInteger address) {
		this(null, null, 0, address);
	}
	
	protected WinDbgLocation(String file, String function, int lineNumber, BigInteger address) {
		this.file = file;
		this.function = function;
		this.lineNumber = lineNumber;
		this.address = address;
	}

	void setAddress(BigInteger address) {
		this.address = address;
	}
	
	public BigInteger getAddress() {
		return address;
	}

	public String getFile() {
		return file;
	}

	public String getFunction() {
		return function;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public boolean equals(ICDILocation location) {
		if (location == this) {
			return true;
		}
		if (location instanceof ICDILineLocation) {
			ICDILineLocation lineLocation = (ICDILineLocation)location;
			String oFile = lineLocation.getFile();
			if (oFile != null && oFile.length() > 0 && file != null && file.length() > 0 && oFile.equals(file)) {
				if (lineLocation.getLineNumber() == lineNumber) {
					return true;
				}
			} else if ((file == null || file.length() == 0) && (oFile == null || oFile.length() == 0)) {
				if (lineLocation.getLineNumber() == lineNumber) {
					return true;
				}
			}
		} else if (location instanceof ICDIFunctionLocation) {
			ICDIFunctionLocation funcLocation = (ICDIFunctionLocation)location;
			String oFile = funcLocation.getFile();
			String oFunction = funcLocation.getFunction();
			if (oFile != null && oFile.length() > 0 && file != null && file.length() > 0 && oFile.equals(file)) {
				if (oFunction != null && oFunction.length() > 0 && function != null && function.length() > 0 && oFunction.equals(function)) {
					return true;
				} else if ((oFunction == null || oFunction.length() == 0) && (function == null || function.length() == 0)) {
					return true;
				}
			} else if ((file == null || file.length() == 0) && (oFile == null || oFile.length() == 0)) {
				if (oFunction != null && oFunction.length() > 0 && function != null && function.length() > 0 && oFunction.equals(function)) {
					return true;
				} else if ((oFunction == null || oFunction.length() == 0) && (function == null || function.length() == 0)) {
					return true;
				}
			}
		} else if (location instanceof ICDIAddressLocation) {
			ICDIAddressLocation addrLocation = (ICDIAddressLocation)location;
			BigInteger oAddr = addrLocation.getAddress();
			if (oAddr != null && oAddr.equals(address)) {
				return true;
			} else if (oAddr == null && address == null) {
				return true;
			}
		} else if (location instanceof ICDIFileLocation) {
			ICDIFileLocation fileLocation = (ICDIFileLocation)location;
			String oFile = fileLocation.getFile();
			if (oFile != null && oFile.length() > 0 && file != null && file.length() > 0 && oFile.equals(file)) {
				return true;
			} else if ((file == null || file.length() == 0) && (oFile == null || oFile.length() == 0)) {
				return true;
			}			
		}
		return false;
	}
}
