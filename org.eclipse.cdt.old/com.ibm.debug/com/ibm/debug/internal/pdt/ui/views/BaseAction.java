package com.ibm.debug.internal.pdt.ui.views;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/views/BaseAction.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 16:01:03)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.*;

/**
 * This is the base class of all the local actions used in the view.
 */
public abstract class BaseAction extends Action {
	private String id;
	GdbView myView;

	/**
	 * TaskAction constructor.
	 */
	protected BaseAction() {
		super();

	}

	/**
	 * TaskAction constructor.
	 */
	protected BaseAction(GdbView aView, String name) {
		super(name);
		myView = aView;
		setID(name);
	}

	/**
	 * Returns the unique action ID that will be
	 * used in contribution managers.
	 */
	public String getID() {
		return id;
	}

	/**
	 * Returns the my view.
	 */
	protected GdbView getGdbView() {
		return myView;
	}

	/**
	 * Sets the unique ID that should be used
	 * in the contribution managers.
	 */
	public void setID(String newId) {
		id = newId;
	}

}