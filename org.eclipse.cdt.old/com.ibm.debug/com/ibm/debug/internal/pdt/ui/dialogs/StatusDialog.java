package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/StatusDialog.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 15:58:58)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import com.ibm.debug.internal.pdt.ui.util.MessageLine;
import com.ibm.debug.internal.pdt.ui.util.StatusTool;
import org.eclipse.core.runtime.IStatus;


/**
 * An abstract base class for dialogs with a status bar and ok/cancel buttons.
 * The status message must be passed over as StatusInfo object and can be
 * an error, warning or ok. The OK button is enabled / disabled depending
 * on the status.
 */
public abstract class StatusDialog extends Dialog {

	private Button fOkButton;
	private MessageLine fStatusLine;
	private IStatus fLastStatus;
	private String fTitle;
	private Image fImage;

	private boolean fStatusLineAboveButtons;

	public StatusDialog(Shell parent) {
		super(parent);
		fStatusLineAboveButtons= false;
	}
	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (fTitle != null)
			shell.setText(fTitle);
	}
	/*
	 * Non Java-Docd
	 */
	public void create() {
		super.create();
		if (fLastStatus != null) {
			updateStatus(fLastStatus);
		}
	}
	/*
	 * Non Java-Docd
	 */
	protected Control createButtonBar(Composite parent) {
		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		layout.marginHeight= 0;
		layout.marginWidth= convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		fStatusLine= new MessageLine(composite);
		fStatusLine.setAlignment(SWT.LEFT);
		fStatusLine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fStatusLine.setMessage("");

		super.createButtonBar(composite);
		return composite;
	}
	/*
	 * Non Java-Docd
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		fOkButton= createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

	}
	protected IStatus getStatus() {
		return fLastStatus;
	}
	/**
	 * Sets the image for this dialog.
	 *
	 * @param image the dialog's image
	 */
	public void setImage(Image image) {
		fImage= image;
		Shell shell= getShell();
		if (shell != null && !shell.isDisposed())
			shell.setImage(fImage);
	}
	/**
	 * Controls whether status line appears to the left of the buttons (default)
	 * or above them.
	 *
	 * @param aboveButtons if <code>true</code> status line is placed above buttons; if
	 * 	<code>false</code> to the right
	 */
	public void setStatusLineAboveButtons(boolean aboveButtons) {
		fStatusLineAboveButtons= aboveButtons;
	}
	/**
	 * Sets the title for this dialog.
	 *
	 * @param title the title
	 */
	public void setTitle(String title) {
		fTitle= title != null ? title : "";
		Shell shell= getShell();
		if (shell != null && !shell.isDisposed())
			shell.setText(fTitle);
	}
	/**
	 * Update the status of the ok button to reflect the given status. Subclasses
	 * may override this method to update additional buttons.
	 */
	protected void updateButtonsEnableState(IStatus status) {
		if (fOkButton != null && !fOkButton.isDisposed())
			fOkButton.setEnabled(!status.matches(IStatus.ERROR));
	}
	/**
	 * Update the dialog's status line to reflect the given status. It is save to call
	 * this method before the dialog has been opened.
	 */
	protected void updateStatus(IStatus status) {
		fLastStatus= status;
		if (fStatusLine != null && !fStatusLine.isDisposed()) {
			updateButtonsEnableState(status);
			StatusTool.applyToStatusLine(fStatusLine, status);
		}
	}
}
