package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.views.*;

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.views.*;
import org.eclipse.cdt.dstore.ui.actions.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.core.internal.resources.*;

import java.util.*;
import java.lang.reflect.*;

import org.eclipse.jface.action.*;
import org.eclipse.ui.internal.*;

public class BrowseObjectAction extends Action 
{
    private DataElement _subject;
    private String      _viewId;
    private DataElement _relationship;

    public BrowseObjectAction(DataElement relationship, DataElement subject, String viewId)
    {	
        super(relationship.getName());
	_relationship = relationship;
	_subject = subject;
	_viewId = viewId;
    }

    public void run() 
    {
	if (_subject != null)
	    {
		openView();
	    }
    }
    

    public void openView()
    {
      IWorkbench desktop = WorkbenchPlugin.getDefault().getWorkbench();
      IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();

      IWorkbenchPage persp= win.getActivePage();
      ILinkable viewPart = (ILinkable)persp.findView(_viewId);

      if (viewPart == null)
	  {
	      try
		  {
		      persp.showView(_viewId);
		      viewPart = (ILinkable)persp.findView(_viewId);
								
		  }
	      catch (PartInitException e)
		  {
		      System.out.println(e);
		  }
	  }
      if (viewPart != null)
	  {
	      viewPart.setInput(_subject);		
	      if (viewPart instanceof GenericViewPart)
		  {
		      GenericViewPart gvp = (GenericViewPart)viewPart;
		      gvp.fixateOnRelationType(_relationship.getName());

		      
		      gvp.setViewDescription(_relationship.getName() + " of " + _subject.getType());
		  }
	  }

    }


}
