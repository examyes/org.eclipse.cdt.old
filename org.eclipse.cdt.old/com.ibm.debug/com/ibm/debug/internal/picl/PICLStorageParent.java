package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLStorageParent.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:00:37)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.Storage;
import com.ibm.debug.model.StorageEventListener;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;

/**
 * Represents the parent of all monitored storage
 */

public class PICLStorageParent extends PICLDebugElement {

	private Object[] fExpandedElements = null;

	/**
	 * Constructor for PICLStorageParent
	 */
	public PICLStorageParent(IDebugElement parent) {
		super(parent, PICLDebugElement.STORAGE_PARENT);
	}

	/**
	 * @see PICLDebugElement#doCleanupDetails()
	 */
	protected void doCleanupDetails() {
		fExpandedElements = null;
	}

	/**
	 * This label is not displayed.
	 * @see PICLDebugElement#getLabel(boolean)
	 */
	public String getLabel(boolean qualified) {
		return "PICLStorageParent label";
	}

	/**
	 * @see IDebugElement#getName()
	 */
	public String getName() throws DebugException {
		return "PICLStorageParent name";
	}

	/**
	 * Used to add storage to this parent.
	 * @param The module that is to be added
	 */
	public void addStorage(Storage storage) {
        addChild(new PICLStorage(this,storage));

	}


	/**
	 * Saves tree expanded elements
	 * @param expanded elements
	 */
	public void saveExpandedElements(Object[] elements) {
		fExpandedElements = elements;
	}

	/**
	 * Returns tree expanded elements
	 * @return expanded elements
	 */
	public Object[] getExpandedElements() {
		return fExpandedElements;
	}

}

