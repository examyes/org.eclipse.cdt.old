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

public class ProjectObjectsViewPart extends ProjectViewPart
{
 public ProjectObjectsViewPart()
 {
  super();
 }

 protected String getF1HelpId()
 {
  return CppHelpContextIds.PROJECT_OBJECTS_VIEW;
 }
    
 public void doClear()
 {
  _viewer.setInput(null);
  setTitle("Project-Objects");
 }

 public void doSpecificInput(DataElement projectParseInformation)
 {
  //Find the Project Objects Object under the projectParseInformation
  DataElement projectObjects = projectParseInformation.getDataStore().find(projectParseInformation, DE.A_NAME, "Project Objects", 1);
  if (projectObjects == null)
   return;
  
  //Finally just set the input and the title
  _viewer.setInput(projectObjects);	    
  setTitle(projectParseInformation.getName() + "Project-Objects");   
 }
}










