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

import java.util.List;


import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.dialogs.*;
import java.lang.reflect.InvocationTargetException;


public class ReplicateFromAction extends CustomAction
{ 
    public class ReplicateFromOperation extends ReplicateOperation
    {
	public ReplicateFromOperation(DataElement subject, List projects, ModelInterface api)
	{
	    super(subject, projects, api);
	}
 
	protected void execute(IProgressMonitor pm) 
	{
	    _pm = pm;
	    DataElement project1 = _subject;

	    _pm.beginTask("Replicating...", _projects.size());

	    _subject.doCommandOn("C_DATES", true);		

	    for (int i = 0; i < _projects.size(); i++)
		{
		    DataElement sourceProject = ((DataElement)_projects.get(i)).dereference();
		    sourceProject.doCommandOn("C_DATES", true);		

		    if (sourceProject != null && sourceProject != _subject)
			{
			    _pm.beginTask("Transfering files from " + sourceProject.getName() + "...", sourceProject.getNestedSize());

			    // do transfer files
			    for (int j = 0; j < sourceProject.getNestedSize(); j++)
				{
				    DataElement source = sourceProject.get(j);
				    if (!source.isReference() && (source.isOfType("file") || source.isOfType("directory")))
					{					    
					    TransferFiles transferAction = new TransferFiles("transfer", source, 
											     _subject, null);					    
					    transferAction.run(pm);
					}
				    _pm.worked(1);
				}
			}
		}
	}
    }


  public ReplicateFromAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
      }

    public void run()
    {
	ModelInterface api = ModelInterface.getInstance();	
	ChooseProjectDialog dlg = new ChooseProjectDialog("Choose a Project To Replicate From", 
							  api.findWorkspaceElement());

	dlg.open();
	
	if (dlg.getReturnCode() == dlg.OK)
	    {
		List projects = dlg.getSelected();
		
		ReplicateFromOperation op = new ReplicateFromOperation(_subject, projects, api);
		ProgressMonitorDialog progressDlg = new ProgressMonitorDialog(api.getDummyShell());
		try
		    {
			progressDlg.run(true, true, op);
		    }
		catch (InterruptedException e) 
		    {
			e.printStackTrace();
		    } 
		catch (InvocationTargetException e) 
		    {
			e.printStackTrace();
		    }

	    }
    }
}


