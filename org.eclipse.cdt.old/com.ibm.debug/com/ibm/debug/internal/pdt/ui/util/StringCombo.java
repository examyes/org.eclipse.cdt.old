package com.ibm.debug.internal.pdt.ui.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/util/StringCombo.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 15:58:40)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class StringCombo extends DialogField{

	private String fText;
	private Combo fComboControl;
	private ModifyListener fModifyListener;

	public StringCombo() {
		super();
		fText= "";
	}

	// ------- layout helpers

	public Control[] doFillIntoGrid(Composite parent, int nColumns) {
		assertEnoughColumns(nColumns);

		Label label= getLabelControl(parent);
		label.setLayoutData(gridDataForLabel(1));
		Combo combo = getComboControl(parent);
		combo.setLayoutData(gridDataForCombo(nColumns - 1));

		return new Control[] { label, combo };
	}

	public int getNumberOfControls() {
		return 2;
	}

	protected static MGridData gridDataForCombo(int span) {
		MGridData gd= new MGridData();
		gd.horizontalAlignment= gd.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.grabColumn= 0;
		gd.horizontalSpan= span;
		return gd;
	}

	// ------- focus methods

	public boolean setFocus() {
		if (isOkToUse(fComboControl)) {
			fComboControl.setSelection(new Point(0, fComboControl.getText().length()));
			fComboControl.setFocus();
		}
		return true;
	}


	// ------- ui creation

	public Combo getComboControl(Composite parent) {
		if (fComboControl == null) {
			assertCompositeNotNull(parent);
			fModifyListener= new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					doModifyText(e);
				}
			};

			fComboControl= new Combo(parent, SWT.DROP_DOWN);
			fComboControl.setFont(parent.getFont());
			fComboControl.addModifyListener(fModifyListener);

			fComboControl.setText(fText);
			fComboControl.setEnabled(isEnabled());
		}
		return fComboControl;
	}

	private void doModifyText(ModifyEvent e) {
		if (isOkToUse(fComboControl)) {
			fText= fComboControl.getText();
		}
		dialogFieldChanged();
	}

	// ------ enable / disable management

	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fComboControl)) {
			fComboControl.setEnabled(isEnabled());
		}
	}

	// ------ text access

	/**
	 * Get the text
	 */
	public String getText() {
		return fText;
	}

	/**
	 * Set the text. Triggers an dialog-changed event
	 */
	public void setText(String text) {
		fText= text;
		if (isOkToUse(fComboControl)) {
			fComboControl.setText(text);
		} else {
			dialogFieldChanged();
		}
	}

	/**
	 * Set the text without triggering a dialog-changed event
	 */
	public void setTextWithoutUpdate(String text) {
		fText= text;
		if (isOkToUse(fComboControl)) {
			fComboControl.removeModifyListener(fModifyListener);
			fComboControl.setText(text);
			fComboControl.addModifyListener(fModifyListener);
		}
	}

	/**
 	 * Set the selection items for the combo box
 	 */
	public void setItems(String[] items) {
		fComboControl.setItems(items);
	}
}
