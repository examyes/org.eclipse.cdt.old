package com.ibm.debug.internal.pdt.ui.views;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2000, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/views/DeleteAction.java, eclipse, eclipse-dev, 20011129
// Version 1.4 (last modified 11/29/01 14:16:02)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.swt.widgets.Item;
/**
 * Delete selected text
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;


public class DeleteAction extends BaseAction {
	/**
	 * DeleteAction constructor comment.
	 * @param aView GdbView
	 * @param name java.lang.String
	 */
	protected DeleteAction(GdbView aView, String name) {
		super(aView, name);
	}

	/**
	 * Removes an item
	 */
	public void run() {
		getGdbView().deleteSelection();
	}

}
