package org.eclipse.cdt.cpp.ui.internal.preferences;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.wizards.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.builder.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.util.*;

public class ParseBehaviourControl extends Composite
{
    private Button _autoParseButton;
    private Button _autoPersistButton;


    public ParseBehaviourControl(Composite cnr, int style)
    {
	super(cnr, style);

    	_autoParseButton = new Button(this, SWT.CHECK);
	_autoParseButton.setText("Perform project parse automatically on resource modification");
	
	_autoPersistButton = new Button(this, SWT.CHECK);
	_autoPersistButton.setText("Persist parse information across sessions");

	setLayout(new GridLayout());
    }


    public boolean getAutoParseSelection()
    {
	return _autoParseButton.getSelection();
    }

    public boolean getAutoPersistSelection()
    {
	return _autoPersistButton.getSelection();
    }

    public void setAutoParseSelection(boolean flag)
    {
	_autoParseButton.setSelection(flag);
    }

    public void setAutoPersistSelection(boolean flag)
    {
	_autoPersistButton.setSelection(flag);
    }

}
