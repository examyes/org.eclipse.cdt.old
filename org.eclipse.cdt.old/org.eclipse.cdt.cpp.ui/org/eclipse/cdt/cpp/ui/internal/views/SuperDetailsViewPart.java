package org.eclipse.cdt.cpp.ui.internal.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;

import org.eclipse.core.resources.*;

import org.eclipse.swt.widgets.*;

import org.eclipse.jface.viewers.*; 

import org.eclipse.ui.*;

import org.eclipse.ui.internal.*;


public class SuperDetailsViewPart extends ObjectsViewPart
{ 
  public SuperDetailsViewPart()
  {
    super();
  }

  public void createPartControl(Composite parent)
  {
    super.createPartControl(parent);    
  }
  

  protected String getF1HelpId()
  {
   return CppHelpContextIds.SUPER_DETAILS_VIEW;
  }


  public void setFocus()
  {  
      _viewer.setFocus();
  }

    public void linkTo(ILinkable viewer)
    {
	// this view doesn't link to others
    }

  protected void internalSelectionChanged(IWorkbenchPart part, ISelection sel) 
  {
    if (part != this && part instanceof ILinkable)
    {
	IWorkbench desktop = WorkbenchPlugin.getDefault().getWorkbench();
	IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();	
	IWorkbenchPage persp= win.getActivePage();

	if (persp.findView(getSite().getId()) == this)
	    {
		((ILinkable)part).linkTo(this);	
		setLinked(true);
	    }
    }

  }

    public void projectChanged(CppProjectEvent event)
    {
	int type = event.getType();
	IProject project = event.getProject();
	switch (type)
	    {
	    case CppProjectEvent.OPEN:
		break;
	    case CppProjectEvent.CLOSE:
	    case CppProjectEvent.DELETE:
		{
		    Display d= _viewer.getViewer().getControl().getDisplay();
		    d.asyncExec(new Runnable()
			{
			    public void run()
			    {
				setInput(null);
			    }
			});
		}
		break;
	       
	    case CppProjectEvent.VIEW_CHANGE:
	      {
		updateViewBackground();		
		updateViewForeground();		
		updateViewFont();
	      }
	      break;
	      
	    case CppProjectEvent.COMMAND:
		{
		    updateSelectionStatus(event);
		}
		break;
	      
	    default:
		break;
	    }
    }


    protected void updateSelectionStatus(CppProjectEvent event)
    {
	if (event.getType() == CppProjectEvent.COMMAND)
	    {
		if (event.getStatus() == CppProjectEvent.DONE)
		    {
			_viewer.enableSelection(true);
			_viewer.resetInput();
		    }
		else if (event.getStatus() == CppProjectEvent.START)
		    {
			_viewer.enableSelection(false);			
		    }
	    }
    }


    public void setInput(DataElement element)
    {    
	_viewer.setInput(element);     

	// create new title
	if (element != null)
	    {
		String objectType = element.getType();
		setViewDescription(objectType + " details");
	    }
	else
	    {
		setViewDescription("No Input");
	    }
    }
}










