package org.eclipse.cdt.cpp.ui.internal.builder;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.preferences.AutoconfControl;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;

import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;

import org.eclipse.core.resources.*;
import java.io.*;
import java.util.*;

import org.eclipse.ui.dialogs.*;

public class AutoconfBuilderSettingsPropertyPage extends PropertyPage implements SelectionListener
{	
	private AutoconfDialogPropertyPageControl _autoconfControl;
	String globalSettingKey = "Is_Global_Setting_Enabled";
	// items in the project property page
	String configureDialogKey = "Show_Configure_Dialog"; // done
	String updateAllDialogKey = "Show_Update_All_Dialog";
	String updateMakefileAmKey = "Show_Update_MakefileAm_Dialog";
	String updateConfigureInKey = "Show_Update_ConfigureIn_Dialog";
	String createDialogKey = "Show_Create_Dialog";
	String runDialogKey = "Show_Run_Dialg";

	IProject project;

	public AutoconfBuilderSettingsPropertyPage()
	{
    	super();
	}


	protected Control createContents(Composite parent)
	{
		project= getProject();
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;

		if (CppPlugin.isCppProject(project))
		{
			_autoconfControl = new AutoconfDialogPropertyPageControl(parent, SWT.NONE);
			performInit();
			_autoconfControl.setLayout(layout);
			_autoconfControl.globalSettingsButton.addSelectionListener(this);
			return _autoconfControl;
		}
		else
		{
			Composite cnr = new Composite(parent, SWT.NONE);
			Label label = new Label(cnr, SWT.NULL);
			label.setText("Not a C/C++ Project");
			cnr.setLayout(layout);          
			cnr.setLayoutData(new GridData(GridData.FILL_BOTH));          
			return cnr;
		}
 
	}

	protected void performInit()
	{
		CppPlugin plugin      = CppPlugin.getDefault();
		// global seting is checked
		ArrayList propertyList;
		ArrayList globalSettingsKeyProp = plugin.readProperty(project,globalSettingKey); 
		if(globalSettingsKeyProp.isEmpty()||globalSettingsKeyProp.get(0).equals("Yes"))
		{
			// by default use global settings

			ArrayList list = new ArrayList();
			list.add("Yes");
			plugin.writeProperty(project,globalSettingKey,list);
			_autoconfControl.setGlobalSettingsSelection(true);
			_autoconfControl.enableLocalActions(false);
			
			// get the configure settings from the global setup and apply it
			propertyList = plugin.readProperty(configureDialogKey);
			// apply Global Settings
			// 1 - configure dialog
			applyConfigureDialogSettings(propertyList);
			
			// get the create configure settings from the global setup and apply it
			propertyList = plugin.readProperty(createDialogKey);
			// apply Global Settings
			// 2 - create configure dialog
			applyCreateConfigureDialogSettings(propertyList);				
			
			// get the run configure settings from the global setup and apply it
			propertyList = plugin.readProperty(runDialogKey);
			// apply Global Settings
			// 3 - run configure dialog
			applyRunConfigureDialogSettings(propertyList);			
			
			// get the update all settings from the global setup and apply it
			propertyList = plugin.readProperty(updateAllDialogKey);
			// apply Global Settings
			// 4 - update all dialog
			applyUpdateAllDialogSettings(propertyList);			
				
			// get the update configure.in settings from the global setup and apply it
			propertyList = plugin.readProperty(updateConfigureInKey);
			// apply Global Settings
			// 5 - update configure.in dialog
			applyUpdateConfigureInDialogSettings(propertyList);			
			
			// get update Makefile.am settings from the global setup and apply it
			propertyList = plugin.readProperty(updateMakefileAmKey);
			// apply Global Settings
			// 6 - update Makefile.am dialog
			applyUpdateMakefileAmDialogSettings(propertyList);			
			
		}
		else // if no then use local settings
		{
			_autoconfControl.setGlobalSettingsSelection(false); 
			_autoconfControl.enableLocalActions(true);
				propertyList = plugin.readProperty(project,configureDialogKey);
			// apply local Settings
			// 1 - configure dialog
			applyConfigureDialogSettings(propertyList);
			
			// get the create configure settings from the global setup and apply it
			propertyList = plugin.readProperty(project,createDialogKey);
			// apply Global Settings
			// 2 - create configure dialog
			applyCreateConfigureDialogSettings(propertyList);				
			
			// get the run configure settings from the global setup and apply it
			propertyList = plugin.readProperty(project,runDialogKey);
			// apply Global Settings
			// 3 - run configure dialog
			applyRunConfigureDialogSettings(propertyList);			
			
			// get the update all settings from the global setup and apply it
			propertyList = plugin.readProperty(project,updateAllDialogKey);
			// apply Global Settings
			// 4 - update all dialog
			applyUpdateAllDialogSettings(propertyList);			
			
			// get the update configure.in settings from the global setup and apply it
			propertyList = plugin.readProperty(project,updateConfigureInKey);
			// apply Global Settings
			// 5 - update configure.in dialog
			applyUpdateConfigureInDialogSettings(propertyList);			
			
			// get update Makefile.am settings from the global setup and apply it
			propertyList = plugin.readProperty(project,updateMakefileAmKey);
			// apply Global Settings
			// 6 - update Makefile.am dialog
			applyUpdateMakefileAmDialogSettings(propertyList);			
		}	
	}
	protected void performDefaults()
	{
		performInit();
	}
	
