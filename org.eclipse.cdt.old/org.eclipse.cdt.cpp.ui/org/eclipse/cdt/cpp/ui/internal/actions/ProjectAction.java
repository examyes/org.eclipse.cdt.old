package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */


import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.actions.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;

public abstract class ProjectAction extends CustomAction
{
 DataStore       _dataStore;
 DataElement     _subject;
 DataElement     _project;
 ModelInterface  _api;
 
 public ProjectAction(DataElement subject, String label, DataElement command, DataStore dataStore)
 {	
  super(subject, label, command, dataStore);
  _dataStore = dataStore;
  _subject   = subject;
  _api       = CppPlugin.getModelInterface();
  _project   = getProjectFor(subject);
 }
 
 private DataElement getProjectFor(DataElement theElement)
 {
  // DataElement parent = theElement;
  //while ( (parent != null) && (!parent.getType().equals("Project")) && (!parent.getType().equals("data")))
  // parent = theElement.getParent();
  //if ( (parent == null) || (!parent.getType().equals("Project")))
   return theElement;
   //return parent;
 }
}

