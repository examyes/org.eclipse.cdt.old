package org.eclipse.cdt.pa.ui.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.pa.ui.api.*;
import org.eclipse.cdt.pa.ui.dialogs.*;

public class AddTraceProgramActionDelegate extends DataElementActionDelegate
{
 
  private PAModelInterface  _api;
 
  
  public AddTraceProgramActionDelegate()
  {
    super();
    
    _api = PAModelInterface.getInstance();
  }
    
  
  public void run()
  {
     
     AddTraceProgramDialog dlg = new AddTraceProgramDialog("Add Trace Program", _subject);
     dlg.open();
        
     if (dlg.getReturnCode() == dlg.OK)
     {
	    DataElement traceProgram = _api.addTraceProgram(_subject, dlg.getTraceFormat(), dlg.getArgument());
	    
	    _api.openPerspective();

	    if (traceProgram != null )
	    {
	      if (dlg.getPostActionId() == AddTraceProgramDialog.ACTION_ANALYZE)
	        _api.analyzeTraceProgram(traceProgram);
	      else if (dlg.getPostActionId() == AddTraceProgramDialog.ACTION_RUN_AND_ANALYZE)
	        _api.runAndAnalyzeTraceProgram(traceProgram);
	    }
	    	    
     }
  }
  
}