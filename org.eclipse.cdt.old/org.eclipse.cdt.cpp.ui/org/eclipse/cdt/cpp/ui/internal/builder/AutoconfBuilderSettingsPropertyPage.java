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
	private AutoconfBuilderPropertyPageControl _autoconfBuildControl;

	// items in the project property page
	String execKey = "Executable_Type"; 

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
			_autoconfBuildControl = new AutoconfBuilderPropertyPageControl(parent, SWT.NONE);
			performInit();
			_autoconfBuildControl.setLayout(layout);
			return _autoconfBuildControl;
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
		ArrayList execType = plugin.readProperty(project,execKey);
		if (execType.isEmpty())
		{
			execType.add("Debug");
			_autoconfBuildControl.setDebugButtonSelection(true);
			_autoconfBuildControl.setOptimizedButtonSelection(false);
			plugin.writeProperty(project,execKey,execType);	
		}
		else
		{
			String type = (String)execType.get(0);
			if (type.equals("Debug"))
			{
				_autoconfBuildControl.setDebugButtonSelection(true);
				_autoconfBuildControl.setOptimizedButtonSelection(false);
				
			}
			else
			{
				_autoconfBuildControl.setOptimizedButtonSelection(true);
				_autoconfBuildControl.setDebugButtonSelection(false);
			}
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
		ArrayList list = new ArrayList();
		if(_autoconfBuildControl.getOptimizedButtonSelection())
		{
			list.add("Optimized");
			plugin.writeProperty(project,execKey,list);
		}
		if(_autoconfBuildControl.getDebugButtonSelection())
		{
			list.add("Debug");
			plugin.writeProperty(project,execKey,list);
		}
		
		return true;
	}
	private IProject getProject()
	{
		return (IProject)getElement();
	}
    
}
