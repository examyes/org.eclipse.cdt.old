package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;

import com.ibm.dstore.ui.dialogs.*;

import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.internal.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.action.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;

import java.io.*;
import java.util.*;

public class ClearAction implements IActionDelegate
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
	{
	    api.clearProject(_project);
	}
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
