package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;

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

public class ParseAction implements IActionDelegate, ISelectionChangedListener
{
  private String    _path;
  private IResource _resource;
  private ISelection _selection;


  public void run(IAction action)
  {
    ISelection selection = _selection;
    if (selection.isEmpty() || !(selection instanceof IStructuredSelection))
      return;

    ModelInterface api = CppPlugin.getModelInterface();	
    if (_resource != null)
      api.parse(_resource, false, true);
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

    if (selection instanceof IStructuredSelection)
    {
	    IStructuredSelection structuredSelection= (IStructuredSelection)selection;
       _resource= (IResource)structuredSelection.getFirstElement();
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
  }
}
