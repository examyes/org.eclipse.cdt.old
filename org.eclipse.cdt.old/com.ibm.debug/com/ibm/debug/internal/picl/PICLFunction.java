package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLFunction.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:01:08)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.Function;
import com.ibm.debug.model.Location;
import com.ibm.debug.model.ViewFile;
import java.io.IOException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;

public class PICLFunction extends PICLDebugElement {

	private Function fFunction = null;

	/**
	 * Constructor for PICLFunction
	 */
	public PICLFunction(IDebugElement parent, Function function) {
		super(parent, PICLDebugElement.FUNCTION);

		fFunction = function;
	}

	/**
	 * @see PICLDebugElement#doCleanupDetails()
	 */
	protected void doCleanupDetails() {
		fFunction = null;
	}

	/**
	 * @see PICLDebugElement#getLabel(boolean)
	 */
	public String getLabel(boolean qualified) {
		String returnType = fFunction.getReturnType();
		String functionName = fFunction.getName();

		if (returnType == null || returnType.length() == 0)
			return fFunction.getName();
		else
			return "(" + returnType + ") " + functionName;
	}

	/**
	 * @see IDebugElement#getName()
	 */
	public String getName() throws DebugException {
		return null;
	}

	/**
	 * return the file name that contains this function
	 * @return file name
	 */
	public String getFileName() {

		Location loc = null;
		try {
			loc = fFunction.getLocation();
			if (loc != null) {
				ViewFile vf = loc.file();
				if (vf != null)
					return vf.baseFileName();
			}
		} catch(IOException ioe) {}

		return null;
	}

	/**
	 * returns the line number in the view file that this function is located
	 * @return line number, returns 0 if no line number
	 */
	public int getLineNumber() {
		Location loc = null;
		try {
			loc = fFunction.getLocation();
			if (loc != null)
				return loc.lineNumber();
		} catch(IOException ioe) {}

		return 0;
	}


}

