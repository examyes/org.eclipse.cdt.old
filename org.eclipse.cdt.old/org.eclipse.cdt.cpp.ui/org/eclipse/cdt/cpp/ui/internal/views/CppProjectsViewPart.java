package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.core.model.*;

import org.eclipse.jface.action.*;
import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.ui.*;

public class CppProjectsViewPart extends ObjectsViewPart implements ISelectionListener, ICppProjectListener
{ 
 public CppProjectsViewPart()
 {
   super();
 }

 public void createPartControl(Composite parent)
 {
  super.createPartControl(parent);
 }

 protected String getF1HelpId()
 {
  return "com.ibm.cpp.ui.cpp_projects_view_context";
 }
  
 public void initInput(DataStore dataStore)
 {
  setTitle("C++ Projects");
  dataStore = _plugin.getDataStore();
  DataElement projectMinerData = dataStore.findMinerInformation("com.ibm.cpp.miners.project.ProjectMiner");
  if (projectMinerData != null)
  {
   DataElement workspace = dataStore.find(projectMinerData, DE.A_TYPE, "Workspace",1);
  if (workspace != null)
    {
    _viewer.setInput(workspace);
    return;
   }
  }
  _viewer.setInput(null);
 }

    public void selectionChanged(IWorkbenchPart part, ISelection sel) 
    {
    }


    public void projectChanged(CppProjectEvent event)
    {
	int type = event.getType();
	IProject project = event.getProject();
	switch (type)
	    {
	    case CppProjectEvent.OPEN:
		{
		}
		break;
	    case CppProjectEvent.CLOSE:
	    case CppProjectEvent.DELETE:
		{
		    _viewer.resetView();
		}
		break;
		
	    default:
		super.projectChanged(event);
		break;
	    }
    }

}










