package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.hosts.views.*;

import com.ibm.cpp.ui.internal.*;

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

public class CppCommandViewPart extends CommandViewPart
{
  private CppCommandViewer _viewer;

  public CppCommandViewPart() 
  {
    super();
  }

    public void createPartControl(Composite container)
    {
	_viewer = new CppCommandViewer(container);
	getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
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










