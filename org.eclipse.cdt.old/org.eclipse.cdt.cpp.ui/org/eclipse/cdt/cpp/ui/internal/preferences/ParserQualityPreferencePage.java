package com.ibm.cpp.ui.internal.preferences;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.wizards.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.builder.*;

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

public class ParserQualityPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private ParseQualityControl _control;

    public ParserQualityPreferencePage() 
    {
    }

    public void init(IWorkbench workbench) 
    {	
    }

    public Control createContents(Composite parent) 
    {	
	_control = new ParseQualityControl(parent, SWT.NONE);
	_control.setLayout(new FillLayout());
	performDefaults();
	return _control;
    }	
    
    public void performDefaults() 
    {
	CppPlugin plugin      = CppPlugin.getDefault();
	ArrayList preferences = plugin.readProperty("ParseQuality");
	if (preferences.isEmpty())
	    {
		_control.setSelection(3);
	    }
	else
	    {
		for (int i = 0; i < preferences.size(); i++)
		    {
			String preference = (String)preferences.get(i);
			int value = Integer.parseInt(preference);
			_control.setSelection(value);
		    }
	    }
    }

    public boolean performOk()
    {
	int value = _control.getSelection();

	ArrayList preferences = new ArrayList();
	preferences.add("" + value);

	CppPlugin plugin      = CppPlugin.getDefault();
	plugin.writeProperty("ParseQuality", preferences);	

	return true;
    }
}
