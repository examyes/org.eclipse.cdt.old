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
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.resource.*;
import org.eclipse.ui.*;


public class TraceFilesViewPart extends ObjectsViewPart implements IPATraceListener
{
 
  // The nested show all action
  class ShowAllAction extends Action
  {
	public ShowAllAction(String label, ImageDescriptor image)
	{
	    super(label, image );
	}
     
	public void run()
	{
	   toggleShowAll();
	}
  }
  
  // attributes
  private DataElement _currentTraceProject;    
  private CppPlugin _cppPlugin;
  private PAModelInterface _api;
  private ShowAllAction _showAllAction;
  
  // Constructor
  public TraceFilesViewPart()
  {
    super();
    _cppPlugin = CppPlugin.getDefault();
    _api = PAPlugin.getDefault().getModelInterface();
    _currentTraceProject = null;
  }

  public void createPartControl(Composite parent)
  {
    super.createPartControl(parent);    
    PATraceNotifier notifier = _api.getTraceNotifier();
    notifier.addTraceListener(this);
  }
 
  public ObjectWindow createViewer(Composite parent, IActionLoader loader)
  {  
    DataStore dataStore = _cppPlugin.getDataStore();
    return new ObjectWindow(parent, ObjectWindow.TABLE, dataStore, _cppPlugin.getImageRegistry(), loader);
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

  public void toggleShowAll() {
  
    _api.setShowAll(!_api.isShowAll());
    if (_api.isShowAll()) {    
     _viewer.setInput(_api.getTraceFilesRoot());     
    }
    else {
     _viewer.setInput(_currentTraceProject);
    }
    
    
	Display d= _viewer.getViewer().getControl().getDisplay();
	d.asyncExec(new Runnable()
	{
	  public void run()
	  {
	     _showAllAction.setChecked(_api.isShowAll());
		 _viewer.resetView();
	   }
	});
	
  }
  
  
  public void selectionChanged(IWorkbenchPart part, ISelection sel) 
  {
   // System.out.println("selection changed called");
   
   if (!_api.isShowAll() && part instanceof PAProjectsViewPart && sel instanceof IStructuredSelection) {
    Object selected = ((IStructuredSelection)sel).getFirstElement();
    if (selected instanceof DataElement) {
     DataElement traceProjectElement = _api.findOrCreateTraceProjectElement((DataElement)selected);
     
     //System.out.println("trace project in files view: " +  traceProjectElement);   
     
     if (traceProjectElement != null && _currentTraceProject != traceProjectElement) {     
       
       _currentTraceProject = traceProjectElement;
       _viewer.setInput(traceProjectElement);
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
   
    case PATraceEvent.FILE_CREATED:
      
      //System.out.println("trace file created");
      
      if (!_api.isShowAll()) {
       DataElement paProject = traceObject.getParent();
       if (paProject != null) {
        _currentTraceProject = paProject;
        _viewer.setInput(paProject);
       }
      }
      else {
        _viewer.setInput(_api.getTraceFilesRoot());
      }
      
	  Display d= _api.getShell().getDisplay();
	  d.asyncExec(new Runnable()
	  {
		public void run()
		{
		  _viewer.resetView();
		}
	  });
      
      break;
     
    case PATraceEvent.FILE_DELETED:
      
      _viewer.resetView();      
      break;
    
    case PATraceEvent.PROJECT_DELETED:
    
      if (_currentTraceProject == traceObject) {
        _currentTraceProject = null;
        _viewer.setInput(_api.getDummyElement());      
        _viewer.resetView();
      }
      break;
      
    default:
      break;
   }
   
  }
  

  public void fillLocalToolBar() 
  {
  	 super.fillLocalToolBar();
	 IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		
	 ImageDescriptor image = PAPlugin.getDefault().getImageDescriptor("showall");
			
	 _showAllAction = new ShowAllAction("Show All", image);
	 _showAllAction.setChecked(_api.isShowAll());
	 _showAllAction.setToolTipText("show all");
	 toolBarManager.add(_showAllAction);

  }
 
 
}
