package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
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

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.dialogs.*;
import java.lang.reflect.InvocationTargetException;


public abstract class ReplicateOperation implements IRunnableWithProgress, ITransferListener
{
    protected ModelInterface _api;
    protected DataElement _subject;
    protected List _projects;
    protected IProgressMonitor _pm;
    
    public ReplicateOperation(DataElement subject, List projects, ModelInterface api)
    {
	_api = api;
	_projects = projects;
	_subject = subject;
    }    
    
    public void run(IProgressMonitor monitor) throws InvocationTargetException
    {
	execute(monitor);
    }
    
    public Shell getShell()
    {
	return _api.getShell();
    }
    
    public void update(String updateString)
    {
	_pm.subTask(updateString);
	
	if (updateString.equals("Ready"))
	    {
		IResource subP = _api.findResource(_subject); 
		if (subP instanceof Repository)
		    {
		    }
		else
		    {
			try
			    {
				subP.refreshLocal(subP.DEPTH_INFINITE, _pm);
			    }
			catch (CoreException e)
			    {
				System.out.println(e);
			    }	
		    }

		for (int i = 0; i < _projects.size(); i++)
		    {
			DataElement project = (DataElement)_projects.get(i); 
			IResource tarP = _api.findResource(project); 
			if (tarP instanceof Repository)
			    {
			    }
			else
			    {
				try
				    {
					tarP.refreshLocal(tarP.DEPTH_INFINITE, _pm);
				    }
				catch (CoreException e)
				    {
					System.out.println(e);
				    }	
			    }
		    }
	    }    
    }
    
    protected abstract void execute(IProgressMonitor pm);
}
