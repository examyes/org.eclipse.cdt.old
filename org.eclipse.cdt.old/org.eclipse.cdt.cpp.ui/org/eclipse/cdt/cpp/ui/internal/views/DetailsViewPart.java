package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.*;

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.ILinkable;

import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*; 

import org.eclipse.ui.*;

public class DetailsViewPart extends ObjectsViewPart
{
  public DetailsViewPart()
  {
    super();
  }

  public void createPartControl(Composite parent)
  {
    super.createPartControl(parent);    
  }
 
    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {
	DataStore dataStore = _plugin.getCurrentDataStore();
	return new ObjectWindow(parent, 0, dataStore, _plugin.getImageRegistry(), this, false);
    }

   
  protected String getF1HelpId()
  {
   return CppHelpContextIds.DETAILS_VIEW;
  }

  public void initInput(DataStore dataStore)
  {
  }

  public void setFocus()
  {  
      _viewer.setFocus();
  }

  public void selectionChanged(IWorkbenchPart part, ISelection sel) 
  {
    if (part != this && part instanceof ILinkable)
    {
      ((ILinkable)part).linkTo(this);	
      setLinked(true);
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
		    updateStatusLine(event);
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

}










