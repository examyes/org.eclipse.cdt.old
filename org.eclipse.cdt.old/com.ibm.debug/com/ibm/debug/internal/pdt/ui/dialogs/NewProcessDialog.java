package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/NewProcessDialog.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 16:01:00)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;

import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLUtils;

public class NewProcessDialog extends Dialog {

	private Button okButton;
	private Button debugInitButton;
	private int result = -1;
	private PICLDebugTarget debugTarget;
	private static final String PREFIX = "NewProcessDialog";
	public static final int DEBUG_INITIALIZATION = 0;
	public static final int RUN_TO_MAIN = 1;


	public NewProcessDialog(Shell shell, PICLDebugTarget target)
	{
		super(shell);
		debugTarget = target;
		setDefaultImage(getImage(DLG_IMG_QUESTION));
		WorkbenchHelp.setHelp(shell,  new Object[] {PICLUtils.getHelpResourceString("NewProcessDialog")});
	}

	/* (non-Javadoc)
 	 * Method declared in Window.
 	 * Caller can pass in PICLDebugPlugin.getActiveWorkbenchShell() as the shell.
 	 */
	protected void configureShell(Shell shell)
	{
		super.configureShell(shell);
		shell.setText(PICLUtils.getResourceString(PREFIX+".title"));
	}

	/**
 	* Creates and returns the contents of the upper part
 	* of this dialog (above the button bar).
 	* <p>
 	* The <code>Dialog</code> implementation of this framework method
 	* creates and returns a new <code>Composite</code> with
 	* standard margins and spacing. Subclasses should override.
 	* </p>
 	*
 	* @param the parent composite to contain the dialog area
 	* @return the dialog area control
 	*/
	protected Control createDialogArea(Composite parent) {
		// create a composite with standard margins and spacing
		Composite composite = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());

		Label l= new Label(composite, SWT.NULL);
		l.setText(PICLUtils.getResourceString(PREFIX+".message"));

		// Get the name of the new process from the ProcessStopInfo
		String name = debugTarget.getProcessStopInfo().getExceptionMsg();

		l= new Label(composite, SWT.NULL);
		l.setText(name);

		debugInitButton = new Button(composite, SWT.CHECK);
		debugInitButton.setText(PICLUtils.getResourceString(PREFIX+".debugInitializationCheckbox"));

		Button b = new Button(composite, SWT.NULL);
		b.setText(PICLUtils.getResourceString(PREFIX+".defaultButton"));
		return composite;
	}

	/* (non-Javadoc)
 	* Method declared on Dialog.
 	* Override to remove cancel button.  The user must make a selection.
 	*/
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}


	protected void okPressed() {
		if(debugInitButton.getSelection())
			result = DEBUG_INITIALIZATION;
		else
			result = RUN_TO_MAIN;
		debugTarget.setNewProcessDialogAnswer(result);
		super.okPressed();
	}


	/**
 	* Notifies that the window's close button was pressed,
 	* the close menu was selected, or the ESCAPE key pressed.
 	* We choose to do nothing here. The user must give a response.
 	* <p>
 	* The default implementation of this framework method
	* sets the window's return code to <code>CANCEL</code>
 	* and closes the window using <code>close</code>.
 	* Subclasses may extend or reimplement.
	* </p>
	* See Window.handleShellCloseEvent()
 	*/

	//TODO: figure out how to remove/disable the close button.
	protected void handleShellCloseEvent() {}

	/**
	 * Returns the user's selection. Will be of constant
	 * type DEBUG_INITIALIZATION or RUN_TO_MAIN.  Will be -1 if selection has not occurred.
	 */
	public int getResult()
	{
		return result;
	}

}

