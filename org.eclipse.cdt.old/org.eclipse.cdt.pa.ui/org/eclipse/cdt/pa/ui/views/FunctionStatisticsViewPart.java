package org.eclipse.cdt.pa.ui.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;

import org.eclipse.cdt.pa.ui.PAPlugin;
import org.eclipse.cdt.pa.ui.api.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.jface.viewers.*; 
import org.eclipse.ui.*;


public class FunctionStatisticsViewPart extends PAObjectsViewPart {


    private PAPlugin _plugin;
    private ModelInterface _cppApi;
    private PAModelInterface _api;
    private DataElement _currentTraceFile;
    private DataElement _currentProject;
    
    // Constructor
    public FunctionStatisticsViewPart()
    {
      super();
      _plugin = PAPlugin.getDefault();
      _cppApi = ModelInterface.getInstance();
      _api = _plugin.getModelInterface();
      _currentTraceFile = null;
      _currentProject = null;
    }
  
 
    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {  
      DataStore dataStore = _plugin.getDataStore();
      return new ObjectWindow(parent, ObjectWindow.TABLE, dataStore, _plugin.getImageRegistry(), loader);
    }
  
  
    protected String getF1HelpId()
    {
      return "org.eclipse.cdt.pa.ui.function_statistics_view_context";
    }
  
    
    public void selectionChanged(IWorkbenchPart part, ISelection sel) 
    {
          
     if (part instanceof TraceFilesViewPart && sel instanceof IStructuredSelection) {
            
      Object selected = ((IStructuredSelection)sel).getFirstElement();
      if (selected instanceof DataElement) {
       DataElement traceFileElement = (DataElement)selected;
       if (traceFileElement != _currentTraceFile) 
       {
        _currentTraceFile = traceFileElement;
        _currentProject = _api.findReferencedProject(traceFileElement);
        _viewer.setInput(_api.getTraceFuctionsRoot(traceFileElement));
       }
      }
     }
     else if (part instanceof PAProjectsViewPart && sel instanceof IStructuredSelection) {
           
      Object selected = ((IStructuredSelection)sel).getFirstElement();
      
      if (selected instanceof DataElement) {
       
       DataElement fileElement = (DataElement)selected;
       DataElement projectElement = _cppApi.getProjectFor(fileElement);
       
       if (projectElement != null && projectElement != _currentProject) {
        DataElement traceProjectElement = _api.findTraceProjectElement(fileElement);
        _currentProject = projectElement;
                
        if (traceProjectElement != null && traceProjectElement.getNestedSize() > 0) {
         DataElement firstTraceFile = traceProjectElement.get(0);
         _currentTraceFile = firstTraceFile;
         _viewer.setInput(_api.getTraceFuctionsRoot(firstTraceFile));
        }
        else {
         // System.out.println("set trace to null");
         _currentTraceFile = null;
         _viewer.setInput(_api.getDummyElement());
        }
       }
      }
      
     }
          
    }
  
    /**
     * From IPATraceListener.
     */
    public void traceChanged(PATraceEvent event) {
    
     DataElement traceObject = event.getObject();
     int type = event.getType();
     switch (type) {
     
      case PATraceEvent.FILE_CREATED:
        
        // System.out.println("trace file: " + traceFile);
        _currentTraceFile = traceObject;
        _currentProject = _api.findReferencedProject(traceObject);
        _viewer.setInput(_api.getTraceFuctionsRoot(traceObject));
	    break;
       
      case PATraceEvent.FILE_DELETED:
        
        if (traceObject == _currentTraceFile) {
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