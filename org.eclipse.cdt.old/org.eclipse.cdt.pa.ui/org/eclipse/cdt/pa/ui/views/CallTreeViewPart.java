package org.eclipse.cdt.pa.ui.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;

import org.eclipse.cdt.pa.ui.PAPlugin;
import org.eclipse.cdt.pa.ui.api.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;

/**
 * The call trace view displays the call trace information for a trace file or trace function.
 * If the input is a trace file, it displays the entire call trace in a tree format.
 * If the input is a trace function, it displays the "calls" and "called by" relations of 
 * the trace function.
 */
public class CallTreeViewPart extends PAObjectsViewPart
{
  private PAPlugin _plugin;
  private PAModelInterface _api;
  private DataElement _currentTraceFile;
  
  // constructor
  public CallTreeViewPart()
  {
    super();
    _plugin = PAPlugin.getDefault();
    _api = _plugin.getModelInterface();
    _currentTraceFile = null;
  }

 
  public ObjectWindow createViewer(Composite parent, IActionLoader loader)
  {  
    DataStore dataStore = _plugin.getDataStore();
    return new ObjectWindow(parent, ObjectWindow.TREE, dataStore, _plugin.getImageRegistry(), loader);
  }

    
  protected String getF1HelpId()
  {
    return "org.eclipse.cdt.pa.ui.calltree_view_context";
  }

  
  public void selectionChanged(IWorkbenchPart part, ISelection sel) 
  {
   
   if (part != this && sel instanceof IStructuredSelection) {
    Object selected = ((IStructuredSelection)sel).getFirstElement();
    
    if (selected instanceof DataElement) {
     DataElement traceObject = (DataElement)selected;
          
     if (traceObject.isOfType("trace file") || traceObject.isOfType("trace program")) {
      
      _currentTraceFile = traceObject;
      DataElement callTraceRoot = _api.getCallTreeRoot(traceObject);
      _viewer.setInput(callTraceRoot);
      _viewer.selectRelationship("calls");
     }
     else if (traceObject.isOfType("trace function")) {
     
      _currentTraceFile = _api.getContainingTraceFile(traceObject);
      _viewer.setInput(traceObject);
     }
          
    }    
   }   
  }

  /**
   * From IPATraceListener.
   */
  public void traceChanged(PATraceEvent event) {
  
   // System.out.println("traceChanged called");
   DataElement traceFile = event.getObject();
   int type = event.getType();
   switch (type) {
   
    case PATraceEvent.FILE_CREATED:
      
      _currentTraceFile = traceFile;
      DataElement callTreeRoot = _api.getCallTreeRoot(traceFile);
      _viewer.setInput(callTreeRoot);
      _viewer.selectRelationship("calls");
      break;
     
    case PATraceEvent.FILE_DELETED:
      
      if (traceFile == _currentTraceFile) {
        _currentTraceFile = null;
        _viewer.setInput(_api.getDummyElement());      
        _viewer.resetView();
      }
      
      break;
      
    default:
      break;
   }
   
  }
    
}
