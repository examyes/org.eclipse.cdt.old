package org.eclipse.cdt.pa.ui.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.pa.ui.api.*;


public class TraceDropDownAction implements IWorkbenchWindowPulldownDelegate
{	
	
	private PAModelInterface _api;

	/**
	 *	Create a new instance of this class
	 */
	public TraceDropDownAction()
	{
		super();
		
		_api = PAModelInterface.getInstance();
	}
	
	private void createMenuForAction(Menu parent, Action action) 
	{
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}
	

	public Menu getMenu(Control parent)
	{
		Menu menu= new Menu(parent);
		
		createMenuForAction(menu, new NewTraceFileAction());
		createMenuForAction(menu, new NewTraceProgramAction());	

		return menu;
	}


	public void dispose() 
	{
	}

	public void init(IWorkbenchWindow window)
	{
	}
	
	public void run(IAction action) 
	{
	}

	public void selectionChanged(IAction action, ISelection selection)
	{
	  DataElement element = null;
	  
	  if (selection instanceof IStructuredSelection)
	  {
		IStructuredSelection structuredSelection= (IStructuredSelection)selection;
		Object first = structuredSelection.getFirstElement();
		
		if (first instanceof DataElement)
		{
		  element = (DataElement)first;
		  String type = element.getType();
		  DataElement descriptor = element.getDescriptor();
		
		  if (type.equals("file") || 
		      (descriptor != null && descriptor.isOfType("file", true)))
		  {
			 action.setEnabled(true);			
		  }
		  else
		  {
			action.setEnabled(false);			
		  }		
		}
		else
		{
		  action.setEnabled(false);
		}
	    
	  }
	  else
	  {
	    action.setEnabled(false);
	  }
	  
	  _api.setSelection(element);
	}
	
}
