package org.eclipse.cdt.pa.ui.actions;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.pa.ui.*;
import org.eclipse.cdt.pa.ui.api.*;


public class NewTraceFileAction extends Action implements IWorkbenchWindowActionDelegate
{
	
	private PAModelInterface _api;
	
	// constructor
	public NewTraceFileAction()
	{
		super("Add Trace File", PAPlugin.getDefault().getImageDescriptor("trace_file"));
		
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
      
      if (selection != null && selection.isOfType("file")) {
        
        _api.addAutoTraceFile(selection);
        _api.openPerspective();
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