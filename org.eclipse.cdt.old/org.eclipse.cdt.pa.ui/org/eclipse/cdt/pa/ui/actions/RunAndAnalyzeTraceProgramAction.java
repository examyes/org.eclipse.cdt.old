package org.eclipse.cdt.pa.ui.actions;

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.cdt.pa.ui.api.*;

public class RunAndAnalyzeTraceProgramAction extends CustomAction
{

  private PAModelInterface _api;
  private String           _outputViewId;

  // The analyze command is done on another thread
  public class AnalyzeTraceProgramThread extends Thread
  {
    
    private DataElement _cmdStatus;
    private DataElement _traceProgram;
    private PAModelInterface _api;
    
    
    public AnalyzeTraceProgramThread(DataElement cmdStatus, DataElement traceProgram)
    {
      _cmdStatus = cmdStatus;
      _traceProgram = traceProgram;
      _api = PAModelInterface.getInstance();
    }
    
    public void run()
    {
	  
	  while (!_cmdStatus.getName().equals("done")) {
	   try {
	    Thread.sleep(40);
	   }
	   catch (InterruptedException e) { break; }
	  }
		  
	  _api.analyzeTraceProgram(_traceProgram);
	      
    }
    
  }
  
  
  // Constructor
  public RunAndAnalyzeTraceProgramAction(DataElement subject, String label, DataElement command, DataStore dataStore)
  {	
     super(subject, label, command, dataStore);
     _api = PAModelInterface.getInstance();
     _outputViewId = "org.eclipse.cdt.cpp.ui.CppOutputViewPart";
  }

  public RunAndAnalyzeTraceProgramAction(java.util.List subjects, String label, DataElement command, DataStore dataStore)
  {	
     super(subjects, label, command, dataStore);
  }
  
  
  public void run()
  {
  
    DataElement cmdStatus = _api.runTraceProgram(_subject);
    
    AnalyzeTraceProgramThread analyzeThread = new AnalyzeTraceProgramThread(cmdStatus, _subject);
    analyzeThread.start();
    
    Display d = _api.getShell().getDisplay();
	ShowViewAction action = new ShowViewAction(_outputViewId, cmdStatus);
	d.asyncExec(action);
	
  }
  
}