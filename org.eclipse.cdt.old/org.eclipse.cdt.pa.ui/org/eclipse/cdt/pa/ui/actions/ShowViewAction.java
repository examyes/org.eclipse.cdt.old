package org.eclipse.cdt.pa.ui.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.ui.*;
import org.eclipse.swt.widgets.*;

public class ShowViewAction implements Runnable
{

    private String      _id;
    private DataElement _input;

    public ShowViewAction(String id, DataElement input)
    {
      _id = id;
      _input = input;
    }

    public void run()
    {
      IWorkbench desktop = PlatformUI.getWorkbench();
      IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();

      IWorkbenchPage persp= win.getActivePage();
      IViewPart viewPart = persp.findView(_id);

      if (viewPart != null && viewPart instanceof ILinkable)
	  {	
	    persp.bringToTop(viewPart);

	    ILinkable linkablePart = (ILinkable)viewPart;
	    {
		if (_input != null)
		    linkablePart.setInput(_input);	
	    }
	  } 
    }
    
}