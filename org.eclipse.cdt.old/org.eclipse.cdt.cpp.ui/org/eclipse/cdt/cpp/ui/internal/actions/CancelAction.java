package org.eclipse.cdt.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;

import org.eclipse.cdt.dstore.core.model.*;

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

public class CancelAction implements ISelectionChangedListener, IWorkbenchWindowActionDelegate
{
  public void run(IAction action)
  {
      CppPlugin plugin = CppPlugin.getDefault();
      ModelInterface api = plugin.getModelInterface();
      DataStore dataStore = plugin.getCurrentDataStore();	
      if (dataStore != null)
	  {
	      DataElement logRoot = dataStore.getLogRoot();
	      for (int i = 0; i < logRoot.getNestedSize(); i++)
		  {
		      DataElement cmd = logRoot.get(i);
		      DataElement status = cmd.get(cmd.getNestedSize() - 1);
		      if (status != null)
			  {
			      String state = status.getName();
			      if (!state.equals("done"))
				  {
				      api.cancel(cmd);
				  }
			  }
		  }
	  }
  }

  public void activateLauncher(Window window, IStructuredSelection selection)
  {
  }

  public void selectionChanged(SelectionChangedEvent selection)
  {
  }


  public void selectionChanged(IAction action, ISelection selection)
  {
  }

    public void dispose()
    {
    }
    
    public void init(IWorkbenchWindow window)
    {
    }
}
