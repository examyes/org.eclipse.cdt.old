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


public class FunctionStatisticsViewPart extends ObjectsViewPart implements IPATraceListener {


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
  
    public void createPartControl(Composite parent)
    {
      super.createPartControl(parent);    
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
          
     if (part instanceof TraceFilesViewPart && sel instanceof IStructuredSelection) {
            
      Object selected = ((IStructuredSelection)sel).getFirstElement();
      if (selected instanceof DataElement) {
       DataElement traceFileElement = (DataElement)selected;
       if (traceFileElement != _currentTraceFile) {
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
    
     // System.out.println("traceChanged called");
     DataElement traceFile = event.getObject();
     int type = event.getType();
     switch (type) {
     
      case PATraceEvent.FILE_CREATED:
        
        _currentTraceFile = traceFile;
        _currentProject = _api.findReferencedProject(traceFile);
        _viewer.setInput(_api.getTraceFuctionsRoot(traceFile));
        break;
       
      case PATraceEvent.FILE_DELETED:
        
        if (traceFile == _currentTraceFile) {
          _viewer.setInput(_api.getDummyElement());
          _viewer.resetView();
        }
        break;
        
      default:
        break;
     }
     
    }
    
}