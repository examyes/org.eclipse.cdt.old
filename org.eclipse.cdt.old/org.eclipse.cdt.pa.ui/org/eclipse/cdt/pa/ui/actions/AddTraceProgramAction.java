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


public class AddTraceProgramAction extends CustomAction
{
 
  private PAModelInterface _api;
  
  // Constructors
  public AddTraceProgramAction(DataElement subject, String label, DataElement command, DataStore dataStore)
  {	
        super(subject, label, command, dataStore);
        
        _api = PAModelInterface.getInstance();
		
  }

  public AddTraceProgramAction(java.util.List subjects, String label, DataElement command, DataStore dataStore)
  {	
        super(subjects, label, command, dataStore);
        setEnabled(false);
  }
   
  public void run()
  {
     	AddTraceProgramDialog dlg = new AddTraceProgramDialog("Add Trace Program", _subject);
        dlg.open();
        
        if (dlg.getReturnCode() == dlg.OK)
        {
	      _api.addTraceProgram(_subject, dlg.getTraceFormat(), dlg.getArgument());
	      _api.openPerspective();
        }
  }
  
}
