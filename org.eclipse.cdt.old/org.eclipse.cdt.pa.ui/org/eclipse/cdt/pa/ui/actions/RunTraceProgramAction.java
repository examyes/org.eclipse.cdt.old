package org.eclipse.cdt.pa.ui.actions;

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.cdt.pa.ui.api.*;

public class RunTraceProgramAction extends CustomAction
{

  private PAModelInterface _api;
  private String           _outputViewId;

  public class ShowViewAction implements Runnable
  {
    private String      _id;
    private DataElement _input;

    public ShowViewAction(String id, DataElement input)
    {
      _id = id;
      _input = input;
    }

    public void run()
    {
      IWorkbench desktop = WorkbenchPlugin.getDefault().getWorkbench();
      IWorkbenchWindow win = desktop.getActiveWorkbenchWindow();

      IWorkbenchPage persp= win.getActivePage();
      IViewPart viewPart = persp.findView(_id);

      if (viewPart != null && viewPart instanceof ILinkable)
	  {	
	    persp.bringToTop(viewPart);

	    ILinkable linkablePart = (ILinkable)viewPart;
	    {
		if (_input != null)
		    linkablePart.setInput(_input);	
	    }
	  } 
    }
  }
  
  
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