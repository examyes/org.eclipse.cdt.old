package com.ibm.cpp.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
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
