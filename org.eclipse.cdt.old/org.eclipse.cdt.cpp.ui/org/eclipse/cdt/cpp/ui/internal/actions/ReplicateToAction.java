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

import org.eclipse.jface.action.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;


import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.dialogs.*;
import java.lang.reflect.InvocationTargetException;


public class ReplicateToAction extends CustomAction
{ 
    public class ReplicateToOperation extends ReplicateOperation
    {
	public ReplicateToOperation(DataElement subject, List projects, ModelInterface api)
	{
	    super(subject, projects, api);
	}

	protected void execute(IProgressMonitor pm) 
	{
	    _pm = pm;
	    _pm.beginTask("Replicating...", _projects.size());

	    for (int i = 0; i < _projects.size(); i++)
		{
		    _pm.worked(1);

		    DataElement targetProject = ((DataElement)_projects.get(i)).dereference();
		    
		    if (targetProject != null && _subject != targetProject)
			{
			    // do transfer files
			    for (int j = 0; j < _subject.getNestedSize(); j++)
				{
				    DataElement source = _subject.get(j);
				    if (!source.isReference())
					{
					    TransferFiles transferAction = new TransferFiles("transfer", source, 
											     targetProject, this);
					    transferAction.start();
					}
				}
			}
		}
	    _pm.done();
	}
    }


  public ReplicateToAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
      }

    public void run()
    {
	ModelInterface api = ModelInterface.getInstance();
	ChooseProjectDialog dlg = new ChooseProjectDialog("Choose a Project To Replicate To", 
							  api.findWorkspaceElement());
	
	dlg.open();
	if (dlg.getReturnCode() == dlg.OK)
	    {
		List projects = dlg.getSelected();
		
		ReplicateToOperation op = new ReplicateToOperation(_subject, projects, api);
		ProgressMonitorDialog progressDlg = new ProgressMonitorDialog(api.getDummyShell());
		try
		    {
			progressDlg.run(false, true, op);
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


