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

public class AutoconfControl extends Composite
{
    private Button _autoRunUpdateButton;
    private Button _showRunDialgtButton;

    private Button _autoCreateUpdateButton;
    private Button _showCreateDialgtButton;

    public AutoconfControl(Composite cnr, int style)
    {
		super(cnr, style);

    	Label Autoupdate = new Label(this,SWT.LEFT);
    	Autoupdate.setText("Automatic Update setup:");
    	
    	_autoRunUpdateButton = new Button(this, SWT.CHECK);
		_autoRunUpdateButton.setText("Perform automatic update whenever run configure is executed");
	
    	_autoCreateUpdateButton = new Button(this, SWT.CHECK);
		_autoCreateUpdateButton.setText("Perform automatic update whenever create configure is executed");
	
		new Label(this,SWT.NONE);
	   	Label dialogSetup = new Label(this,SWT.LEFT);
    	dialogSetup.setText("Dialog show/hide setup:");
    	
		_showRunDialgtButton = new Button(this, SWT.CHECK);
		_showRunDialgtButton.setText("Show run configure Message Dialg before execution");
	
		_showCreateDialgtButton = new Button(this, SWT.CHECK);
		_showCreateDialgtButton.setText("Show create configure Message Dialg before execution");

		setLayout(new GridLayout());
    }


    public boolean getAutoRunUpdateSelection()
    {
		return _autoRunUpdateButton.getSelection();
    }

    public boolean getShowRunDialogSelection()
    {
		return _showRunDialgtButton.getSelection();
    }
    
    
    public boolean getAutoCreateUpdateSelection()
    {
		return _autoCreateUpdateButton.getSelection();
    }

    public boolean getShowCreateDialogSelection()
    {
		return _showCreateDialgtButton.getSelection();
    }


    public void setAutoRunUpdateSelection(boolean flag)
    {
		_autoRunUpdateButton.setSelection(flag);
    }

    public void setShowRunDialogSelection(boolean flag)
    {
		_showRunDialgtButton.setSelection(flag);
    }



    public void setAutoCreateUpdateSelection(boolean flag)
    {
		_autoCreateUpdateButton.setSelection(flag);
    }

    public void setShowCreateDialogSelection(boolean flag)
    {
		_showCreateDialgtButton.setSelection(flag);
    }


}
