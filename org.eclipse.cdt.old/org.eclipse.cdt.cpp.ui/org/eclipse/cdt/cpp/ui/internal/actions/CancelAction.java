package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;

import com.ibm.dstore.core.model.*;

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
