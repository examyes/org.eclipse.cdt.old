package org.eclipse.cdt.pa.ui.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;
import org.eclipse.cdt.cpp.ui.internal.views.*;

import org.eclipse.cdt.pa.ui.PAPlugin;
import org.eclipse.cdt.pa.ui.api.*;

import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*; 
import org.eclipse.ui.*;


public class CallersViewPart extends ObjectsViewPart implements IPATraceListener
{
  private PAPlugin _plugin;
  private PAModelInterface _api;
  private DataElement _currentTraceFile;
  
  // constructor
  public CallersViewPart()
  {
    super();
    _plugin = PAPlugin.getDefault();
    _api = _plugin.getModelInterface();
    _currentTraceFile = null;
  }

  public void createPartControl(Composite parent)
  {
    super.createPartControl(parent);
    _viewer.fixateOnRelationType("caller arc");
    
    PATraceNotifier notifier = _api.getTraceNotifier();
    notifier.addTraceListener(this);
  }
 
  public ObjectWindow createViewer(Composite parent, IActionLoader loader)
  {  
    DataStore dataStore = _plugin.getDataStore();
    return new ObjectWindow(parent, ObjectWindow.TABLE, dataStore, _plugin.getImageRegistry(), loader);
  }

  public IActionLoader getActionLoader()
  {
	IActionLoader loader = PAActionLoader.getInstance();
	return loader;
  }
    
  protected String getF1HelpId()
  {
    return CppHelpContextIds.DEFAULT_OBJECTS_VIEW;
  }

  public void initInput(DataStore dataStore)
  {
  }

  public void setFocus()
  {  
      _viewer.setFocus(); 
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
   DataElement traceFile = event.getObject();
   int type = event.getType();
   switch (type) {
   
    case PATraceEvent.FILE_CREATED:
      
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
