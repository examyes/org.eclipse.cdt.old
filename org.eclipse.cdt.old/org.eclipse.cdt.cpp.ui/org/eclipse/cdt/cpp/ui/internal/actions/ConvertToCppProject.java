package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.dialogs.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;

import org.eclipse.ui.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;


import java.io.*;
import java.util.*;

public class ConvertToCppProject extends CppActionDelegate 
{
    private CppPlugin     _plugin;

    public ConvertToCppProject()
    {
	_plugin = CppPlugin.getDefault();
    }

    public void run(IAction action)     
    {
	// add C++ project indicator
	if (_currentResource != null && _currentResource instanceof IProject)
	    {
		IProject project = (IProject)_currentResource;
		IPath newPath = project.getFullPath();
		QualifiedName indicatorFile = new QualifiedName("C++ Project", newPath.toString());
		try
		    {
			project.setPersistentProperty(indicatorFile, "yes");
		    }
		catch (CoreException ce)
		    {
		    }
		
		// add parse paths
		ArrayList paths = _plugin.readProperty("DefaultParseIncludePath");
		_plugin.writeProperty(project, "Include Path", paths);
		
		// add parse behaviour
		ArrayList autoParse = _plugin.readProperty("AutoParse");
		_plugin.writeProperty(project, "AutoParse", autoParse);      
		
		ArrayList autoPersist = _plugin.readProperty("AutoPerist");
		_plugin.writeProperty(project, "AutoPersist", autoPersist);      
		
		// add parse quality
		ArrayList preferences = _plugin.readProperty("ParseQuality");
		_plugin.writeProperty(project, "ParseQuality", preferences);      
		
		// add build history
		ArrayList builds = _plugin.readProperty("DefaultBuildInvocation");
		_plugin.writeProperty(project, "Build History", builds);
		
		// add clean history
		ArrayList cleans = _plugin.readProperty("DefaultCleanInvocation");
		_plugin.writeProperty(project, "Clean History", cleans);
		
		ArrayList variables = _plugin.readProperty("DefaultEnvironment");
		_plugin.writeProperty(project, "Environment", variables);
		
		ModelInterface api = ModelInterface.getInstance();
		
		// add build spec
		try 
		    { 
			// add build spec
			String builderName = "org.eclipse.cdt.cpp.ui.cppbuilder";
			IProjectDescription projectDescription =  project.getDescription();
			
			ICommand command = projectDescription.newCommand();
			command.setBuilderName(builderName);
			
			ICommand[] commands = projectDescription.getBuildSpec();
			ICommand[] newCommands = new ICommand[commands.length + 1];
			System.arraycopy(commands, 0, newCommands, 0, commands.length);
			newCommands[commands.length] = command;
			projectDescription.setBuildSpec(newCommands);	
			
			// specify nature
			String[] natures = projectDescription.getNatureIds();
			String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = "org.eclipse.cdt.cpp.ui.cppnature";
			projectDescription.setNatureIds(newNatures);
			project.setDescription(projectDescription, null);
		    } 
		catch (CoreException e)  
		    {
			System.out.println(e);
		    }
		
		api.openProject(project);
	    }
    }

    protected void checkEnabledState(IAction a)
    {
    }
}
