package com.ibm.cpp.ui.internal.vcm;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.vcm.*;
import com.ibm.cpp.ui.internal.views.*;
import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.actions.*;

import com.ibm.dstore.ui.widgets.ExtendedTreeViewer;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.ui.connections.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.ui.views.contentoutline.*;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

public class CreateProjectAction extends CustomAction
{ 
  public CreateProjectAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
      }

  public void run()
      {
        DataElement selected = _subject;
       
	PlatformVCMProvider provider = PlatformVCMProvider.getInstance();

	ConnectionManager mgr = ConnectionManager.getInstance();
	Connection connection = mgr.findConnectionFor(selected.getDataStore());
	if (connection != null)
	    {
		Repository res = (Repository)provider.createRepository(connection, selected);
		ModelInterface api = ModelInterface.getInstance();
		api.openProject(res);
		
		// now view stuff
		Workspace workspace = (Workspace)WorkbenchPlugin.getPluginWorkspace();
		IWorkbench wb = WorkbenchPlugin.getDefault().getWorkbench();	
		IWorkbenchWindow dw = wb.getActiveWorkbenchWindow();
		
		// Open the perspective.
		try 
		    {
			IWorkbenchPage persp = null;
			
			IWorkbenchPage[] perspectives = dw.getPages();
			for (int i = 0; i < perspectives.length; i++)
			    {
				IWorkbenchPage aPersp = perspectives[i];
				String layoutId = aPersp.getLabel();
				if (layoutId.equals("Workspace : C/C++ Development Perspective"))
				    {
					persp = aPersp;	
					dw.setActivePage(persp);		  
				    }	      
			    }
			
			if (persp == null)
			    {
				persp = dw.openPage("com.ibm.cpp.ui.internal.views.CppPerspective", workspace.getRoot());
			    }
			
		    } 
		catch (WorkbenchException e) 
		    {
			System.out.print(e);
		    }
	    }
	else
	    {
		String msg = "You can not create a project from a resource within this scope";
		MessageDialog explainD = new MessageDialog(null,
							   "Invalid Resource", 
							   null, 
							   msg, 
							   MessageDialog.INFORMATION,
							   new String[]  { "OK" },
							   0);
		
		
		explainD.openInformation(new Shell(), "Invalid Resource", msg);          
	    }
      }
}