	protected void performApply()
	{
		// if show all selected then check all buttons
		
		// if don't show all selected then uncheck all
	}
	
	public boolean performOk()
	{
		CppPlugin plugin      = CppPlugin.getDefault();
		if(_autoconfControl.getGlobalSettingsSelection())// if global setting is selected then use it
		{
			// by default use global settings
			ArrayList list = new ArrayList();
			list.add("Yes");
			plugin.writeProperty(project,globalSettingKey,list);
		}
		else
		{
			// setglobal to no
			ArrayList list = new ArrayList();
			list.add("No");
			plugin.writeProperty(project,globalSettingKey,list);
			
			// then set the local property accordingly
			// show dialog when configure
			plugin.writeProperty(project,configureDialogKey,getProjectProperty(_autoconfControl.getShowConfigureDialogSelection()));
			// setting for show create configure dialog property
			plugin.writeProperty(project,createDialogKey,getProjectProperty(_autoconfControl.getShowCreateDialogSelection()));
			// setting for show run configure dialog property
			plugin.writeProperty(project,runDialogKey,getProjectProperty(_autoconfControl.getShowRunDialogSelection()));
			// setting for show update All dialog property
			plugin.writeProperty(project,updateAllDialogKey,getProjectProperty(_autoconfControl.getUpdateAllButtonSelection()));
			// setting for show update configure.in dialog property
			plugin.writeProperty(project,updateConfigureInKey,getProjectProperty(_autoconfControl.getUpdateConfigureInButtonSelection()));
			// setting for show update Makefile.am dialog property
			plugin.writeProperty(project,updateMakefileAmKey,getProjectProperty(_autoconfControl.getUpdateMakefileAmButtonSelection()));		
		}	
		return true;
	}
	
	private ArrayList getProjectProperty(boolean selection)
   	{
		ArrayList list = new ArrayList();
		if (selection)
			list.add("Yes");		
		else
			list.add("No");		
   		return list;
   	}
	
	
	
	private IProject getProject()
	{
		return (IProject)getElement();
	}
    
    public void applyConfigureDialogSettings(ArrayList list)
    {
    	CppPlugin plugin      = CppPlugin.getDefault();
    	// begin  - checking configure diaolg set up
		if (list.isEmpty())
		{
			list.add("Yes");
			_autoconfControl.setShowConfigureDialogSelection(true);
			plugin.writeProperty(project,configureDialogKey,list);
		}
		else
		{
			String preference = (String)list.get(0);
			if (preference.equals("Yes"))
			{
				_autoconfControl.setShowConfigureDialogSelection(true);
			}
			else
			{
				_autoconfControl.setShowConfigureDialogSelection(false);
			}
		}
		// end configure dialog setup
    }
    public void applyCreateConfigureDialogSettings(ArrayList list)
    {
    	CppPlugin plugin      = CppPlugin.getDefault();
    	// begin  - checking configure diaolg set up
		if (list.isEmpty())
		{
			list.add("Yes");
			_autoconfControl.setShowCreateDialogSelection(true);
			plugin.writeProperty(project,createDialogKey,list);
		}
		else
		{
			String preference = (String)list.get(0);
			if (preference.equals("Yes"))
			{
				_autoconfControl.setShowCreateDialogSelection(true);
			}
			else
			{
				_autoconfControl.setShowCreateDialogSelection(false);
			}
		}
		// end configure dialog setup
    }
    
    
    public void applyRunConfigureDialogSettings(ArrayList list)
    {
    	CppPlugin plugin      = CppPlugin.getDefault();
    	// begin  - checking configure diaolg set up
		if (list.isEmpty())
		{
			list.add("Yes");
			_autoconfControl.setShowRunDialogSelection(true);
			plugin.writeProperty(project,runDialogKey,list);
		}
		else
		{
			String preference = (String)list.get(0);
			if (preference.equals("Yes"))
			{
				_autoconfControl.setShowRunDialogSelection(true);
			}
			else
			{
				_autoconfControl.setShowRunDialogSelection(false);
			}
		}
		// end configure dialog setup
    }  
    
