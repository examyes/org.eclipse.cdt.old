package com.ibm.dstore.ui.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.ui.views.*;

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.*;
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
        IWorkspace workspace = DataStoreUIPlugin.getDefault().getPluginWorkspace();
	IWorkbenchWindow dw =  DataStoreUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
	IWorkbenchPage persp = null;
	
	IWorkbenchPage[] perspectives = dw.getPages();

	try
	    {
		persp = dw.openPage("com.ibm.dstore.ui.views.ObjectsPerspective", input);
	    }
        catch (WorkbenchException e)
	    {
		System.out.println(e);
		e.printStackTrace();
	    }
	
	final IViewPart viewPart = persp.findView("com.ibm.dstore.ui.views.GenericViewPart");
	if (viewPart != null)
	    {
		persp.bringToTop(viewPart);
	    }
    }

}
