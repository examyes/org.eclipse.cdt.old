package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.vcm.*;
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

import org.eclipse.swt.widgets.*;

import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import java.lang.reflect.InvocationTargetException;

public class DeleteProjectAction extends CustomAction
{ 
    public class DeleteOperation implements IRunnableWithProgress
    {
	private IProject _project;
	private ModelInterface _api;
	private boolean _deleteContents;

	public DeleteOperation(IProject project, ModelInterface api, boolean deleteContents)
	{
	    _project = project;
	    _api = api; 
	    _deleteContents = deleteContents;
	}    

	public void run(IProgressMonitor monitor) throws InvocationTargetException
	{
	    execute(monitor);
	}

	protected void execute(IProgressMonitor pm) 
	{
	    // close project
	    if (_project.isOpen())
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

	    if (_project instanceof Repository)
		{
		    DataElement closedProject = _api.findProjectElement(_project);
		    if (closedProject != null)
			{
			    DataElement workspace = closedProject.getParent();
			    closedProject.getDataStore().deleteObject(workspace, closedProject);
			    workspace.getDataStore().refresh(workspace);
			    workspace.removeNestedData(closedProject);
			}
		}
	    
	    // delete project
	    try
		{		    
		    _project.delete(_deleteContents, true, pm);
		}
	    catch(CoreException e)
		{
		    System.out.println(e);
		}
	}
    }


  public DeleteProjectAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
      }

    public void run()
    { 
	ModelInterface api = ModelInterface.getInstance();
	IProject project = api.findProjectResource(_subject);
	if (project != null)
	    {
		Shell shell = api.getDummyShell();
		String msg = "About to delete project \'" + project.getName() + "\'.\n";
		msg += "Delete all its contents under " + project.getLocation().toOSString() + " as well?";
		String title = "Delete Project Contents";

		MessageDialog dialog = new MessageDialog(
							 shell,
							 title, 
							 null,	// accept the default window icon
							 msg, 
							 MessageDialog.QUESTION, 
							 new String[] {IDialogConstants.YES_LABEL, 
								       IDialogConstants.NO_LABEL, 
								       IDialogConstants.CANCEL_LABEL},
							 0); 	// yes is the default
		int code = dialog.open();
		boolean deleteContent = false;
		switch (code)
		    {
		    case 0: // Yes
			deleteContent = true;
			break;
		    case 1: // No
			deleteContent = false;
			break;
		    default: // CANCEL
			return;
		    }

		DeleteOperation op = new DeleteOperation(project, api, deleteContent);
		ProgressMonitorDialog progressDlg = new ProgressMonitorDialog(shell);
		try
		    {
			progressDlg.run(true, true, op);
		    }
		catch (InterruptedException e) 
		    {
			System.out.println(e);
		    } 
		catch (InvocationTargetException e) 
		    {
			System.out.println(e);
		    }
	    }
    }
}
