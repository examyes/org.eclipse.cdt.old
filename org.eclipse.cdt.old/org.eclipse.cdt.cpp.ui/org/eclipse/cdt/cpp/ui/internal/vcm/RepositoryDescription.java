package com.ibm.cpp.ui.internal.vcm;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.resources.*;

public class RepositoryDescription implements IProjectDescription 
{
    private ICommand[] _commands;

    public RepositoryDescription()
	{
	    _commands = new ICommand[1];
	    _commands[0] = new org.eclipse.core.internal.events.BuildCommand();
	}

    public ICommand[] getBuildSpec()
	{
	    return _commands;
	}

    public String getComment()
	{
	    return null;
	}

    public IPath getLocation()
	{
	    return null;
	}

    public String getName()
	{
	    return null;
	}

    public String[] getNatureIds()
	{
	    return null;
	}

    public IProject[] getReferencedProjects()
	{
	    return null;
	}

    public boolean hasNature(String natureId)
	{
	    return true;
	}

    public ICommand newCommand()
	{
	    return null;
	}

    public void setBuildSpec(ICommand[] buildSpec)
	{
	}

    public void setComment(String comment)
	{
	}

    public void setLocation(IPath location)
	{
	}

    public void setName(String projectName)
    {
    }
 
    public void setNatureIds(String[] natures)
	{
	}
    
    public void setReferencedProjects(IProject[] projects)
	{
	}
}
