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


public class CalleesViewPart extends PAObjectsViewPart
{
  private PAPlugin _plugin;
  private PAModelInterface _api;
  private DataElement _currentTraceFile;
  
  // constructor
  public CalleesViewPart()
  {
    super();
    _plugin = PAPlugin.getDefault();
    _api = _plugin.getModelInterface();
    _currentTraceFile = null;
  }

  public void createPartControl(Composite parent)
  {
    super.createPartControl(parent); 
    _viewer.fixateOnRelationType("callee arc");
  }
 
  public ObjectWindow createViewer(Composite parent, IActionLoader loader)
  {  
    DataStore dataStore = _plugin.getDataStore();
    return new ObjectWindow(parent, ObjectWindow.TABLE, dataStore, _plugin.getImageRegistry(), loader);
  }

  protected String getF1HelpId()
  {
    return "org.eclipse.cdt.pa.ui.callees_view_context";
  }

  
  public void selectionChanged(IWorkbenchPart part, ISelection sel) 
  {
   
   if (part != this && sel instanceof IStructuredSelection) {
    Object selected = ((IStructuredSelection)sel).getFirstElement();
    
    if (selected instanceof DataElement) {
     DataElement traceObject = (DataElement)selected;
     
     // System.out.println(traceObject);
     
     if (traceObject.isOfType("trace function")) {
     
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
   DataElement traceObject = event.getObject();
   int type = event.getType();
   switch (type) {
        
    case PATraceEvent.FILE_DELETED:
      
      if (traceObject == _currentTraceFile) {
        _currentTraceFile = null;
		initInput(null);
      }
      break;

    case PATraceEvent.PROJECT_CHANGED:
    
      if (traceObject != null) {
        _viewer.setInput(traceObject);
      }
      else {
        initInput(null);
      }
      
      break;
      
    default:
      break;
   }
   
  }
      
}
