package com.ibm.cpp.ui.internal.preferences;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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

public class ParserPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
    private Button _autoParseButton;

    public ParserPreferencePage() 
    {
    }

    public void init(IWorkbench workbench) 
    {	
    }

    public Control createContents(Composite parent) 
    {	
	Composite control = new Composite(parent, SWT.NONE);

	_autoParseButton = new Button(control, SWT.CHECK);
	_autoParseButton.setText("Perform parse automatically on resource modification");
	performDefaults();
	
	control.setLayout(new GridLayout());

	return control;
    }	
    
    public void performDefaults() 
    {
	CppPlugin plugin      = CppPlugin.getDefault();
	ArrayList preferences = plugin.readProperty("AutoParse");
	if (preferences.isEmpty())
	    {
		_autoParseButton.setSelection(false);
	    }
	else
	    {
		String preference = (String)preferences.get(0);
		if (preference.equals("Yes"))
		    {
			_autoParseButton.setSelection(true);
		    }
		else
		    {
			_autoParseButton.setSelection(false);
		    }
	    }
    }

    public boolean performOk()
    {
	ArrayList preferences = new ArrayList();
	if (_autoParseButton.getSelection())
	    {
		preferences.add("Yes");		
	    }
	else
	    {
		preferences.add("No");		
	    }

	CppPlugin plugin      = CppPlugin.getDefault();
	plugin.writeProperty("AutoParse", preferences);	

	return true;
    }

}
