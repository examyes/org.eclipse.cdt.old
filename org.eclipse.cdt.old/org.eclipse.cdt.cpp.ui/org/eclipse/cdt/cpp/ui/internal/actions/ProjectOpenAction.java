package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import com.ibm.cpp.ui.internal.vcm.*;
import java.util.*;
import org.eclipse.core.resources.*;


public class ProjectOpenAction extends ProjectAction
{ 
 DataElement _openCommand;
 
 public ProjectOpenAction(DataElement subject, String label, DataElement command, DataStore dataStore)
 {	
  super(subject, label, command, dataStore);
  _openCommand = _dataStore.createCommandDescriptor(null, "C_OPEN_REMOTE", "com.ibm.cpp.miners.project.ProjectMiner", "C_OPEN_REMOTE");
 }

 private IProject getRemoteProject(String name)
 {
  RemoteProjectAdapter rmtAdapter = RemoteProjectAdapter.getInstance();
  if (rmtAdapter != null)
  {
   IProject[] rprojects = rmtAdapter.getProjects();
   if (rprojects != null)
   {
    for (int j = 0; j < rprojects.length; j++)
    {	
     System.out.println(rprojects[j]);
     if (rprojects[j].getName().equals(name))
      return rprojects[j];
    }
   }
  }
  return null;
 }
 
 public void run()
 {
  IProject theProject = getRemoteProject(_subject.getName());
  _api.openProject(theProject);
  //try{((Repository)theProject).open(null);
  //}
  //catch(Throwable e){
  // }
  
  //((Repository)theProject).refresh();
  
  DataElement theProjectElement = ((Repository)theProject).getElement();
  //System.out.println("tpe = " + theProjectElement);
  
  ArrayList args = new ArrayList();
  args.add(_project);
  DataElement status = _dataStore.command(_openCommand, args, theProjectElement, false);
  _api.monitorStatus(status);
  _api.showView("com.ibm.cpp.ui.CppProjectsViewPart", null);
  _api.showView("com.ibm.cpp.ui.ParsedSourceViewPart", null);
  _api.showView("com.ibm.cpp.ui.DetailsViewPart", null);
 }
}

