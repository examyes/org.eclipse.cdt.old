package org.eclipse.cdt.pa.ui.actions;

import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.pa.ui.api.*;


public class AnalyzeTraceProgramAction extends CustomAction
{
  private PAModelInterface _api;

  // constructor
  public AnalyzeTraceProgramAction(DataElement subject, String label, DataElement command, DataStore dataStore)
  {	
        super(subject, label, command, dataStore);
        _api = PAModelInterface.getInstance();
  }

  public AnalyzeTraceProgramAction(java.util.List subjects, String label, DataElement command, DataStore dataStore)
  {	
        super(subjects, label, command, dataStore);
  }
  
  public void run()
  {
    
     _api.analyzeTraceProgram(_subject);
  }
  
}