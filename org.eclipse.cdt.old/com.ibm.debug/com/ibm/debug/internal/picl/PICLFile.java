package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLFile.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 16:01:07)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;

import com.ibm.debug.model.Function;
import com.ibm.debug.model.ViewFile;

public class PICLFile extends PICLDebugElement {

	private ViewFile fViewFile = null;
	private boolean fFunctionsRetrieved = false;

	/**
	 * Constructor for PICLFile
	 */
	public PICLFile(IDebugElement parent, ViewFile viewFile) {
		super(parent,PICLDebugElement.VIEWFILE);

		fViewFile = viewFile;
	}

	/**
	 * @see PICLDebugElement#doCleanupDetails()
	 */
	protected void doCleanupDetails() {
		fViewFile = null;
	}

	/**
	 * @see PICLDebugElement#getLabel(boolean)
	 */
	public String getLabel(boolean qualified) {
		try {
			if (!fViewFile.view().isSourceView())
				return PICLUtils.getResourceString("picl_file.no_source");
			else
				return fViewFile.name();
		} catch(IOException ioe) {
			return PICLUtils.getResourceString("picl_file.source_error");
		}
	}

	/**
	 * @see IDebugElement#getName()
	 */
	public String getName() throws DebugException {
		return null;
	}



	/**
	 * @see PICLDebugElement#hasChildren()
	 */
	public boolean hasChildren() {
		return true;
	}

	/**
	 * @see PICLDebugElement#getChildren()
	 */
	public IDebugElement[] getChildren() throws DebugException {
		if (!fFunctionsRetrieved) {
			Vector functions = null;
			try {
				functions = fViewFile.getFunctions();
			} catch(IOException ioe) {}
			if (functions != null) {
				Iterator iter = functions.iterator();
				while (iter.hasNext()) {
					Function f = (Function)iter.next();
					if (f != null)
						addChild(new PICLFunction(this, f));
				}
			}
			fFunctionsRetrieved = true;

		}
		return super.getChildren();

	}

	/**
	 * @see PICLDebugElement#getChildrenNoExpand()
	 */
	public IDebugElement[] getChildrenNoExpand() throws DebugException {
		return super.getChildren();
	}

	public boolean hasSource() {
		return fViewFile.view().isSourceView();
	}

	/**
	 * return the file name
	 * @return file name
	 */
	public String getFileName() {
		try {
			return fViewFile.baseFileName();
		} catch(IOException ioe) {}

		return null;
	}
	/**
	 * returns the line number in the view file
	 * @return line number, returns 0 if no line number
	 */
	public int getLineNumber() {
		return 1;
	}

}
