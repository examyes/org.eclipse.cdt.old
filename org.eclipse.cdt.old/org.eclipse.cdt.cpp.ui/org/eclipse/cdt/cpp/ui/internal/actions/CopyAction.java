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

	    for (int i = 0; i < _projects.size() && !pm.isCanceled(); i++)
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
			
		}
		_subject.getDataStore().refresh(_subject.getParent());
//
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
	ChooseProjectDialog dlg = new ChooseProjectDialog("Choose a Directory to Copy to", 
							  api.findWorkspaceElement());
	dlg.useFilter(true);
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


