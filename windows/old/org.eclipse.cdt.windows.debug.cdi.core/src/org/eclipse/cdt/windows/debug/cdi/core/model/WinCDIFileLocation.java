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

import org.eclipse.cdt.debug.core.cdi.ICDIFileLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;

/**
 * @author Doug Schaefer
 *
 */
public abstract class WinCDIFileLocation extends WinCDILocation implements ICDIFileLocation {

	private final String file;
	
	public WinCDIFileLocation(String file) {
		this.file = file;
	}
	
	public String getFile() {
		return file;
	}

	public boolean equals(ICDILocation location) {
		if (location == this)
			return true;
		if (location == null)
			return false;
		if ((location instanceof WinCDIFileLocation))
			return false;
		return file.equals(((WinCDIFileLocation)location).getFile());
	}

}
