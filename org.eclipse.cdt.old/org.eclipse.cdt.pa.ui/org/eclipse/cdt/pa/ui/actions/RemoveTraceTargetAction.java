package org.eclipse.cdt.pa.ui.actions;

import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.pa.ui.api.*;


public class RemoveTraceTargetAction extends CustomAction
{

  private PAModelInterface _api;
  
  // Constructors
  public RemoveTraceTargetAction(DataElement subject, String label, DataElement command, DataStore dataStore)
  {	
        super(subject, label, command, dataStore);
        
        _api = PAModelInterface.getInstance();
		
  }

  public RemoveTraceTargetAction(java.util.List subjects, String label, DataElement command, DataStore dataStore)
  {	
        super(subjects, label, command, dataStore);
        setEnabled(false);
  }
   
  public void run()
  {
    _api.removeTraceTarget(_subject);
  }


}