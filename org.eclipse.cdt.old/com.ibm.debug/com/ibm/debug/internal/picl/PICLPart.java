package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLPart.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 16:00:04)
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

import com.ibm.debug.model.Part;
import com.ibm.debug.model.View;
import com.ibm.debug.model.ViewFile;

public class PICLPart extends PICLDebugElement {

	private Part fPart = null;
	private boolean fFilesRetrieved = false;

	/**
	 * Constructor for PICLPart
	 */
	public PICLPart(IDebugElement parent, Part part) {
		super(parent, PICLDebugElement.PART);
		fPart = part;
	}

	/**
	 * @see PICLDebugElement#doCleanupDetails()
	 */
	protected void doCleanupDetails() {
		fPart = null;
	}

	/**
	 * @see PICLDebugElement#getLabel(boolean)
	 */
	public String getLabel(boolean qualified) {
		return fPart.name();
	}

	/**
	 * @see IDebugElement#getName()
	 */
	public String getName() throws DebugException {
		return fPart.name();
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
		if (fFilesRetrieved)
			return super.getChildren();
		else {
			// get the list of files
			// loop through all of the views and add the files as children
			View[] views = fPart.views();

			// determine if a source view exists.  If it doesn't then default to
			// the last view found.

			View defaultView = null;
			for (int i=0; i < views.length; i++) {
				if (views[i] == null)
					continue;
				defaultView = views[i];
				if (defaultView.isSourceView())
					break;
			}


			Vector viewFiles = null;
			try {
				viewFiles = defaultView.getFiles();
			} catch(IOException ioe) {}

			if (viewFiles != null) {
				Iterator iter = viewFiles.iterator();
				while (iter.hasNext()) {
					ViewFile vf = (ViewFile)iter.next();
					if (vf == null)
						continue;
					addChild(new PICLFile(this,vf));
				}
			}
			fFilesRetrieved = true;
			return super.getChildren();
		}
	}

	/**
	 * @see PICLDebugElement#getChildrenNoExpand()
	 */
	public IDebugElement[] getChildrenNoExpand() throws DebugException {
		return super.getChildren();
	}

}