    public void applyUpdateAllDialogSettings(ArrayList list)
    {
    	CppPlugin plugin      = CppPlugin.getDefault();
    	// begin  - checking configure diaolg set up
		if (list.isEmpty())
		{
			list.add("Yes");
			_autoconfControl.setUpdateAllButtonSelection(true);
			plugin.writeProperty(project,updateAllDialogKey,list);
		}
		else
		{
			String preference = (String)list.get(0);
			if (preference.equals("Yes"))
			{
				_autoconfControl.setUpdateAllButtonSelection(true);
			}
			else
			{
				_autoconfControl.setUpdateAllButtonSelection(false);
			}
		}
		// end configure dialog setup
    }   
    
    public void applyUpdateConfigureInDialogSettings(ArrayList list)
    {
    	CppPlugin plugin      = CppPlugin.getDefault();
    	// begin  - checking configure diaolg set up
		if (list.isEmpty())
		{
			list.add("Yes");
			_autoconfControl.setUpdateConfigureInButtonSelection(true);
			plugin.writeProperty(project,updateConfigureInKey,list);
		}
		else
		{
			String preference = (String)list.get(0);
			if (preference.equals("Yes"))
			{
				_autoconfControl.setUpdateConfigureInButtonSelection(true);
			}
			else
			{
				_autoconfControl.setUpdateConfigureInButtonSelection(false);
			}
		}
		// end configure dialog setup
    }   
    
     public void applyUpdateMakefileAmDialogSettings(ArrayList list)
    {
    	CppPlugin plugin      = CppPlugin.getDefault();
    	// begin  - checking configure diaolg set up
		if (list.isEmpty())
		{
			list.add("Yes");
			_autoconfControl.setUpdateMakefileAmButtonSelection(true);
			plugin.writeProperty(project,updateMakefileAmKey,list);
		}
		else
		{
			String preference = (String)list.get(0);
			if (preference.equals("Yes"))
			{
				_autoconfControl.setUpdateMakefileAmButtonSelection(true);
			}
			else
			{
				_autoconfControl.setUpdateMakefileAmButtonSelection(false);
			}
		}
		// end configure dialog setup
    }      
    
	public void widgetDefaultSelected(SelectionEvent e)
    {
		widgetSelected(e);
    }

    public void widgetSelected(SelectionEvent e)
    {
		ArrayList list;
		// disable the dialog
		Button button = (Button)e.widget;
		CppPlugin plugin      = CppPlugin.getDefault();
		if(button.getSelection())
		{
			_autoconfControl.enableLocalActions(false);
			// update the buttons to be as the pefernces setup
			// 1- configure dialog button
			ArrayList configureDialogList = plugin.readProperty(configureDialogKey);
			if(!configureDialogList.isEmpty())
			{
				if(configureDialogList.get(0).equals("Yes"))
					_autoconfControl.setShowConfigureDialogSelection(true);
				else
					_autoconfControl.setShowConfigureDialogSelection(false);
			}
				
			// 2- create configure dialog button
			list = plugin.readProperty(createDialogKey);
			if(!list.isEmpty())
			{
				if(list.get(0).equals("Yes"))
					_autoconfControl.setShowCreateDialogSelection(true);
				else
					_autoconfControl.setShowCreateDialogSelection(false);
			}
			// 3- run configure dialog button
			list = plugin.readProperty(runDialogKey);
			if(!list.isEmpty())
			{
				if(list.get(0).equals("Yes"))
					_autoconfControl.setShowRunDialogSelection(true);
				else
					_autoconfControl.setShowRunDialogSelection(false);
			}
			// 4- update all dialog button
			list = plugin.readProperty(updateAllDialogKey);
			if(!list.isEmpty())
			{
				if(list.get(0).equals("Yes"))
					_autoconfControl.setUpdateAllButtonSelection(true);
				else
					_autoconfControl.setUpdateAllButtonSelection(false);
			}
			// 5- update configure.in dialog button
			list = plugin.readProperty(updateConfigureInKey);
			if(!list.isEmpty())
			{
				if(list.get(0).equals("Yes"))
					_autoconfControl.setUpdateConfigureInButtonSelection(true);
				else
					_autoconfControl.setUpdateConfigureInButtonSelection(false);
			}
			// 6- update makefile.am dialog button
			list = plugin.readProperty(updateMakefileAmKey);
			if(!list.isEmpty())
			{
				if(list.get(0).equals("Yes"))
					_autoconfControl.setUpdateMakefileAmButtonSelection(true);
				else
					_autoconfControl.setUpdateMakefileAmButtonSelection(false);
			}
		}
		else
		{
			_autoconfControl.enableLocalActions(true);
		}
    }
}
