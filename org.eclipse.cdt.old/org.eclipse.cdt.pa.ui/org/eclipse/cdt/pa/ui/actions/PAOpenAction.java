package org.eclipse.cdt.pa.ui.actions;

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.cpp.ui.internal.actions.OpenEditorAction;
import org.eclipse.cdt.pa.ui.api.*;

import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.*;


public class PAOpenAction extends OpenEditorAction
{

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
  
  
	private PAModelInterface _paApi;
	private String           _outputViewId;
	
	
	public PAOpenAction(DataElement element)
	{
	  super(element);
	  
	  _paApi = PAModelInterface.getInstance();
	  _outputViewId = "org.eclipse.cdt.cpp.ui.CppOutputViewPart";
	}
	    
    
    public void performGoto(boolean flag)
    {
      
      if (flag && _element.isOfType("trace program"))
      {
       runAndAnalyzeTraceProgram();
      }
      else
       super.performGoto(flag);
    }
    
    
    private void runAndAnalyzeTraceProgram()
    {
            
      DataElement cmdStatus = _paApi.runTraceProgram(_element);
    
      AnalyzeTraceProgramThread analyzeThread = new AnalyzeTraceProgramThread(cmdStatus, _element);
      analyzeThread.start();
    
      Display d = _paApi.getShell().getDisplay();
	  ShowViewAction action = new ShowViewAction(_outputViewId, cmdStatus);
	  d.asyncExec(action);
      
    }
    
}