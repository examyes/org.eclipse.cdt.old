package org.eclipse.cdt.cpp.ui.internal.preferences;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
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
   
    private Button _showConfigureDialogtButton;
    private Button _autoRunUpdateButton;
    private Button _showRunDialogtButton;

    private Button _autoConfigureUpdateButton;
    private Button _autoCreateUpdateButton;
    private Button _showCreateDialogtButton;
    
    private Button _updateAllButton;
    private Button _updateConfigureInButton;
    private Button _updateMakefileAmButton;

    public AutoconfControl(Composite cnr, int style)
    {
		super(cnr, style);

    	
    	Group autoUpdateGroup = new Group(this,SWT.NONE);
    	autoUpdateGroup.setText("Autoconf settings for handling automatic update:");
    	GridLayout autoLayout = new GridLayout();
    	autoLayout.numColumns = 1;
    	autoUpdateGroup.setLayout(autoLayout);
 		autoUpdateGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

		Composite auotUpdateComp = new Composite(autoUpdateGroup,SWT.NONE);
    	GridLayout autoCompLayout = new GridLayout();
    	autoCompLayout.numColumns = 1;
    	auotUpdateComp.setLayout(autoCompLayout);
 		auotUpdateComp.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		
    	
    	_autoConfigureUpdateButton = new Button(auotUpdateComp, SWT.CHECK);
		_autoConfigureUpdateButton.setText("Perform automatic update whenever configure is executed");

		new Label(auotUpdateComp,SWT.NONE);
		
    	_autoCreateUpdateButton = new Button(auotUpdateComp, SWT.CHECK);
		_autoCreateUpdateButton.setText("Perform automatic update whenever generate configure is executed");
	
	   	_autoRunUpdateButton = new Button(auotUpdateComp, SWT.CHECK);
		_autoRunUpdateButton.setText("Perform automatic update whenever run configure is executed");
	
	
    	Group dialogGroup = new Group(this,SWT.NONE);
    	dialogGroup.setText("Show/hide settings for pop up dialogs when autoconf actions performed:");
    	GridLayout dialogLayout = new GridLayout();
    	dialogLayout.numColumns = 1;
    	dialogGroup.setLayout(dialogLayout);
 		dialogGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

		Composite dialogComp = new Composite(dialogGroup,SWT.NONE);
    	GridLayout dialogCompLayout = new GridLayout();
    	dialogCompLayout.numColumns = 1;
    	dialogComp.setLayout(dialogCompLayout);
 		dialogComp.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

		_showConfigureDialogtButton = new Button(dialogComp, SWT.CHECK);
		_showConfigureDialogtButton.setText("Show configure dialog before execution");

		new Label(dialogComp,SWT.NONE);
	   		
		_showCreateDialogtButton = new Button(dialogComp, SWT.CHECK);
		_showCreateDialogtButton.setText("Show \"Generate configure\" dialog before execution");
		
		_showRunDialogtButton = new Button(dialogComp, SWT.CHECK);
		_showRunDialogtButton.setText("Show \"Run configure\" dialog before execution");

		new Label(dialogComp,SWT.NONE);

		_updateAllButton = new Button(dialogComp, SWT.CHECK);
		_updateAllButton.setText("Show \"Generate/Update all automake files\" dialog before execution");
	
		_updateConfigureInButton = new Button(dialogComp, SWT.CHECK);
		_updateConfigureInButton.setText("Show \"Update configure.in\" dialog before execution");

		_updateMakefileAmButton = new Button(dialogComp, SWT.CHECK);
		_updateMakefileAmButton.setText("Show \"Update Makefile.am\" dialog before execution");



		setLayout(new GridLayout());
    }


	// gets

    public boolean getAutoConfigureUpdateSelection()
    {
		return _autoConfigureUpdateButton.getSelection();
    }

    public boolean getShowConfigureDialogSelection()
    {
		return _showConfigureDialogtButton.getSelection();
    }
    public boolean getAutoRunUpdateSelection()
    {
		return _autoRunUpdateButton.getSelection();
    }

    public boolean getShowRunDialogSelection()
    {
		return _showRunDialogtButton.getSelection();
    }
    
    
    public boolean getAutoCreateUpdateSelection()
    {
		return _autoCreateUpdateButton.getSelection();
    }

    public boolean getShowCreateDialogSelection()
    {
		return _showCreateDialogtButton.getSelection();
    }
    public boolean getUpdateAllButtonSelection()
    {
		return _updateAllButton.getSelection();
    }
    public boolean getUpdateConfigureInButtonSelection()
	{
   		return _updateConfigureInButton.getSelection();
	}
	public boolean getUpdateMakefileAmButtonSelection()
	{
   		return _updateMakefileAmButton.getSelection();
	}
   
    
    // sets
    
    public void setAutoConfigureUpdateSelection(boolean flag)
    {
		_autoConfigureUpdateButton.setSelection(flag);
    }

    public void setShowConfigureDialogSelection(boolean flag)
    {
		_showConfigureDialogtButton.setSelection(flag);
    }
   
    public void setAutoRunUpdateSelection(boolean flag)
    {
		_autoRunUpdateButton.setSelection(flag);
    }

    public void setShowRunDialogSelection(boolean flag)
    {
		_showRunDialogtButton.setSelection(flag);
    }
    public void setAutoCreateUpdateSelection(boolean flag)
    {
		_autoCreateUpdateButton.setSelection(flag);
    }

    public void setShowCreateDialogSelection(boolean flag)
    {
		_showCreateDialogtButton.setSelection(flag);
    }

    public void setUpdateAllButtonSelection(boolean flag)
    {
		_updateAllButton.setSelection(flag);
    }
    public void setUpdateConfigureInButtonSelection(boolean flag)
	{
   		_updateConfigureInButton.setSelection(flag);
	}
	public void setUpdateMakefileAmButtonSelection(boolean flag)
	{
   		_updateMakefileAmButton.setSelection(flag);
	}

}
