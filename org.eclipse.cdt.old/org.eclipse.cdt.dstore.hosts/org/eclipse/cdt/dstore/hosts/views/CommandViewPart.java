package org.eclipse.cdt.dstore.hosts.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.hosts.*;

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;

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










