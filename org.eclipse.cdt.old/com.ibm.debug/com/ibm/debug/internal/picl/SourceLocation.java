package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/SourceLocation.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 15:58:07)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.internal.picl.PICLUtils;
import org.eclipse.core.runtime.IPath;

/**
 * Implements <code>SourceLocation</code>
 *
 *
 */
public class SourceLocation {

	private final static String PREFIX= "source_location.";
	private final static String UNDETERMINED= "undetermined";

	protected int fLineNumber;
	protected IPath fFilePath;
	/**
	 * Creates a <code>SourceLocation</code> and sets it to
	 * its undetermined state.
	 */
	public SourceLocation() {
		setUndetermined();
	}

	/**
	 * @see com.ibm.itp.dui.api.model.ISourceLocation
	 */
	public int getLineNumber() {
		return fLineNumber;
	}

	/**
	 * @see com.ibm.itp.dui.api.model.ISourceLocation
	 */
	public IPath getPath() {
		return fFilePath;
	}

	/**
	 * Sets the line number path for this source location.
	 */
	public void setLineNumber(int newLineNumber) {
		fLineNumber= newLineNumber;
	}

	/**
	 * Sets the file path for this source location.
	 */
	public void setPath(IPath newFilePath) {
		fFilePath= newFilePath;
	}

	/**
	 * Sets this source location to its undetermined state.
	 */
	public void setUndetermined() {
		fLineNumber= -1;
		fFilePath= null;
	}

	/**
	 * Returns a <code>String</code> that represents the value of this object.
	 */
	public String toString() {
		if (getPath() == null) {
			return PICLUtils.getResourceString(UNDETERMINED);
		}
		return getPath().toOSString() + ": " + getLineNumber();
	}

}
