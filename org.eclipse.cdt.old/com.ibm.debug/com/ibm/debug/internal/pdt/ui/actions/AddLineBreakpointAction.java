package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/AddLineBreakpointAction.java, eclipse, eclipse-dev, 20011128
// Version 1.6 (last modified 11/28/01 15:58:13)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;

import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.internal.pdt.ui.dialogs.LineBPWizard;


public class AddLineBreakpointAction extends AbstractOpenWizardWorkbenchAction implements IViewActionDelegate {
	/**
	 * Constructor for AddLineBreakpointAction
	 */
	public AddLineBreakpointAction(){
		super();
		init(PICLDebugPlugin.getActiveWorkbenchWindow());
	}

	/**
	 * Constructor for AddAddressBreakpointAction
	 */
	public AddLineBreakpointAction(IWorkbench workbench, String label, Class[] acceptedTypes) {
		super(workbench, label, acceptedTypes, true);
	}

	/**
	 * @see AbstractOpenWizardAction#createWizard()
	 */
	protected Wizard createWizard() {
		return new LineBPWizard();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}


	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
	}


}

