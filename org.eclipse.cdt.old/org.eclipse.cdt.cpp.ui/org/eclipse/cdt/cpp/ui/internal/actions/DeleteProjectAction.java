package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.vcm.*;
import com.ibm.cpp.ui.internal.dialogs.*;

import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.model.*;

import com.ibm.dstore.hosts.actions.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.dialogs.*;

import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import java.lang.reflect.InvocationTargetException;

public class DeleteProjectAction extends CustomAction
{ 
    public class DeleteOperation implements IRunnableWithProgress
    {
	private IProject _project;
	private ModelInterface _api;

	public DeleteOperation(IProject project, ModelInterface api)
	{
	    _project = project;
	    _api = api; 
	}    

	public void run(IProgressMonitor monitor) throws InvocationTargetException
	{
	    execute(monitor);
	}

	protected void execute(IProgressMonitor pm) 
	{
	    // close project
	    if (_project.isOpen())
		{
		    _api.closeProject(_project);
		    
		    try
			{
			    _project.close(pm);
			}
		    catch (CoreException e)
			{
			    System.out.println(e);
			}
		}

	    if (_project instanceof Repository)
		{
		    DataElement closedProject = _api.findProjectElement(_project);
		    if (closedProject != null)
			{
			    closedProject.getDataStore().deleteObject(closedProject.getParent(), closedProject);
			}
		}

	    // delete project
	    try
		{
		    _project.delete(false, true, pm);
		}
	    catch(CoreException e)
		{
		    System.out.println(e);
		}
	}
    }


  public DeleteProjectAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
      }

    public void run()
    { 
	ModelInterface api = ModelInterface.getInstance();
	IProject project = api.findProjectResource(_subject);
	if (project != null)
	    {
		DeleteOperation op = new DeleteOperation(project, api);
		ProgressMonitorDialog progressDlg = new ProgressMonitorDialog(api.getDummyShell());
		try
		    {
			progressDlg.run(true, true, op);
		    }
		catch (InterruptedException e) 
		    {
			System.out.println(e);
		    } 
		catch (InvocationTargetException e) 
		    {
			System.out.println(e);
		    }
	    }
    }
}
