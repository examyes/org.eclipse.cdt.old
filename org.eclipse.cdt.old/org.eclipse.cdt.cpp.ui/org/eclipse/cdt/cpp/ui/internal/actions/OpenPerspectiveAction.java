package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.views.*;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.views.*;
import com.ibm.dstore.ui.actions.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.core.internal.resources.*;

import java.util.*;
import java.lang.reflect.*;

import org.eclipse.jface.action.*;
import org.eclipse.ui.internal.*;

public class OpenPerspectiveAction extends CustomAction 
{
  public OpenPerspectiveAction(String label)
      {	
        super(label);
      }
    
  public void run() 
      {
	  if (_subject != null)
	      {
		  openPerspective(_subject);
	      }
      }

    private void openPerspective(DataElement input)
    {
        IWorkspace workspace = CppPlugin.getDefault().getPluginWorkspace();
		IWorkbenchWindow dw = CppPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage persp = null;
	
		IWorkbenchPage[] perspectives = dw.getPages();

		try 
	    {
			persp = dw.openPage("com.ibm.cpp.ui.CppObjectPerspective", input);

	    }
        catch (WorkbenchException e)
	    {
	    }

	    
    }

}
