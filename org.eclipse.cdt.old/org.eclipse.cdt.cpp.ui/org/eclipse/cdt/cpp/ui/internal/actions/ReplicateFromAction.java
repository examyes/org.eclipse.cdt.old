package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
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
	    
	    if (!_subject.isExpanded())
			   {
			    _subject.expandChildren(true);
			   }
	    _subject.doCommandOn("C_DATES", true);		

	    for (int i = 0; i < _projects.size() && !pm.isCanceled(); i++)
		{
		    DataElement sourceProject = ((DataElement)_projects.get(i)).dereference();
		
		    	      		    	       
		    if (!sourceProject.isExpanded())
			   {
			    sourceProject.expandChildren(true);
			   }
		      
 			    sourceProject.doCommandOn("C_DATES", true);		
  
  

		    if (sourceProject != null && sourceProject != _subject)
			{
				ArrayList files = sourceProject.getAssociated("contents");
				
			    _pm.beginTask("Replicating files from " + sourceProject.getName() + "...", 
			    		files.size());

			    // do transfer files
			    for (int j = 0; j < files.size() && !pm.isCanceled(); j++)
				{
				    DataElement source = (DataElement)files.get(j);					    
					TransferFiles transferAction = new TransferFiles("transfer", source, 
											     _subject, null);					    
					transferAction.run(pm);
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

		IProgressMonitor monitor = progressDlg.getProgressMonitor();
		if (monitor.isCanceled())
		{
			System.out.println("cancelled");
		}

	    }
    }
}


