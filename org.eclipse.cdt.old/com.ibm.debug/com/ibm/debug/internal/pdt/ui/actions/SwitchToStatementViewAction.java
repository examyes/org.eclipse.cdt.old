package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/SwitchToStatementViewAction.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 16:00:01)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.model.ViewInformation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;

public class SwitchToStatementViewAction extends SwitchViewBaseAction {

	/**
	 * Constructor for SwitchToDisViewAction
	 */
	protected SwitchToStatementViewAction() {
		super();
	}

	/**
	 * Constructor for SwitchToDisViewAction
	 */
	protected SwitchToStatementViewAction(String arg0) {
		super(arg0);
	}

	/**
	 * Constructor for SwitchToDisViewAction
	 */
	protected SwitchToStatementViewAction(String arg0, ImageDescriptor arg1) {
		super(arg0, arg1);
	}

	/**
	 * @see Action#run()
	 */
	public void run() {
		if (stackFrame != null)
		{
			PICLDebugTarget pdt = (PICLDebugTarget) stackFrame.getDebugTarget();
			ViewInformation viewInfo = pdt.getDebugEngine().getDisassemblyViewInformation();
			stackFrame.setViewInformation(viewInfo, true);

			// tell the view we want the file redisplayed
			Object[] myArray= new Object[]{stackFrame};
			StructuredSelection newSelection = new StructuredSelection(myArray);
			view.getViewer().setSelection(newSelection);
		}
	}

}

