package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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

public class DeleteResourceAction extends CustomAction
{ 
    public class DeleteOperation implements IRunnableWithProgress
    {
	private DataElement    _resource;
	private ModelInterface _api;

	public DeleteOperation(DataElement resource, ModelInterface api)
	{
	    _resource = resource;
	    _api = api; 
	}    

	public void run(IProgressMonitor monitor) throws InvocationTargetException
	{
	    execute(monitor);
	}

	protected void execute(IProgressMonitor pm) 
	{
	    DataStore dataStore = _resource.getDataStore();

	    DataElement deleteD = dataStore.localDescriptorQuery(_resource.getDescriptor(), "C_DELETE");
	    
	    pm.beginTask("Deleting " + _resource.getSource(), 3);
	    if (deleteD != null) 
		{
		    IResource pResource = _api.getResource(_resource.getSource());

		    pm.worked(1);

		    dataStore.synchronizedCommand(deleteD, _resource);

		    pm.worked(1);
		    
		    if (pResource != null)
			{  
			    try
				{
				    pResource.getParent().refreshLocal(pResource.DEPTH_INFINITE, pm);
				}
			    catch (CoreException e)
				{
				    System.out.println(e);
				}	
			}

		    pm.worked(1);
		}

	    pm.done();
	}
    }


  public DeleteResourceAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
      }

    public void run()
    { 
	ModelInterface api = ModelInterface.getInstance();
	DeleteOperation op = new DeleteOperation(_subject, api);
	ProgressMonitorDialog progressDlg = new ProgressMonitorDialog(api.getDummyShell());
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
