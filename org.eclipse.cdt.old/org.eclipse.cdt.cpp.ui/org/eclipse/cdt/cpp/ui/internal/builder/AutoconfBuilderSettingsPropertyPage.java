package org.eclipse.cdt.cpp.ui.internal.builder;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.util.ArrayList;

import org.eclipse.cdt.cpp.ui.internal.CppPlugin;
import org.eclipse.cdt.cpp.ui.internal.api.ModelInterface;
import org.eclipse.cdt.dstore.core.model.DataElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

public class AutoconfBuilderSettingsPropertyPage extends PropertyPage
{	
	private AutoconfBuilderPropertyPageControl _autoconfBuildControl;

	// items in the project property page
	String optionKey = "Executable_Type";
	String compKey="Compiler_Type";
	String extensionKey = "Extra_Dist_Extensions"; 

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
		
		// optimnization options
		
		ArrayList options = plugin.readProperty(project,optionKey);
		if (options.isEmpty())
		{
			options.add("Debug");
			_autoconfBuildControl.setDebugButtonSelection(true);
			_autoconfBuildControl.setOptimizedButtonSelection(false);
			plugin.writeProperty(project,optionKey,options);	
		}
		else
		{
			String type = (String)options.get(0);
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
		
		// extra_dist file extensions
		
		ArrayList extensions = plugin.readProperty(project,extensionKey);
		if(!extensions.isEmpty())
		{
			_autoconfBuildControl.setTableItems(extensions);
		}

	}
	protected void performDefaults()
	{
		performInit();
	}
	
	protected void performApply()
	{
		CppPlugin plugin      = CppPlugin.getDefault();
		ModelInterface api = ModelInterface.getInstance();
		
		ArrayList list = new ArrayList();
		if(_autoconfBuildControl.getOptimizedButtonSelection())
		{
			list.add("Optimized");
			plugin.writeProperty(project,optionKey,list);
			DataElement optimizedCmd = plugin.getDataStore().localDescriptorQuery(api.findProjectElement(project).getDescriptor(), "C_OPTIMIZED_OPTION");			
			DataElement status = plugin.getDataStore().command(optimizedCmd, api.findProjectElement(project));
		}
		if(_autoconfBuildControl.getDebugButtonSelection())
		{
			list.add("Debug");
			plugin.writeProperty(project,optionKey,list);
			DataElement optimizedCmd = plugin.getDataStore().localDescriptorQuery(api.findProjectElement(project).getDescriptor(), "C_DEBUG_OPTION");			
			DataElement status = plugin.getDataStore().command(optimizedCmd, api.findProjectElement(project));

		}
		plugin.writeProperty(project,extensionKey,_autoconfBuildControl.getExtensionList());
	}
	
	public boolean performOk()
	{
		ModelInterface api = ModelInterface.getInstance();
		api.setDistributionExtensions(getProject());
		return true;
	}
	private IProject getProject()
	{
		return (IProject)getElement();
	}
    
}
