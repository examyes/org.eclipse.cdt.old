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

public class AutoconfBuilderPropertyPageControl extends Composite
{
   
    //private Button _showConfigureDialogtButton;
    //private Button _showRunDialogtButton;
    //private Button _showCreateDialogtButton;
    
    //private Button _updateAllButton;
    //private Button _updateConfigureInButton;
    private Button _debuggableButton;
    private Button _optimizedButton;
    private Group _execGroup;
  //  private Group _advancedGroup;
   // private Group _advancedConfigureGroup;
    
    //protected Button globalSettingsButton;
    
    // labels
    //private Label configureDialogSetup;
   // private Label advancedSetup;
    
    private CppPlugin _plugin;

    public AutoconfBuilderPropertyPageControl(Composite cnr, int style)
    {
		super(cnr, style);
		
		_plugin = CppPlugin.getDefault();
		
		GridLayout layout = new GridLayout();
	   	layout.numColumns = 1;
	   	
	   	
		// group #1 - executable group
		
		_execGroup = new Group(this,SWT.NONE);
		_execGroup.setText("Executable type:");
		GridLayout g1Layout = new GridLayout();
	   	g1Layout.numColumns = 1;
	   	
		_execGroup.setLayout(g1Layout);
		_execGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));

		Composite execComp = new Composite(_execGroup,SWT.NONE);
		
		GridLayout c1Layout = new GridLayout();
	   	c1Layout.numColumns = 1;
		execComp.setLayout(c1Layout);

		_debuggableButton = new Button(execComp, SWT.RADIO);
		_debuggableButton.setText("Debug executable");	   	

		_optimizedButton = new Button(execComp, SWT.RADIO);
		_optimizedButton.setText("Optimized executable");
		
/*	   	// group #2 - advanced autoconf files group
	   	
	   	_advancedGroup = new Group(this,SWT.NONE);
	   	//_advancedGroup.setText(_plugin.getLocalizedString("AutoconfPoperties.Advanced_Group_Title"));
		_advancedGroup.setText("Advanced actions' message dialog setup:");
		
		GridLayout g2Layout = new GridLayout();
	   	g2Layout.numColumns = 1;
		
		_advancedGroup.setLayout(g2Layout);
		_advancedGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
	   	   
	   	Composite advancedComp = new Composite(_advancedGroup,SWT.NONE);
	   	GridLayout c2Layout = new GridLayout();
	   	c2Layout.numColumns = 1;
	   	advancedComp.setLayout(c2Layout);
	   	advancedComp.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
	   	
		_updateAllButton = new Button(advancedComp, SWT.CHECK);
		_updateAllButton.setText("Show \"Generate/Update all automake files\" dialog before execution");
	
		_updateConfigureInButton = new Button(advancedComp, SWT.CHECK);
		_updateConfigureInButton.setText("Show \"Update configure.in\" dialog before execution");

		_updateMakefileAmButton = new Button(advancedComp, SWT.CHECK);
		_updateMakefileAmButton.setText("Show \"Update Makefile.am\" dialog before execution");
		
		// group #3 - advanced configure group
		
		_advancedConfigureGroup = new Group(this,SWT.NONE);
		_advancedConfigureGroup.setText("Advanced configure Actions:");
		
		GridLayout g3Layout = new GridLayout();
	   	g3Layout.numColumns = 1;
		
		
		_advancedConfigureGroup.setLayout(g3Layout);
		_advancedConfigureGroup.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		
		Composite advConfComp = new Composite(_advancedConfigureGroup,SWT.NONE);
		GridLayout c3Layout = new GridLayout();
	   	c3Layout.numColumns = 1;
		advConfComp.setLayout(c3Layout);
		advConfComp.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		
		_showCreateDialogtButton = new Button(advConfComp, SWT.CHECK);
		_showCreateDialogtButton.setText("Show \"generate configure\" dialog before execution");
		
		_showRunDialogtButton = new Button(advConfComp, SWT.CHECK);
		_showRunDialogtButton.setText("Show \"run configure\" dialog before execution");

		new Label(this,SWT.LEFT);
		new Label(this,SWT.LEFT);
		globalSettingsButton = new Button(this, SWT.CHECK);
		globalSettingsButton.setText("Apply preference's page settings");*/

		setLayout(layout);
    }

	// gets
	
     
    public boolean getDebugButonSelection()
    {
		return _debuggableButton.getSelection();
    }
    public boolean getOptimizedButtonSelection()
    {
		return _optimizedButton.getSelection();
    }


    
/*    public boolean getShowCreateDialogSelection()
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
    }*/
    
    // sets
    

    public void setDebugButonSelection(boolean flag)
    {
		_debuggableButton.setSelection(flag);
    }
   
    public void setOptimizedButtonSelection(boolean flag)
    {
		_optimizedButton.setSelection(flag);
    }

 /*   public void setShowCreateDialogSelection(boolean flag)
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
    }*/
    
}
