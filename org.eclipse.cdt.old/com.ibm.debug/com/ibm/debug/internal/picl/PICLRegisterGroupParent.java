package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLRegisterGroupParent.java, eclipse, eclipse-dev, 20011128
// Version 1.7 (last modified 11/28/01 15:59:39)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;

public class PICLRegisterGroupParent extends PICLDebugElement {

	Object[] expandedElements;

    /**
     * Constructor for PICLRegisterGroupParent
     */
    protected PICLRegisterGroupParent(IDebugElement parent) {
        super(parent, PICLDebugElement.REGISTER_GROUP_PARENT);
    }

    /**
     * This label will not appear since it is acting only as a parent to the register groups
     * @see DebugElement#getLabel(boolean)
     */
    public String getLabel(boolean qualified) {
        return "PICLRegisterGroupParent label";
    }

    /**
     * @see PICLDebugElement#doCleanupDetails()
     */
    protected void doCleanupDetails() {}

    /**
     * @see IDebugElement#getName()
     */
    public String getName() throws DebugException {
        return "PICLRegisterGroupParent name";
    }

    /**
     * Stores the current expanded state of its child tree.
     * Used to restore tree to current state next time selection returns
     * to current selected thread in debug view.
     *
     * @see getExpandedElements()
     */
    public void setExpandedElements(Object[] state){
    	expandedElements = state;
    }

    /**
     * Returns the current expanded state of its child tree.
     * Used to restore tree to current state when selection returns
     * to current selected thread in debug view.
     *
     * @see setExpandedElements()
     */
    public Object[] getExpandedElements(){
		return expandedElements;
	}

	/**
	 * Reset all of the registers in the register groups owned by this parent
	 */
	public void resetChanged() {
		// loop through all of the register groups and reset the changed flags

		if (!hasChildren())
			return;

		IDebugElement registerGroups[] = null;

		try {
			registerGroups = getChildrenNoExpand();
		} catch(DebugException de) {
			return;
		}
		for (int i = 0;i < registerGroups.length; i++)
			((PICLRegisterGroup)registerGroups[i]).resetChanged();

	}

}

