package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.dialogs.*;

import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.model.*;

import com.ibm.dstore.hosts.actions.*;

import java.io.*; 
import java.util.*;

import com.ibm.cpp.ui.internal.vcm.*;
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

public class OpenProjectAction extends CustomAction
{ 
    public class OpenOperation implements IRunnableWithProgress
    {
	private IProject _project;
	private ModelInterface _api;
	private DataElement _projectElement;

	public OpenOperation(IProject project, DataElement subject, ModelInterface api)
	{
	    _project = project;
	    _api = api;
	    _projectElement = subject;
	}    

	public void run(IProgressMonitor monitor) throws InvocationTargetException
	{
	    execute(monitor);
	}

	protected void execute(IProgressMonitor pm) 
	{
	    try
		{
		    _project.open(pm);
		}
	    catch (CoreException e)
		{
		    System.out.println(e);
		}

	    if (_project instanceof Repository)
		{
		}
	    else
		{
		    _api.openProject(_project);
		}
	}
    }

    private IProject _project;
    private ModelInterface _api;

  public OpenProjectAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
	_api = ModelInterface.getInstance();
	_project = _api.findProjectResource(subject);
        setEnabled(!_project.isOpen());
      }

    public void run()
    { 
	if (_project != null)
	    {
		OpenOperation op = new OpenOperation(_project, _subject, _api);
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
