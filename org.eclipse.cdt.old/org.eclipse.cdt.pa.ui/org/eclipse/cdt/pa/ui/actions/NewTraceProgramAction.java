package org.eclipse.cdt.pa.ui.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.pa.ui.*;
import org.eclipse.cdt.pa.ui.api.*;
import org.eclipse.cdt.pa.ui.dialogs.*;


public class NewTraceProgramAction extends Action implements IWorkbenchWindowActionDelegate
{

    private PAModelInterface _api;
    
	// constructor
	public NewTraceProgramAction()
	{
	  super("Analyze C/C++ Application", PAPlugin.getDefault().getImageDescriptor("trace_program"));
	  
	  _api = PAModelInterface.getInstance();
	}
	
	public void dispose() 
	{
		// do nothing.
	}
	
	public void init(IWorkbenchWindow window) 
	{
		// do nothing.
	}	

    public void run()
    {
      DataElement selection = _api.getSelection();
      
      if (selection != null && selection.isOfType("executable"))
      {
        AddTraceProgramDialog dlg = new AddTraceProgramDialog("Add Trace Program", selection);
        dlg.open();
        
        if (dlg.getReturnCode() == dlg.OK)
        {
	      DataElement traceProgram = _api.addTraceProgram(selection, dlg.getTraceFormat(), dlg.getArgument());
	    
	      _api.openPerspective();

	      if (traceProgram != null )
	      {
	        if (dlg.getPostActionId() == AddTraceProgramDialog.ACTION_ANALYZE)
	          _api.analyzeTraceProgram(traceProgram);
	        else if (dlg.getPostActionId() == AddTraceProgramDialog.ACTION_RUN_AND_ANALYZE)
	          _api.runAndAnalyzeTraceProgram(traceProgram);
	      }
		}
      }
    }

    
	public void run(IAction action)
	{
	  run();
	}
	
	
	public void selectionChanged(IAction action, ISelection selection) 
	{	  
	}

}