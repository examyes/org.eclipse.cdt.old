package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;
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

public class CloseProjectAction extends CustomAction
{ 
    public class CloseOperation implements IRunnableWithProgress
    {
	private IProject _project;
	private ModelInterface _api;

	public CloseOperation(IProject project, ModelInterface api)
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
    }

    private IProject _project;
    private ModelInterface _api;

  public CloseProjectAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);

	_api = ModelInterface.getInstance();
	_project = _api.findProjectResource(subject);
        setEnabled(_project.isOpen());
      }

    public void run()
    { 
	if (_project != null)
	    {
		CloseOperation op = new CloseOperation(_project, _api);
		ProgressMonitorDialog progressDlg = new ProgressMonitorDialog(_api.getDummyShell());
		try
		    {
			progressDlg.run(true, true, op);
		    }
		catch (InterruptedException e) 
		    {
		    } 
		catch (InvocationTargetException e) 
		    {
		    }
	    }
    }
}
