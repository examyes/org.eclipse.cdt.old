package com.ibm.cpp.ui.internal.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.actions.*;
import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.api.*;

public abstract class ProjectAction extends CustomAction
{
 ModelInterface  _api;
 
 public ProjectAction(DataElement subject, String label, DataElement command, DataStore dataStore)
 {	
  super(subject, label, command, dataStore);
  _api       = CppPlugin.getModelInterface();
  }
 
 public ProjectAction(java.util.List subjects, String label, DataElement command, DataStore dataStore)
 {	
  super(subjects, label, command, dataStore);
  _api       = CppPlugin.getModelInterface();
  }
 
}

