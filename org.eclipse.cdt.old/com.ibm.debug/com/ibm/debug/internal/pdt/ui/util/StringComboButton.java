package com.ibm.debug.internal.pdt.ui.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/util/StringComboButton.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 15:59:15)
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * This class provides a combo box (which contains both a list and
 * an editable text field) followed by a button.
 */

public class StringComboButton extends StringCombo {

    private Button fBrowseButton;
    private String fBrowseButtonLabel;
    private IStringButtonAdapter fStringButtonAdapter;

    private boolean fButtonEnabled;

    public StringComboButton(IStringButtonAdapter adapter) {
        super();
        fStringButtonAdapter= adapter;
        fBrowseButtonLabel= "!Browse...!";
        fButtonEnabled= true;
    }

    public void setButtonLabel(String label) {
        fBrowseButtonLabel= label;
    }

    // ------ adapter communication

    public void changeControlPressed() {
        fStringButtonAdapter.changeControlPressed(this);
    }

    // ------- layout helpers

    public Control[] doFillIntoGrid(Composite parent, int nColumns) {
        assertEnoughColumns(nColumns);

        Label label= getLabelControl(parent);
        label.setLayoutData(gridDataForLabel(1));
                Combo combo = getComboControl(parent);
                combo.setLayoutData(gridDataForCombo(nColumns - 2));
        Control button= getChangeControl(parent);
        button.setLayoutData(gridDataForControl(1));

        return new Control[] { label, combo, button };
    }

    public int getNumberOfControls() {
        return 3;
    }

    protected static MGridData gridDataForControl(int span) {
        MGridData gd= new MGridData();
        gd.horizontalAlignment= gd.FILL;
        gd.grabExcessHorizontalSpace= false;
        gd.horizontalSpan= span;
        return gd;
    }

    // ------- ui creation

    public Control getChangeControl(Composite parent) {
        if (fBrowseButton == null) {
            assertCompositeNotNull(parent);

            fBrowseButton= new Button(parent, SWT.PUSH);
            fBrowseButton.setText(fBrowseButtonLabel);
            fBrowseButton.setEnabled(isEnabled() && fButtonEnabled);
            fBrowseButton.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    changeControlPressed();
                }
                public void widgetSelected(SelectionEvent e) {
                    changeControlPressed();
                }
            });

        }
        return fBrowseButton;
    }

    // ------ enable / disable management

    public void enableButton(boolean enable) {
        if (isOkToUse(fBrowseButton)) {
            fBrowseButton.setEnabled(isEnabled() && enable);
        }
        fButtonEnabled= enable;
    }

    protected void updateEnableState() {
        super.updateEnableState();
        if (isOkToUse(fBrowseButton)) {
            fBrowseButton.setEnabled(isEnabled() && fButtonEnabled);
        }
    }

}
