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

public class AutoconfBuilderSettingsPropertyPage extends PropertyPage
{	
	private AutoconfBuilderPropertyPageControl _autoconfControl;

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
			_autoconfControl = new AutoconfBuilderPropertyPageControl(parent, SWT.NONE);
			performInit();
			_autoconfControl.setLayout(layout);
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


}
