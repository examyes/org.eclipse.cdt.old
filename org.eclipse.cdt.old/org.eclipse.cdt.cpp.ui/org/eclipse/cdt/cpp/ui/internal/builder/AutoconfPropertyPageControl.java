package org.eclipse.cdt.cpp.ui.internal.builder;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.wizards.*;
import org.eclipse.cdt.cpp.ui.internal.*;

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

import com.ibm.debug.model.Line;

public class AutoconfPropertyPageControl extends Composite
{
   
    private Button _showConfigureDialogtButton;
    private Button _showRunDialogtButton;
    private Button _showCreateDialogtButton;
    
    private Button _updateAllButton;
    private Button _updateConfigureInButton;
    private Button _updateMakefileAmButton;
    
    protected Button globalSettingsButton;
    
    // labels
    private Label configureDialogSetup;
    private Label advancedSetup;
    

    public AutoconfPropertyPageControl(Composite cnr, int style)
    {
		super(cnr, style);
		
		configureDialogSetup = new Label(this,SWT.LEFT);
    	configureDialogSetup.setText("Autoconf actions message dialog behaviour setup:");

		_showConfigureDialogtButton = new Button(this, SWT.CHECK);
		_showConfigureDialogtButton.setText("Show configure dialog before execution");
		
		new Label(this,SWT.NONE);
	   	advancedSetup = new Label(this,SWT.LEFT);
    	advancedSetup.setText("Advanced actions' message dialog behaviour setup:");
    	
		_updateAllButton = new Button(this, SWT.CHECK);
		_updateAllButton.setText("Show \"Generate/Update all automake files\" dialog before execution");
	
		_updateConfigureInButton = new Button(this, SWT.CHECK);
		_updateConfigureInButton.setText("Show \"Update configure.in\" dialog before execution");

		_updateMakefileAmButton = new Button(this, SWT.CHECK);
		_updateMakefileAmButton.setText("Show \"Update Makefile.am\" dialog before execution");
		
		_showCreateDialogtButton = new Button(this, SWT.CHECK);
		_showCreateDialogtButton.setText("Show \"generate configure\" dialog before execution");
		
		_showRunDialogtButton = new Button(this, SWT.CHECK);
		_showRunDialogtButton.setText("Show \"run configure\" dialog before execution");

		
		new Label(this,SWT.LEFT);
		new Label(this,SWT.LEFT);
		globalSettingsButton = new Button(this, SWT.CHECK);
		globalSettingsButton.setText("Apply preference's page settings");
		

		setLayout(new GridLayout());
    }

	// gets
	
    public boolean getShowConfigureDialogSelection()
    {
		return _showConfigureDialogtButton.getSelection();
    }

    
    public boolean getShowRunDialogSelection()
    {
		return _showRunDialogtButton.getSelection();
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
	public boolean getGlobalSettingsSelection()
    {
		return globalSettingsButton.getSelection();
    }
    
    // sets
    

    public void setShowConfigureDialogSelection(boolean flag)
    {
		_showConfigureDialogtButton.setSelection(flag);
    }
   
    public void setShowRunDialogSelection(boolean flag)
    {
		_showRunDialogtButton.setSelection(flag);
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
	public void setGlobalSettingsSelection(boolean flag)
    {
		globalSettingsButton.setSelection(flag);
    }
    
    ///////////////
    
    public void enableLocalActions(boolean flg)
    {
    	this.advancedSetup.setEnabled(flg);
    	this.configureDialogSetup.setEnabled(flg);
    	//////////////
    	this._showConfigureDialogtButton.setEnabled(flg);
    	this._showCreateDialogtButton.setEnabled(flg);
     	this._showRunDialogtButton.setEnabled(flg);
    	this._updateAllButton.setEnabled(flg);
    	this._updateConfigureInButton.setEnabled(flg);
    	this._updateMakefileAmButton.setEnabled(flg);   	
    }

}
