package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.core.model.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.core.resources.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.ui.*;

public class SystemObjectsViewPart extends ProjectViewPart
{
 public SystemObjectsViewPart()
 {
  super();
 }
 
 protected String getF1HelpId()
 {
  return CppHelpContextIds.SYSTEM_OBJECTS_VIEW;
 }

   
 public void doClear()
 {
  _viewer.setInput(null);
  setTitle("System-Objects");
 }

 public void doSpecificInput(DataElement projectParseInformation)
 {
  //Find the System Objects Object under the projectParseInformation
  DataElement systemObjects = projectParseInformation.getDataStore().find(projectParseInformation, DE.A_NAME, "System Objects", 1);
  if (systemObjects == null)
   return;
  
  //Finally just set the input and the title
  _viewer.setInput(systemObjects);	    
  _viewer.selectRelationship("contents");
  setTitle(projectParseInformation.getName() + "System-Objects");   
 }
}










