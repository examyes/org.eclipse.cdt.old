package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;

import com.ibm.dstore.ui.dialogs.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.action.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import org.eclipse.ui.*;

import java.io.*;
import java.util.*;

public class SaveAction implements IActionDelegate, ISelectionChangedListener
{
  private String    _path;
    private IProject  _project;
  private ISelection _selection;


  public void run(IAction action)
  {
    ISelection selection = _selection;
    if (selection.isEmpty() || !(selection instanceof IStructuredSelection))
      return;

    IStructuredSelection ss = (IStructuredSelection)selection;
    ModelInterface api = CppPlugin.getModelInterface();	
    if (_project != null)
      api.saveProject(_project);
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
      	_project = (IProject)structuredSelection.getFirstElement();
         if (_project != null)
         {
            if (CppPlugin.isCppProject(_project))
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
