package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/SetPreferredViewAction.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 15:59:58)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.internal.pdt.ui.dialogs.PreferredSourceViewDialog;
import com.ibm.debug.internal.picl.PICLDebugElement;
import com.ibm.debug.internal.picl.PICLDebugTarget;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;


public class SetPreferredViewAction implements IViewActionDelegate {
	private PICLDebugTarget debugTarget = null;
	/**
	 * Constructor for SetPreferredViewAction
	 */
	public SetPreferredViewAction() {
		super();
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart arg0) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction arg0) {
		Shell shell= PICLDebugPlugin.getActiveWorkbenchShell();
		PreferredSourceViewDialog dialog = new PreferredSourceViewDialog(shell, debugTarget);
		dialog.open();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection sel) {
		Object element = null;
		boolean show = false;
		debugTarget = null;

		if (sel instanceof IStructuredSelection)
		{
			IStructuredSelection selection = (IStructuredSelection) sel;
			element = selection.getFirstElement();
		} else
		  element = sel;

		if (element instanceof PICLDebugElement)
		{
		 	// enable menu item
			show = true;
			debugTarget = (PICLDebugTarget) ((PICLDebugElement)element).getDebugTarget();
		}
		else if (element instanceof ILaunch)
		{
		 	IDebugTarget dt =((ILaunch) element).getDebugTarget();
			if (dt instanceof PICLDebugTarget) {
				show = true;
				debugTarget = (PICLDebugTarget) dt;
			}
		}

		if (show == true)
		{
		 	// enable menu item
			action.setEnabled(true);
		} else
		{
		 	// disable menu item
			action.setEnabled(false);
		}
	}


}

