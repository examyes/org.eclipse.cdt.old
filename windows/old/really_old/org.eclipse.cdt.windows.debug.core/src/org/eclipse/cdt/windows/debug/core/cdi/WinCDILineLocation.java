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

import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDILineLocation implements ICDILineLocation {

	private final String file;
	private final int lineNumber;
	
	public WinCDILineLocation(String file, int lineNumber) {
		this.file = file;
		this.lineNumber = lineNumber;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}

	public String getFile() {
		return file;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocation#equals(org.eclipse.cdt.debug.core.cdi.ICDILocation)
	 */
	public boolean equals(ICDILocation location) {
		if (location instanceof ICDILineLocation) {
			ICDILineLocation lineloc = (ICDILineLocation)location;
			return lineNumber == lineloc.getLineNumber()
				&& file.equals(lineloc.getFile());
		} else
			return false;
	}

}
