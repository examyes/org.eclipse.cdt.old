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

public class ParseQualityControl extends Composite
{
    private Label _quality;
    private Label _speed;
    private Scale _scale;
    private Text _description;
    private CppPlugin _plugin;

    public ParseQualityControl(Composite cnr, int style)
    {
	super(cnr, style);
	_plugin = CppPlugin.getDefault();

	Group group = new Group(this, SWT.NONE);
	group.setText(_plugin.getLocalizedString("ParsePreferences.Speed_Quality_of_Analysis"));
	
	Composite scontrol = new Composite(group, SWT.NONE);

	Label s = new Label(scontrol, SWT.NONE);
	s.setText(_plugin.getLocalizedString("ParsePreferences.Fast"));

	_scale = new Scale(scontrol, SWT.HORIZONTAL);
	_scale.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
	_scale.setMinimum(0);
	_scale.setMaximum(3);
	_scale.setIncrement(1);
	_scale.setPageIncrement(1);
	_scale.addSelectionListener(new SelectionListener()
	    {
		public void widgetSelected(SelectionEvent e) 
		{
		    int selection = _scale.getSelection();
		    describeSelection(selection);
		}
		
		public void widgetDefaultSelected(SelectionEvent e) 
		{
		    int selection = _scale.getSelection();
		    describeSelection(selection);
		}
	    });

	
	Label q = new Label(scontrol, SWT.NONE);
	q.setText(_plugin.getLocalizedString("ParsePreferences.Comprehensive"));

	GridLayout slayout = new GridLayout();
	slayout.numColumns = 3;
	scontrol.setLayout(slayout);
	GridData dp0 = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
	scontrol.setLayoutData(dp0);

	_description = new Text(group, SWT.WRAP | SWT.MULTI | SWT.READ_ONLY);
	GridData dData = new GridData(GridData.GRAB_VERTICAL |GridData.FILL_BOTH);
	dData.heightHint = 80;
	dData.widthHint  = 160;
	_description.setLayoutData(dData);

	GridLayout layout = new GridLayout();
	layout.numColumns = 1;
	group.setLayout(layout);
	group.setLayoutData(new GridData(GridData.FILL_BOTH));
    }	

    public int getSelection()
    {
	return _scale.getSelection();
    }    

    public void setSelection(int value)
    {
	_scale.setSelection(value);
	describeSelection(value);
    }

    public void describeSelection(int selection)
    {
	switch (selection)
	    {
	    case 0:
		_description.setText(_plugin.getLocalizedString("ParsePreferences.q0"));
		break;
	    case 1:
		_description.setText(_plugin.getLocalizedString("ParsePreferences.q1"));
		break;
	    case 2:	
		_description.setText(_plugin.getLocalizedString("ParsePreferences.q2"));
		break;
	    case 3:
		_description.setText(_plugin.getLocalizedString("ParsePreferences.q3"));
		break;
	    default:
	    }
    }
}
