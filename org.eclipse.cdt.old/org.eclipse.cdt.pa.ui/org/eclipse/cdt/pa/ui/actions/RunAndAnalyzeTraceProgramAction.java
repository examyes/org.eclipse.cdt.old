package org.eclipse.cdt.pa.ui.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.cdt.pa.ui.api.*;

public class RunAndAnalyzeTraceProgramAction extends CustomAction
{

  private PAModelInterface _api;
  
  
  // Constructor
  public RunAndAnalyzeTraceProgramAction(DataElement subject, String label, DataElement command, DataStore dataStore)
  {	
     super(subject, label, command, dataStore);
     
     _api = PAModelInterface.getInstance();
  }

  public RunAndAnalyzeTraceProgramAction(java.util.List subjects, String label, DataElement command, DataStore dataStore)
  {	
     super(subjects, label, command, dataStore);
  }
  
  
  public void run()
  {
    if (!_api.isRunningTraceProgram())
      _api.runAndAnalyzeTraceProgram(_subject);	
  }
  
}