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


public class CopyAction extends CustomAction
{ 
    public class CopyOperation extends ReplicateOperation
    {
	public CopyOperation(DataElement subject, List projects, ModelInterface api)
	{
	    super(subject, projects, api);
	}
 
	protected void execute(IProgressMonitor pm) 
	{
	    _pm = pm;
	    DataElement project1 = _subject;

	    _pm.beginTask("Copying " + _subject.getName() + "...", _projects.size());

	    _subject.doCommandOn("C_DATES", true);		

	    for (int i = 0; i < _projects.size(); i++)
		{
		    DataElement targetProject = ((DataElement)_projects.get(i)).dereference();
		   
		    
		    if (targetProject != null && targetProject != _subject)
			{
			    TransferFiles transferAction = new TransferFiles("transfer", _subject, 
									     targetProject, null);	
				transferAction.checkTimestamps(false);							     				    
			    transferAction.run(pm);
			    _pm.worked(1);
			}
			
			targetProject.getDataStore().refresh(targetProject);
		}
	}
    }


  public CopyAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
      }


  public CopyAction(java.util.List subjects, String label, DataElement command, DataStore dataStore)
  {
  	super(subjects, label, command, dataStore);
  }	

    public void run()
    {
	ModelInterface api = ModelInterface.getInstance();	
	ChooseProjectDialog dlg = new ChooseProjectDialog("Choose a directory to copy to", 
							  api.findWorkspaceElement());
	dlg.useFilter(false);
	dlg.open();
	
	if (dlg.getReturnCode() == dlg.OK)
	    {
		List projects = dlg.getSelected();
		
		for (int i = 0; i < _subjects.size(); i++)
		{		
			DataElement subject = (DataElement)_subjects.get(i);
			CopyOperation op = new CopyOperation(subject, projects, api);
			ProgressMonitorDialog progressDlg = new ProgressMonitorDialog(api.getDummyShell());
			try
		    {
				progressDlg.run(false, true, op);
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
}


