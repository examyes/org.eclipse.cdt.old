package com.ibm.debug.internal.pdt.ui.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/util/SelectionButtonDialogField.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 15:58:35)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Radio or checkbox button
 */

public class SelectionButtonDialogField extends DialogField {

	private Button fButton;
	private boolean fIsSelected;
	private DialogField[] fAttachedDialogFields;
	private int fButtonStyle;

	/**
	 * styles: SWT.RADIO, SWT.CHECK, SWT.TOGGLE, SWT.PUSH
	 */
	public SelectionButtonDialogField(int buttonStyle) {
		super();
		fIsSelected= false;
		fAttachedDialogFields= null;
		fButtonStyle= buttonStyle;
	}

	public void attachDialogField(DialogField dialogField) {
		attachDialogFields(new DialogField[] { dialogField });
	}

	public void attachDialogFields(DialogField[] dialogFields) {
		fAttachedDialogFields= dialogFields;
		for (int i= 0; i < dialogFields.length; i++) {
			dialogFields[i].setEnabled(fIsSelected);
		}
	}

	public boolean isAttached(DialogField editor) {
		if (fAttachedDialogFields != null) {
			for (int i=0; i < fAttachedDialogFields.length; i++) {
				if (fAttachedDialogFields[i] == editor) {
					return true;
				}
			}
		}
		return false;
	}

	// ------- layout helpers

	public Control[] doFillIntoGrid(Composite parent, int nColumns) {
		assertEnoughColumns(nColumns);

		Button rbutton= getSelectionButton(parent);
		MGridData gd= new MGridData();
		gd.horizontalSpan= nColumns;
		gd.horizontalAlignment= MGridData.FILL;
		rbutton.setLayoutData(gd);

		return new Control[] { rbutton };
	}

	public int getNumberOfControls() {
		return 1;
	}

	// ------- ui creation

	public Button getSelectionButton(Composite group) {
		if (fButton == null) {
			assertCompositeNotNull(group);

			fButton= new Button(group, fButtonStyle);
			fButton.setFont(group.getFont());
			fButton.setText(fLabelText);
			fButton.setEnabled(isEnabled());
			fButton.setSelection(fIsSelected);
			fButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					doWidgetSelected(e);
				}
				public void widgetSelected(SelectionEvent e) {
					doWidgetSelected(e);
				}
			});
		}
		return fButton;
	}

	private void doWidgetSelected(SelectionEvent e) {
		if (isOkToUse(fButton)) {
			changeValue(fButton.getSelection());
		}
	}

	private void changeValue(boolean newState) {
		if (fIsSelected != newState) {
			fIsSelected= newState;
			if (fAttachedDialogFields != null) {
				boolean focusSet= false;
				for (int i= 0; i < fAttachedDialogFields.length; i++) {
					fAttachedDialogFields[i].setEnabled(fIsSelected);
					if (fIsSelected && !focusSet) {
						focusSet= fAttachedDialogFields[i].setFocus();
					}
				}
			}
			dialogFieldChanged();
		} else if (fButtonStyle == SWT.PUSH) {
			dialogFieldChanged();
		}
	}

	// ------ model access

	public boolean isSelected() {
		return fIsSelected;
	}

	public void setSelection(boolean selected) {
		changeValue(selected);
		if (isOkToUse(fButton)) {
			fButton.setSelection(selected);
		}
	}

	// ------ enable / disable management

	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fButton)) {
			fButton.setEnabled(isEnabled());
		}
	}




}
