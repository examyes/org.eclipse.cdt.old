package org.eclipse.cdt.cpp.ui.internal;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import java.io.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;


// for now this is really just used for filtering and determining whether a project is a C++ project

public class CppProject implements IProjectNature 
{
    private IProject _project;

    public CppProject() 
    {
    }    

    public void configure() throws CoreException 
    {
        // Add nature-specific information
        // for the project, such as adding a builder
        // to a project's build spec.
	String builderName = "org.eclipse.cdt.cpp.ui.cppbuilder";
	IProjectDescription projectDescription =  _project.getDescription();
	
	ICommand command = projectDescription.newCommand();
	command.setBuilderName(builderName);
	ICommand[] newCommands = new ICommand[1];
	newCommands[0] = command;
	projectDescription.setBuildSpec(newCommands);	
    }

    public void deconfigure() throws CoreException 
    {
        // Remove the nature-specific information here.
    }

    public IProject getProject() 
    {
        return _project;
    }

    public void setProject(IProject value) 
    {
        _project = value;
    }

}
