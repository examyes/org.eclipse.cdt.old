package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/PICLLaunchWizardDialog.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 16:00:21)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Subclassed to provide access to button presses
 */
public class PICLLaunchWizardDialog extends WizardDialog {

	protected PICLLaunchWizard fWizard;

	/**
	 * Constructs a wizard dialog
	 */
	public PICLLaunchWizardDialog(Shell shell, PICLLaunchWizard w) {
		super(shell, w);
		fWizard= w;
	}
	protected void cancelPressed() {
		fWizard.performCancel();
		super.cancelPressed();
	}
}
