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

public class AutoconfSettingsPropertyPage extends PropertyPage implements SelectionListener
{	
	private AutoconfPropertyPageControl _autoconfControl;
	String configureDialogKey = "Show_Configure_Dialog";
	String globalSettingKey = "Is_Global_Setting_Enabled";
	IProject project;

	public AutoconfSettingsPropertyPage()
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
			_autoconfControl = new AutoconfPropertyPageControl(parent, SWT.NONE);
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
		ArrayList globalSettingsKeyProp = plugin.readProperty(project,globalSettingKey); 
		if(globalSettingsKeyProp.isEmpty())
		{
			// by default use global settings
			ArrayList list = new ArrayList();
			list.add("Yes");
			plugin.writeProperty(project,globalSettingKey,list);
			_autoconfControl.setGlobalSettingsSelection(true);
			_autoconfControl.enableLocalActions(false);
			
		}
		// if global setting is not empty
		// check its value - if yes then use the global settings
		else
		{
			ArrayList showDialogConfigure;
			if(globalSettingsKeyProp.get(0).equals("Yes"))
			{
				// get the configure settings from the global setup and apply it
				showDialogConfigure = plugin.readProperty(configureDialogKey);
				_autoconfControl.setGlobalSettingsSelection(true);
				_autoconfControl.enableLocalActions(false);
				// apply Global Settings
				// 1 - configure dialog
				applySettings(showDialogConfigure);
			}
			else // if no then use local settings
			{
				showDialogConfigure = plugin.readProperty(project,configureDialogKey);
				_autoconfControl.setGlobalSettingsSelection(false); 
				_autoconfControl.enableLocalActions(true);
				// apply local Settings
				// 1 - configure dialog
				applySettings(showDialogConfigure);
				
			}
		}
		
	}
	protected void performDefaults()
	{

		CppPlugin plugin      = CppPlugin.getDefault();
		ArrayList globalsettings = plugin.readProperty(project,globalSettingKey);
		if(!globalsettings.isEmpty())
		{
			if(globalsettings.get(0).equals("Yes"))
			{
				_autoconfControl.setGlobalSettingsSelection(true);
				_autoconfControl.enableLocalActions(false);
			}
			else
			{
				ArrayList showDialogConfigure = plugin.readProperty(configureDialogKey);
				if (showDialogConfigure.isEmpty())
				{
					showDialogConfigure.add("Yes");
					_autoconfControl.setShowConfigureDialogSelection(true);
					plugin.writeProperty(configureDialogKey,showDialogConfigure);
				}
				else
				{
					String preference = (String)showDialogConfigure.get(0);
					if (preference.equals("Yes"))
					{
						_autoconfControl.setShowConfigureDialogSelection(true);
					}
					else
					{
						_autoconfControl.setShowConfigureDialogSelection(false);
					}
				}

			}
		}

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
			ArrayList showConfigureDialog = new ArrayList();
			if (_autoconfControl.getShowConfigureDialogSelection())
			{
				showConfigureDialog.add("Yes");		
			}
			else
			{
				showConfigureDialog.add("No");		
			}	
			plugin.writeProperty(project,configureDialogKey, showConfigureDialog);
		
		}	
		return true;
	}
	
	private IProject getProject()
	{
		return (IProject)getElement();
	}
    public void applySettings(ArrayList list)
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
	public void widgetDefaultSelected(SelectionEvent e)
    {
		widgetSelected(e);
    }

    public void widgetSelected(SelectionEvent e)
    {
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
		}
		else
		{
			_autoconfControl.enableLocalActions(true);
		}
    }
}
