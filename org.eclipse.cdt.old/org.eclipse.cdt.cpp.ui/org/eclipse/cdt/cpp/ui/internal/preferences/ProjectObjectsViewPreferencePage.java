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

public class ProjectObjectsViewPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private Button _scopeView;

    public ProjectObjectsViewPreferencePage() 
    {
    }

    public void init(IWorkbench workbench) 
    {	
    }

    public Control createContents(Composite parent) 
    {	
	Composite control = new Composite(parent, SWT.NONE);

	_scopeView = new Button(control, SWT.CHECK); 
	_scopeView.setText("Scope to the currently selected file");

	performDefaults();
	
	control.setLayout(new GridLayout());

	return control;
    }	
    
    public void performDefaults() 
    {
	CppPlugin plugin      = CppPlugin.getDefault();
	ArrayList scopeView = plugin.readProperty("ScopeProjectObjectsView");
	if (scopeView.isEmpty())
	    {
		_scopeView.setSelection(false);
	    }
	else
	    {
		String preference = (String)scopeView.get(0);
		if (preference.equals("Yes"))
		    {
			_scopeView.setSelection(true);
		    }
		else
		    {
			_scopeView.setSelection(false);
		    }
	    }
    }

    public boolean performOk()
    {
	// auto parse
	ArrayList scopeView = new ArrayList();
	if (_scopeView.getSelection())
	    {
		scopeView.add("Yes");		
	    }
	else
	    {
		scopeView.add("No");		
	    }

	CppPlugin plugin      = CppPlugin.getDefault();
	plugin.writeProperty("ScopeProjectObjectsView", scopeView);	

	return true;
    }

}
