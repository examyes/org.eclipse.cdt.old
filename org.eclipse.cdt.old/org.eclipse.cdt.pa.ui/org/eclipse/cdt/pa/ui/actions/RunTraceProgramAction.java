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

public class RunTraceProgramAction extends CustomAction
{

  private PAModelInterface _api;
  private String           _outputViewId;  
  
  // Constructor
  public RunTraceProgramAction(DataElement subject, String label, DataElement command, DataStore dataStore)
  {	
        super(subject, label, command, dataStore);
        _api = PAModelInterface.getInstance();
        _outputViewId = "org.eclipse.cdt.cpp.ui.CppOutputViewPart";
  }

  public RunTraceProgramAction(java.util.List subjects, String label, DataElement command, DataStore dataStore)
  {	
        super(subjects, label, command, dataStore);
  }
  
  public void run() {
  
    	DataElement cmdStatus = _api.runTraceProgram(_subject);
    	Display d = _api.getShell().getDisplay();
		ShowViewAction action = new ShowViewAction(_outputViewId, cmdStatus);
	    d.asyncExec(action);		
  }
  
}