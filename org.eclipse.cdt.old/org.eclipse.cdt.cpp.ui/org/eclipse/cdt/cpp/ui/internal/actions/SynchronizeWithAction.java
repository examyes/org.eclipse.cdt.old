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
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import org.eclipse.jface.dialogs.*;
import java.lang.reflect.InvocationTargetException;

public class SynchronizeWithAction extends CustomAction
{ 
    public class SynchronizeWithOperation extends ReplicateOperation
    {
	public SynchronizeWithOperation(DataElement subject, List projects, ModelInterface api)
	{
	    super(subject, projects, api);
	}

	protected void execute(IProgressMonitor pm) 
	{
	    _pm = pm;
	    DataElement project1 = _subject;
	    
	    _pm.beginTask("Synchronizing...", _projects.size());
	    
	    for (int i = 0; i < _projects.size(); i++)
		{
		    DataElement project2 = ((DataElement)_projects.get(i)).dereference();

		    if (!project1.isExpanded())
			{
			    project1.expandChildren(true);
			}
		    if (!project2.isExpanded())
			{
			    project2.expandChildren(true);
			}
				
		    project1.doCommandOn("C_DATES", true);		
		    project2.doCommandOn("C_DATES", true);		
		    
		    if (project2 != null && project1 != project2)
			{
				
				ArrayList files = project1.getAssociated("contents");
				
			    _pm.beginTask("Synchronizing " + 
					  project1.getName() + " with " + project2.getName() + "...", 
					  files.size());

			    // transfer from project1 to project2
			    for (int j = 0; j < files.size() && !pm.isCanceled(); j++)
				{
				    DataElement source = (DataElement)files.get(j);
					TransferFiles transferAction = new TransferFiles("transfer", source, 
											     project2, null);
					transferAction.run(pm);
				    _pm.worked(1);
				}

				ArrayList files2 = project2.getAssociated("contents");
			    _pm.beginTask("Synchronizing " + 
					  project2.getName() + " with " + project1.getName() + "...", 
					  files2.size());
			    

			    // transfer from project2 to project1
			    for (int k = 0; k < files2.size() && !pm.isCanceled(); k++)
				{
				    DataElement source = (DataElement)files2.get(k);
					TransferFiles transferAction = new TransferFiles("transfer", source, 
											     project1, null);					     
					transferAction.run(pm);
				    _pm.worked(1);
				}				
			}
		}
	}
    }
    
    public SynchronizeWithAction(DataElement subject, String label, DataElement command, DataStore dataStore)
    {	
        super(subject, label, command, dataStore);
    }
    
    public void run()
    {
	ModelInterface api = ModelInterface.getInstance();

	ChooseProjectDialog dlg = new ChooseProjectDialog("Choose a Project To Synchronize With", 
							  api.findWorkspaceElement());
	dlg.open();
	
	if (dlg.getReturnCode() == dlg.OK)
	    {
		List projects = dlg.getSelected();
		
		SynchronizeWithOperation op = new SynchronizeWithOperation(_subject, projects, api);
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


