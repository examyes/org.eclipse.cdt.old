package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/AbstractOpenWizardWorkbenchAction.java, eclipse, eclipse-dev, 20011128
// Version 1.6 (last modified 11/28/01 15:58:10)
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public abstract class AbstractOpenWizardWorkbenchAction extends AbstractOpenWizardAction implements IWorkbenchWindowActionDelegate{

	public AbstractOpenWizardWorkbenchAction(IWorkbench workbench, String label, boolean acceptEmptySelection) {
		super(workbench, label, null, acceptEmptySelection);
	}

	public AbstractOpenWizardWorkbenchAction(IWorkbench workbench, String label, Class[] activatedOnTypes, boolean acceptEmptySelection) {
		super(workbench, label, activatedOnTypes, acceptEmptySelection);
	}

	protected AbstractOpenWizardWorkbenchAction() {
	}

	/**
	 * @see IActionDelegate#run
	 */
	public void run(IAction action) {
		run();
	}

	/**
	 * @see AbstractOpenWizardAction#dispose
	 */
	public void dispose() {
		// do nothing.
		setWorkbench(null);
	}

	/**
	 * @see AbstractOpenWizardAction#init
	 */
	public void init(IWorkbenchWindow window) {
		setWorkbench(window.getWorkbench());
	}

	/**
	 * @see IActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing. Action doesn't depend on selection.
	}

}
