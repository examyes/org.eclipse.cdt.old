package com.ibm.debug.internal.pdt.ui.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/util/CheckedListDialogField.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 15:58:25)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;

public class CheckedListDialogField extends ListDialogField {

	private Button fCheckAllButton;
	private Button fUncheckAllButton;
	private String fCheckAllButtonLabel;
	private String fUncheckAllButtonLabel;

	private List fCheckElements;

	/**
	 * Create a table without custom  / remove buttons
	 */
	public CheckedListDialogField(ILabelProvider lprovider, int config) {
		super(lprovider, config);
		fCheckElements= new ArrayList();
		fCheckAllButtonLabel= "!Check All!";
		fUncheckAllButtonLabel= "!Uncheck All!";
	}

	public CheckedListDialogField(IListAdapter adapter, String[] customButtonLabels, ILabelProvider lprovider, int config) {
		super(adapter, customButtonLabels, lprovider, config);
		fCheckElements= new ArrayList();
		fCheckAllButtonLabel= "!Check All!";
		fUncheckAllButtonLabel= "!Uncheck All!";
	}

	public void setCheckAllButtonLabel(String checkButtonLabel) {
		fCheckAllButtonLabel= checkButtonLabel;
	}

	public void setUncheckAllButtonLabel(String uncheckButtonLabel) {
		fUncheckAllButtonLabel= uncheckButtonLabel;
	}


	// hook to create the CheckboxTableViewer
	protected TableViewer createTableViewer(Composite parent) {
		Table table= new Table(parent, SWT.CHECK + getListStyle());
		CheckboxTableViewer tableViewer= new CheckboxTableViewer(table);
		tableViewer.addCheckStateListener(new CheckListener());
		return tableViewer;
	}


	// hook to set the checked elements (can only be done after widget creation)
	public Control getListControl(Composite parent) {
		Control control= super.getListControl(parent);
		((CheckboxTableViewer)fTable).setCheckedElements(fCheckElements.toArray());
		return control;
	}

	// hook to add some own buttons
	protected void createExtraButtons(Composite parent) {
		SelectionListener listener= new CheckListener();

		fCheckAllButton= createButton(parent, fCheckAllButtonLabel, listener);
		fUncheckAllButton= createButton(parent, fUncheckAllButtonLabel, listener);
	}

	// hook in to get element changes to update check model
	public void dialogFieldChanged() {
		for (int i= fCheckElements.size() -1; i >= 0; i--) {
			if (!fElements.contains(fCheckElements.get(i))) {
				fCheckElements.remove(i);
			}
		}
		super.dialogFieldChanged();
	}

	private void checkStateChanged() {
		//call super and do not update check model
		super.dialogFieldChanged();
	}

	// ------ enable / disable management

	private void updateCheckButtonState() {
		if (fTable != null) {
			boolean enabled= !fElements.isEmpty() && isEnabled();
			if (isOkToUse(fCheckAllButton)) {
				fCheckAllButton.setEnabled(enabled);
			}
			if (isOkToUse(fUncheckAllButton)) {
				fUncheckAllButton.setEnabled(enabled);
			}
		}
	}


	/**
	 * @see ListDialogField#updateButtonState
	 */
	protected void updateButtonState() {
		super.updateButtonState();
		updateCheckButtonState();
	}

	// ------ model access


	public List getCheckedElements() {
		return new ArrayList(fCheckElements);
	}


	public void setCheckedElements(List list) {
		fCheckElements= list;
		if (fTable != null) {
			((CheckboxTableViewer)fTable).setCheckedElements(list.toArray());
		}
		checkStateChanged();
	}

	public void setChecked(Object object, boolean state) {
		if (!fCheckElements.contains(object)) {
			fCheckElements.add(object);
		}
		if (fTable != null) {
			((CheckboxTableViewer)fTable).setChecked(object, state);
		}
		checkStateChanged();
	}

	public void checkAll(boolean state) {
		if (state) {
			fCheckElements= getElements();
		} else {
			fCheckElements.clear();
		}
		if (fTable != null) {
			((CheckboxTableViewer)fTable).setAllChecked(state);
		}
		checkStateChanged();
	}


	// ------- CheckListener

	private class CheckListener implements SelectionListener, ICheckStateListener {

		// ------- SelectionListener

		public void widgetDefaultSelected(SelectionEvent e) {
			doCheckButtonPressed(e);
		}
		public void widgetSelected(SelectionEvent e) {
			doCheckButtonPressed(e);
		}

		// ------- ICheckStateListener

		public void checkStateChanged(CheckStateChangedEvent e) {

			doCheckStateChanged(e);
		}
	}

	private void doCheckStateChanged(CheckStateChangedEvent e) {
		if (e.getChecked()) {
			fCheckElements.add(e.getElement());
		} else {
			fCheckElements.remove(e.getElement());
		}
		checkStateChanged();
	}

	private void doCheckButtonPressed(SelectionEvent e) {
		if (e.widget == fCheckAllButton) {
			checkAll(true);
			return;
		} else if (e.widget == fUncheckAllButton) {
			checkAll(false);
			return;
		}
	}





}
