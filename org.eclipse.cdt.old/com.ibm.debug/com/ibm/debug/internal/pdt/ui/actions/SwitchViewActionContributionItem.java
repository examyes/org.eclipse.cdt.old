package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/SwitchViewActionContributionItem.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 15:59:57)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.internal.picl.PICLStackFrame;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;

public class SwitchViewActionContributionItem extends ActionContributionItem {
	protected short viewKind;
	/**
	 * Constructor for SwitchViewActionContributionItem
	 */
	public SwitchViewActionContributionItem(IAction action, short viewKind) {
		super(action);
		this.viewKind = viewKind;
	}

	/**
	 * Gets the isEnabled
	 * @return Returns a boolean
	 */
	public boolean isEnabled() {
		return getAction().isEnabled();
	}
	/**
	 * Sets the isEnabled
	 * @param isEnabled The isEnabled to set
	 */
	public void setEnabled(boolean isEnabled) {
		getAction().setEnabled(isEnabled);
	}

	/**
	 * Gets the viewKind
	 * @return Returns a short
	 */
	public short getViewKind() {
		return viewKind;
	}

	public void setStackFrame(PICLStackFrame frame) {
		((SwitchViewBaseAction)getAction()).setStackFrame(frame);
	}

}

