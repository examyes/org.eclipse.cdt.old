package org.eclipse.cdt.cpp.ui.internal.api;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.*;


public class StatusLineUpdater implements ICppProjectListener
{
	
	private static StatusLineUpdater  _instance;
	private static IStatusLineManager _mgr;
	private static IProgressMonitor   _pm;
	
	private StatusLineUpdater()
	{	
	}
	
	public static StatusLineUpdater getInstance()
	{
	  if (_instance == null)
	   _instance = new StatusLineUpdater();
	   
	  return _instance;
	}
	
	public Shell getShell() {
	  return ModelInterface.getInstance().getShell();
	}

    public void projectChanged(CppProjectEvent event)
    {
	  int type = event.getType();
	  IProject project = event.getProject();
	  switch (type)
	    {
		
	    case CppProjectEvent.COMMAND:
		{
		    updateStatusLine(event);
		}
		break;
				
	    default:
		break;
	    }
    }
    
    
    protected void updateStatusLine(CppProjectEvent event)
    {
    
    if (_pm == null)
    {
	  IWorkbench desktop = WorkbenchPlugin.getDefault().getWorkbench();
	  IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();	
	  IWorkbenchPage persp= win.getActivePage();
      IViewReference[] viewRefs = persp.getViewReferences();
    
      if (viewRefs.length > 0)
      {
	    _mgr = viewRefs[0].getView(false).getViewSite().getActionBars().getStatusLineManager();
	    _pm = _mgr.getProgressMonitor();
	  } 
	}
	
	try
	{
	if (_pm != null && event.getType() == CppProjectEvent.COMMAND)
	    {
		DataElement commandStatus = event.getObject();
		if (commandStatus != null)
		    {
		DataElement commandObject = commandStatus.getParent();
		
		if ((commandObject != null) && (event.getProject() != null))
		    {
			if (event.getStatus() == CppProjectEvent.DONE)
			    {
				_pm.done();

				String commandStr = commandObject.getValue() + " running on " + 
				    event.getProject().getName();
				commandStr += " is complete";
				_mgr.setMessage(commandStr);

			    }
			else if (event.getStatus() == CppProjectEvent.START)
			    {
				String commandStr = commandObject.getValue() + " running on " + event.getProject().getName();
				_mgr.setMessage(commandStr);
				DataElement cancelDescriptor = commandObject.getDataStore().localDescriptorQuery(commandObject.getDescriptor(), "C_CANCEL");
				if (cancelDescriptor != null)
				{
					_mgr.setCancelEnabled(true);
				}
				_pm.beginTask(commandStr, IProgressMonitor.UNKNOWN);
			    }
			   else if (_pm != null && _pm.isCanceled())
			   {
			   	ModelInterface api = ModelInterface.getInstance();
			   	api.cancel(commandObject);
			   }
		    }
		    }
	    }   
	}
	catch (Exception e)
	{
	  //System.out.println(e);	  	
	}
    }
    
}