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


  		if (!_subject.isExpanded())
			   {
			    _subject.expandChildren(true);
			   }
	    _subject.doCommandOn("C_DATES", true);	
  			 
	
	    for (int i = 0; i < _projects.size(); i++)
		{
		    DataElement targetProject = ((DataElement)_projects.get(i)).dereference();
		    
		    if (targetProject != null && _subject != targetProject)
			{
		       if (!targetProject.isExpanded())
			   {
			    targetProject.expandChildren(true);
			   }
		     
 			    targetProject.doCommandOn("C_DATES", true);		
  			   
	

			    // do transfer files
			    ArrayList files = _subject.getAssociated("contents");
			    _pm.beginTask("Replicating files from " + _subject.getName() + "...", files.size());		    
			    
			    for (int j = 0; j < files.size() && !pm.isCanceled(); j++)
				{
				    DataElement source = (DataElement)files.get(j);
					TransferFiles transferAction = new TransferFiles("transfer", source, 
											     targetProject, null);
					 transferAction.run(pm);
				    _pm.worked(1);
				}
			}
		}
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


