package com.ibm.debug.internal.pdt.ui.actions;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/actions/AbstractOpenWizardAction.java, eclipse, eclipse-dev, 20011128
// Version 1.6 (last modified 11/28/01 15:58:09)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.Iterator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import com.ibm.debug.internal.picl.PICLUtils;

public abstract class AbstractOpenWizardAction extends Action {

	public static final String WIZARD_TITLE= "AbstractOpenWizardAction.title";

	private IWorkbench fWorkbench;

	private Class[] fActivatedOnTypes;
	private boolean fAcceptEmptySelection;

	public AbstractOpenWizardAction(IWorkbench workbench, String label, boolean acceptEmptySelection) {
		this(workbench, label, null, acceptEmptySelection);
	}

	public AbstractOpenWizardAction(IWorkbench workbench, String label, Class[] activatedOnTypes, boolean acceptEmptySelection) {
		super(label);
		fWorkbench= workbench;
		fActivatedOnTypes= activatedOnTypes;
		fAcceptEmptySelection= acceptEmptySelection;
	}

	protected AbstractOpenWizardAction() {
	}

	protected IWorkbench getWorkbench() {
		return fWorkbench;
	}

	protected void setWorkbench(IWorkbench workbench) {
		fWorkbench= workbench;
	}

	private boolean isOfAcceptedType(Object obj) {
		for (int i= 0; i < fActivatedOnTypes.length; i++) {
			if (fActivatedOnTypes[i].isInstance(obj)) {
				return true;
			}
		}
		return false;
	}


	private boolean isEnabled(Iterator iter) {
		while (iter.hasNext()) {
			Object obj= iter.next();
			if (!isOfAcceptedType(obj) || !shouldAcceptElement(obj)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * can be overridden to add more checks
	 * obj is guaranteed to be instance of one of the accepted types
	 */
	protected boolean shouldAcceptElement(Object obj) {
		return true;
	}

	/**
	 * Create the specific Wizard
	 * (to be implemented by a subclass)
	 */
	abstract protected Wizard createWizard();


	protected ISelection getCurrentSelection() {
		IWorkbenchWindow window= fWorkbench.getActiveWorkbenchWindow();
		if (window != null) {
			return window.getSelectionService().getSelection();
		}
		return null;
	}

	/**
	 * The user has invoked this action.
	 */
	public void run() {
		Wizard wizard= createWizard();
		if(wizard == null)
			return;
		WizardDialog dialog= new WizardDialog(fWorkbench.getActiveWorkbenchWindow().getShell(), wizard);
		dialog.create();
	//	dialog.getShell().setText(PICLUtils.getResourceString(WIZARD_TITLE));
		dialog.open();
	}

	public boolean canActionBeAdded() {
		ISelection selection= getCurrentSelection();
		if (selection == null || selection.isEmpty()) {
			return fAcceptEmptySelection;
		}
		if (fActivatedOnTypes != null) {
			if (selection instanceof IStructuredSelection) {
				return isEnabled(((IStructuredSelection)selection).iterator());
			}
			return false;
		}
		return true;
	}

}
