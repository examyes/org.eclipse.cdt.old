package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/SwitchViewBaseAction.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 15:59:56)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.internal.picl.PICLStackFrame;
import org.eclipse.debug.internal.ui.DebugView;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

public abstract class SwitchViewBaseAction extends Action {
	protected PICLStackFrame stackFrame = null;
	protected DebugView view = null;

	/**
	 * Constructor for SwitchToDisViewA3
	 */
	protected SwitchViewBaseAction() {
		super();
	}

	/**
	 * Constructor for SwitchToDisViewA3
	 */
	protected SwitchViewBaseAction(String arg0) {
		super(arg0);
	}

	/**
	 * Constructor for SwitchToDisViewA3
	 */
	protected SwitchViewBaseAction(String arg0, ImageDescriptor arg1) {
		super(arg0, arg1);
	}

	/**
	 * Gets the stackFrame
	 * @return Returns a PICLStackFrame
	 */
	public PICLStackFrame getStackFrame() {
		return stackFrame;
	}

	/**
	 * Sets the stackFrame
	 * @param stackFrame The stackFrame to set
	 */
	public void setStackFrame(PICLStackFrame stackFrame) {
		this.stackFrame = stackFrame;
	}

	/**
	 * Gets the view
	 * @return Returns a DebugView
	 */
	public DebugView getView() {
		return view;
	}
	/**
	 * Sets the view
	 * @param view The view to set
	 */
	public void setView(DebugView view) {
		this.view = view;
	}

}

