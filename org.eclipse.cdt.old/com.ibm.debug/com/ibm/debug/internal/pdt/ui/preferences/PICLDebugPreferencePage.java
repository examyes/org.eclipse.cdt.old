package com.ibm.debug.internal.pdt.ui.preferences;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/preferences/PICLDebugPreferencePage.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:00:12)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ibm.debug.PICLDebugPlugin;
import com.ibm.debug.internal.pdt.ui.util.MGridData;
import com.ibm.debug.internal.pdt.ui.util.MGridLayout;
import com.ibm.debug.internal.pdt.ui.util.Separator;
import com.ibm.debug.internal.pdt.ui.util.StatusInfo;
import com.ibm.debug.internal.pdt.ui.util.StringDialogField;
import com.ibm.debug.internal.picl.PICLUtils;

public class PICLDebugPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    private static final String PAGE_NAME = "PICLDebugPreferencePage";

    private StringDialogField fPort;
    private IPreferenceStore fPreferenceStore;

    public PICLDebugPreferencePage() {
        fPreferenceStore = PICLDebugPlugin.getDefault().getPreferenceStore();
    }

    public void init(IWorkbench workbench) {
    }

    /**
     * @see PreferencePage#createContents
     */
    public Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        int nColumns = 2;

        MGridLayout layout = new MGridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = nColumns;
        composite.setLayout(layout);

        fPort = new StringDialogField();
        fPort.setLabelText(PICLUtils.getResourceString(PAGE_NAME+".portLabel"));
        fPort.doFillIntoGrid(composite, nColumns);

        new Separator().doFillIntoGrid(composite, 1, 1);

        Label label = new Label(composite, SWT.WRAP | SWT.LEFT);
        label.setText(PICLUtils.getResourceString(PAGE_NAME+".portNote"));
        MGridData gridData = new MGridData();
        gridData.horizontalAlignment = gridData.FILL;
        gridData.horizontalSpan = 1;
        label.setLayoutData(gridData);

        restoreSettings();

        return composite;
    }

    /**
     * @see PreferencePage#performDefaults
     */
    public void performDefaults() {
        String port = fPreferenceStore.getDefaultString(PICLDebugPlugin.DAEMON_PORT);
        fPort.setText(port);
    }

    protected void restoreSettings() {
        String port = fPreferenceStore.getString(PICLDebugPlugin.DAEMON_PORT);
        fPort.setText(port);
    }

    /**
     * @see PreferencePage#performOk
     */
    public boolean performOk() {
        boolean result = false;

        String port = fPort.getText();
        if(!port.equals("")) {
            try {
                Integer.parseInt(port);
                fPreferenceStore.setValue(PICLDebugPlugin.DAEMON_PORT, port);
                result = true;
            } catch(NumberFormatException e) {
            }
        }

        if (result == false) {
            StatusInfo status = new StatusInfo();
            status.setError(PICLUtils.getResourceString(PAGE_NAME+".portMustBeIntError"));
            ErrorDialog.openError(getShell(), PICLUtils.getResourceString("ErrorDialog.error"), null, status);
        }

        return result;
    }
}
