package org.eclipse.cdt.pa.ui.actions;

import org.eclipse.core.resources.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.pa.ui.api.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.swt.widgets.*;


public class AddTraceFileAction extends CustomAction
{  
  
  private ModelInterface _cppApi;
  private PAModelInterface _api;
  
  // Constructors
  public AddTraceFileAction(DataElement subject, String label, DataElement command, DataStore dataStore)
  {	
        super(subject, label, command, dataStore);
        
        _cppApi = ModelInterface.getInstance();
        _api = PAModelInterface.getInstance();
		
  }

  public AddTraceFileAction(java.util.List subjects, String label, DataElement command, DataStore dataStore)
  {	
        super(subjects, label, command, dataStore);
        setEnabled(false);
  }
   
  public void run()
  {
    _api.addTraceFile(_subject, "auto");    
  }
  
}
