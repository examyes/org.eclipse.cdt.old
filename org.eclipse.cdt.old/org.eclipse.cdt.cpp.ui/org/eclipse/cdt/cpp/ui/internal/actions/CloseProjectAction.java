package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.dialogs.*;

import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.cdt.dstore.hosts.actions.*;

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

    private ArrayList _projects;
    private ModelInterface _api;

  public CloseProjectAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);

		_api = ModelInterface.getInstance();
		_projects = new ArrayList();
		
		IProject project = _api.findProjectResource(subject);
        setEnabled(project.isOpen());
        _projects.add(project);
      }
      
 public CloseProjectAction(java.util.List subjects, String label, DataElement command, DataStore dataStore)
      {	
        super(subjects, label, command, dataStore);
		
		_api = ModelInterface.getInstance();
		_projects = new ArrayList();
		
		for (int i = 0; i < subjects.size(); i++)
		{
			DataElement subject = (DataElement)subjects.get(i);
			IProject project = _api.findProjectResource(subject);
			
			if (!project.isOpen())
			{
        		setEnabled(false);
        		return;
			}
			else
			{
				setEnabled(true);
				_projects.add(project);
			}
		}
      }

    public void run()
    { 
	 for (int i = 0; i < _projects.size(); i++)
	    {
	    	IProject project = (IProject)_projects.get(i);

			CloseOperation op = new CloseOperation(project, _api);
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
