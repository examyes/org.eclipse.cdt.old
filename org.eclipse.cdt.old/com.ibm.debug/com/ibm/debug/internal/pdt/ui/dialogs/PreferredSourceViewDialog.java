package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/PreferredSourceViewDialog.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 16:00:03)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.EPDC;
import com.ibm.debug.internal.picl.PICLDebugTarget;
import com.ibm.debug.internal.picl.PICLUtils;
import com.ibm.debug.model.Host;
import com.ibm.debug.model.ViewInformation;
import java.lang.reflect.Array;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;

public class PreferredSourceViewDialog extends StatusDialog {

	// Class to keep track of what view is associated with which button
	class ButtonViewPair {
		ViewInformation view;
		Button button;
		ButtonViewPair(Button b, ViewInformation v) {
			button = b;
			view = v;
		}
	}
	protected final static String PREFIX= "PreferredSourceViewDialog";
	private PICLDebugTarget debugTarget = null;
	ButtonViewPair buttonViewPairs[] = null;
	int numButtons = 0;  // number of view buttons in dialog
	/**
	 * Constructor for PreferredSourceViewDialog
	 */
	public PreferredSourceViewDialog(Shell parent, PICLDebugTarget target) {
		super(parent);
		debugTarget = target;
		setTitle(PICLUtils.getResourceString(PREFIX+".title"));
		WorkbenchHelp.setHelp(parent,  new Object[] {PICLUtils.getHelpResourceString("PreferredSourceDialog")});
	}

	/**
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		// create a composite with standard margins and spacing
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;

		composite.setLayout(layout);
		composite.setFont(parent.getFont());

		Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
		label.setText(PICLUtils.getResourceString(PREFIX+".description"));

		Button button = null;

		ViewInformation preferredView = debugTarget.getPreferredView();
		short preferredKind = EPDC.View_Class_Source;
		if (preferredView != null)
			preferredKind = preferredView.kind();

		// Add radio button for each view supported by current engine

		ViewInformation engineViews[] = debugTarget.getDebugEngine().supportedViews();


		Button defaultButton = null;
		boolean useStatementView = false;
		if (debugTarget.getDebugEngine().host().getPlatformID() == Host.OS400)
			useStatementView = true;

		int viewCount = (engineViews == null ? 0 : engineViews.length);
		if (viewCount > 0)
			buttonViewPairs = new ButtonViewPair[viewCount];
		int index = 0;
		int i = 0;  //loop counter
		short kind = 0;
		for (i = 0; i < viewCount; i++)	{
			if (engineViews[i] == null) continue;
			ViewInformation vi = engineViews[i];
			if (vi == null) continue;
			kind = vi.kind();

			if (kind == EPDC.View_Class_Source) {
				button = new Button(composite, SWT.RADIO);
				button.setText(PICLUtils.getResourceString(PREFIX+".sourceButton"));
				buttonViewPairs[index] = new ButtonViewPair(button, vi);
				index++;
			}
			else if (kind == EPDC.View_Class_Disasm) {
				button = new Button(composite, SWT.RADIO);
				if (useStatementView)
					button.setText(PICLUtils.getResourceString(PREFIX+".statementButton"));
				else
					button.setText(PICLUtils.getResourceString(PREFIX+".disassemblyButton"));
				buttonViewPairs[index] = new ButtonViewPair(button, vi);
				index++;
			}
			else if (kind == EPDC.View_Class_Mixed) {
				button = new Button(composite, SWT.RADIO);
				button.setText(PICLUtils.getResourceString(PREFIX+".mixedButton"));
				buttonViewPairs[index] = new ButtonViewPair(button, vi);
				index++;
			}
			else if (kind == EPDC.View_Class_Listing) {
				button = new Button(composite, SWT.RADIO);
				button.setText(PICLUtils.getResourceString(PREFIX+".listingButton"));
				buttonViewPairs[index] = new ButtonViewPair(button, vi);
				index++;
			}
			if (kind == preferredKind)
				defaultButton = button;
			if (defaultButton == null)
				// must be adding first button, so set as default
				defaultButton = button;
		}

		if (defaultButton != null)
			defaultButton.setSelection(true);
		numButtons = index;
		return composite;

	}

	/**
	 * @see Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		super.cancelPressed();
	}

	/**
	 * @see Dialog#okPressed()
	 */
	protected void okPressed() {
		// Tell the DebugTarget about the new preferred view
		for (int i = 0; i < numButtons; i++) {
			if (buttonViewPairs[i].button.getSelection()) {
				debugTarget.setPreferredView(buttonViewPairs[i].view);
				break;
			}
		}
		super.okPressed();
	}

}

