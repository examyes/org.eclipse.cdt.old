package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.dialogs.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;

import org.eclipse.jface.action.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.ui.*;

import java.io.*;
import java.util.*;

public class ParseAction implements ISelectionChangedListener, IWorkbenchWindowActionDelegate
{
  private String    _path;

  private IResource   _resource;
  private DataElement _resourceElement;

  private ISelection _selection;


  public void run(IAction action)
  {
    ISelection selection = _selection;
    if (selection.isEmpty() || !(selection instanceof IStructuredSelection))
      return;

    ModelInterface api = CppPlugin.getModelInterface();	
    if (_resource != null)
	{
	    api.parse(_resource, false, true);
	}
    else if (_resourceElement != null)
	{
	    api.parse(_resourceElement, false, true);
	}
  }


  public void activateLauncher(Window window, IStructuredSelection selection)
  {
    if (window instanceof IWorkbenchWindow)
      {
	((IWorkbenchWindow) window).getActivePage().saveAllEditors(true);
      }
  }

  public void selectionChanged(SelectionChangedEvent selection)
  {
  }


  public void selectionChanged(IAction action, ISelection selection)
  {
    boolean state= !selection.isEmpty();
    _selection = selection;
    _resource = null;
    _resourceElement = null;

    if (selection instanceof IStructuredSelection)
    {
	IStructuredSelection structuredSelection= (IStructuredSelection)selection;
	Object first = structuredSelection.getFirstElement();
	
	if (first instanceof IResource)
	    {		
		_resource= (IResource)first;
		if (_resource != null)
		    {
			IProject project = _resource.getProject();
			
			if (CppPlugin.isCppProject(project))
			    {
				((Action)action).setEnabled(true);
			    }
			else
			    {
				((Action)action).setEnabled(false);
			    }
		    }
	    }
	else if (first instanceof DataElement)
	    {
		_resourceElement = (DataElement)first;
		DataElement descriptor = _resourceElement.getDescriptor();
		
		if (descriptor != null && descriptor.isOfType("file"))
		    {
			((Action)action).setEnabled(true);			
		    }
		else
		    {
			((Action)action).setEnabled(false);			
		    }
	    }
    }
  }
    
    public void dispose()
    {
    }

    public void init(IWorkbenchWindow window)
    {
    }
}
