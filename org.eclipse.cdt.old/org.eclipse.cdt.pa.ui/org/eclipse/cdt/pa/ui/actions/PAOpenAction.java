package org.eclipse.cdt.pa.ui.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.cpp.ui.internal.actions.OpenEditorAction;
import org.eclipse.cdt.pa.ui.*;
import org.eclipse.cdt.pa.ui.api.*;
import org.eclipse.cdt.pa.ui.views.*;

import org.eclipse.ui.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;


public class PAOpenAction extends OpenEditorAction implements ISelectionListener
{  
    
	private PAModelInterface _api;
    private int 		 	 _viewId;
	private boolean			 _flag;
	
	private static int		 OTHER_VIEW   = 0;
	private static int		 CALLERS_VIEW = 1;
	private static int		 CALLEES_VIEW = 2;
	
	public PAOpenAction(DataElement element)
	{
	  super(element);
	  
      IWorkbenchWindow win = PAPlugin.getActiveWorkbenchWindow();
      ISelectionService selectionService = win.getSelectionService();
	  selectionService.addSelectionListener(this);
	  
	  _api  = PAModelInterface.getInstance();
	  _flag = false;
	  _viewId = OTHER_VIEW;
	}
	    

    public void selectionChanged(IWorkbenchPart part, ISelection sel) 
    {
      if (part instanceof CallersViewPart)
       _viewId = CALLERS_VIEW;
      else if (part instanceof CalleesViewPart)
       _viewId = CALLEES_VIEW;
      else
       _viewId = OTHER_VIEW;
    }

    /**
     * Overwrite the performGoto interface from OpenEditorAction
     */
    public void performGoto(boolean flag)
    {
      _flag = flag;
      
      // If the selected element is a trace program, double click will
      // trigger a "run and analyze" action.
      if (_element.isOfType("trace program"))
      {
        if (flag && !_api.isRunningTraceProgram())
          runAndAnalyzeTraceProgram();
      }
      
      // For a trace function, we will find its source location and go to
      // that location.
      else if (_element.isOfType("trace function"))
      {
        if (_element.getSource() == null || _element.getSource().length() == 0)
        {
          _api.findTraceSourceLocation(_element, true);          
        }
        else
        {
          gotoSourceLocation();
        }
      }
      
      // For a call arc, we will find the trace function with a matched name
      // and go to the source location of the trace function.
      else if (_element.isOfType("call arc"))
      {
        String functionName = null;
        if (_viewId == CALLERS_VIEW)
          functionName = _element.getName();
        else if (_viewId == CALLEES_VIEW)
          functionName = _element.getValue();
          
        if (functionName != null)
        {
          DataElement traceFile = _element.getParent().getParent();
          DataElement traceFunctionsRoot = _api.getTraceFuctionsRoot(traceFile);
          
          DataElement matchedFunction = null;
          for (int i = traceFunctionsRoot.getNestedSize() - 1; i >= 0; i--)
          {
            DataElement traceFunction = traceFunctionsRoot.get(i);
            
            if (!traceFunction.isDeleted() && functionName.equals(traceFunction.getName()))
            {
              matchedFunction = traceFunction;
              break;
            }
          }
          
          if (matchedFunction !=  null)
          {
            setSelected(matchedFunction);
            
        	if (matchedFunction.getSource() == null || matchedFunction.getSource().length() == 0)
        	{
          	  _api.findTraceSourceLocation(matchedFunction, true);          
        	}
        	else
        	{
          	  gotoSourceLocation();
        	}
            
          }
        }
        
      }
      else
        gotoSourceLocation();
    }
    
    
    public void gotoSourceLocation()
    {
      super.performGoto(_flag);
    }
        
    
    private void runAndAnalyzeTraceProgram()
    {
      _api.runAndAnalyzeTraceProgram(_element);      
    }
    
    
}