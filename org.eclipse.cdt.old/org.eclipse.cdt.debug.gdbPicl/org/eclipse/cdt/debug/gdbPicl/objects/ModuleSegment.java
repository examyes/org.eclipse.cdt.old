/*
 * Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import org.eclipse.cdt.debug.gdbPicl.*;

import java.util.*;
import java.text.*;

/**
 * ModuleSegment.
 */
public class ModuleSegment {
	public ModuleSegment(String s, String e) {
		startAddress = s;
		endAddress = e;
	}

	/**
	 * Gets the endAddress.
	 * @return Returns a String
	 */
	public String getEndAddress() {
		return endAddress;
	}

	/**
	 * Sets the endAddress.
	 * @param endAddress The endAddress to set
	 */
	public void setEndAddress(String endAddress) {
		this.endAddress = endAddress;
	}

	/**
	 * Gets the startAddress.
	 * @return Returns a String
	 */
	public String getStartAddress() {
		return startAddress;
	}

	/**
	 * Sets the startAddress.
	 * @param startAddress The startAddress to set
	 */
	public void setStartAddress(String startAddress) {
		this.startAddress = startAddress;
	}

	//Data members
	private String startAddress = "-1";
	private String endAddress = "-1";

}