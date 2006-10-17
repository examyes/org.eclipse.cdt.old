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

import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;

/**
 * @author Doug Schaefer
 *
 * Nothing magic here. Windows can handle function names as offset expressions
 * when creating breakpoints. The file name is ignored since we are using
 * C++ expressions and these can't use filenames. But we store them here for
 * posterity (and who knows if the above statement may change).
 */
public class WinCDIFunctionLocation implements ICDIFunctionLocation {

	private final String file;
	private final String function;
	
	public WinCDIFunctionLocation(String file, String function) {
		this.file = file;
		this.function = function;
	}
	
	public String getFunction() {
		return function;
	}

	public String getFile() {
		return file;
	}

	public boolean equals(ICDILocation location) {
		if (location instanceof ICDIFunctionLocation) {
			ICDIFunctionLocation floc = (ICDIFunctionLocation)location;
			if (file == null) {
				if (floc.getFile() != null)
					return false;
			} else {
				if (!file.equals(floc.getFile()))
					return false;
			}
			
			if (function == null) {
				if (floc.getFunction() != null)
					return false;
			} else {
				if (!function.equals(floc.getFunction()))
					return false;
			}
			
			return true;
		} else
			return false;
	}

}
