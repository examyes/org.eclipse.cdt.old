package com.ibm.dstore.hosts.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.hosts.*;

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

import java.util.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;

import org.eclipse.ui.part.*;
import org.eclipse.ui.*;
import org.eclipse.core.resources.*;

public class CommandViewPart extends ViewPart implements ISelectionListener
{
  private CommandViewer _viewer;
  private HostsPlugin   _plugin = HostsPlugin.getDefault();

  public CommandViewPart() 
  {
    super();
  }

  public void createPartControl(Composite container)
  {
    _viewer = new CommandViewer(container);
    getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
  }

  public void dispose()
      {
        IWorkbench aWorkbench = _plugin.getWorkbench();
        IWorkbenchWindow win= aWorkbench.getActiveWorkbenchWindow();
        win.getSelectionService().removeSelectionListener(this);
        super.dispose();
      }

  public void setFocus()
  {
  }

    public void selectionChanged(IWorkbenchPart part, ISelection sel)
    {
	if (sel != null && sel instanceof IStructuredSelection)
	    {
		IStructuredSelection es= (IStructuredSelection) sel;
		Object input = es.getFirstElement();
		if (_viewer != null && ((input instanceof DataElement) ||(input instanceof IResource)))
		    _viewer.setInput(input);	
	    }
    }


}










