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

import org.eclipse.cdt.debug.core.cdi.ICDILocation;

public class WinDbgLocation implements ICDILocation {

	private long address;
	private String file;
	private String function;
	private int lineNumber;
	
	public WinDbgLocation(String file, String function, int lineNumber) {
		this.file = file;
		this.function = function;
		this.lineNumber = lineNumber;
		address = 0;
	}
	
	public WinDbgLocation(long address) {
		this.address = address;
		this.file = null;
		this.function = null;
		this.lineNumber = 0;
	}
	
	void setAddress(long address) {
		this.address = address;
	}
	
	public long getAddress() {
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
		return address == location.getAddress()
			&& lineNumber == location.getLineNumber()
			&& function.equals(location.getFunction())
			&& file.equals(location.getFile());
	}
}
