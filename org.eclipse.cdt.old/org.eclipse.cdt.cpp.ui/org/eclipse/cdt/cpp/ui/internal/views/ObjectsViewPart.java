package org.eclipse.cdt.cpp.ui.internal.views;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.hosts.views.OutputViewer;

import org.eclipse.cdt.cpp.ui.internal.*;
import org.eclipse.cdt.cpp.ui.internal.actions.*; 
import org.eclipse.cdt.cpp.ui.internal.vcm.*;
import org.eclipse.cdt.cpp.ui.internal.api.*;

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.ObjectWindow;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.actions.CustomAction;

import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.dstore.ui.resource.*;

import org.eclipse.cdt.dstore.ui.views.*;

//import org.eclipse.help.*;
import org.eclipse.ui.help.ViewContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;


import org.eclipse.core.resources.*;
import org.eclipse.ui.views.navigator.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.jface.action.*; 
import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import java.util.*;

import java.lang.reflect.*;
 
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.core.runtime.*; 
import org.eclipse.jface.resource.*;

public class ObjectsViewPart extends GenericViewPart 
    implements ICppProjectListener
{
	
	class LockViewAction extends Action
    {
	public LockViewAction(String label, ImageDescriptor image)
	{
	    super(label, image );
	}
     
	public void run()
	{
	    _viewer.toggleLock();
	}
    }


    private   DataStore      _currentDataStore;    
    protected ModelInterface _api;
    protected CppPlugin      _plugin;
    protected   LockViewAction   _lockAction;
    private     boolean          _isLocked;
    
    public ObjectsViewPart()
    {
	super();
	_plugin = CppPlugin.getDefault();
	_api = _plugin.getModelInterface();
	_isLinked = true;
	_isLocked = false;
    }
    
    protected String getF1HelpId()
    {
     return "org.eclipse.cdt.cpp.ui.objects_view_context";
    }
 
    public void updateViewForeground()
    {
	ArrayList colours = _plugin.readProperty("ViewForeground");
	if (colours.size() == 3)
	    {
		int r = new Integer((String)colours.get(0)).intValue();
		int g = new Integer((String)colours.get(1)).intValue();
		int b = new Integer((String)colours.get(2)).intValue();
	 	
 		_viewer.getViewer().setForeground(r, g, b);	      
	    }    
    }
    
    public void updateViewBackground()
    {
	ArrayList colours = _plugin.readProperty("ViewBackground");
	if (colours.size() == 3)
	    {
		int r = new Integer((String)colours.get(0)).intValue();
		int g = new Integer((String)colours.get(1)).intValue();
		int b = new Integer((String)colours.get(2)).intValue();
		
		_viewer.getViewer().setBackground(r, g, b);	      
	    }    
    }
    
    public void updateViewFont()
    {
	ArrayList fontArray = _plugin.readProperty("ViewFont");
	if (fontArray.size() > 0)
	    {
		String fontStr = (String)fontArray.get(0);
		fontStr = fontStr.replace(',', '|');
		FontData fontData = new FontData(fontStr);
		_viewer.getViewer().setFont(fontData);
	    }
    }

    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {
	DataStore dataStore = _plugin.getCurrentDataStore();
	return new ObjectWindow(parent, ObjectWindow.TREE, dataStore, _plugin.getImageRegistry(), loader);
    }
 
    public IActionLoader getActionLoader()
    {
	IActionLoader loader = CppActionLoader.getInstance();
	return loader;
    }

    
  public void createPartControl(Composite parent)
    {	
	super.createPartControl(parent);
	_api.getDummyShell();	

	updateViewBackground();
	updateViewForeground();
	updateViewFont();
		
	CppProjectNotifier notifier = _api.getProjectNotifier();
	notifier.addProjectListener(this);
	
	initInput(null);
        WorkbenchHelp.setHelp(_viewer.getViewer().getControl(), new ViewContextComputer(this, getF1HelpId()));
    }
    
    public void initInput(DataStore dataStore)
    {
	_viewer.setInput((getSite().getPage().getInput()));      
    } 
   
   public void initDataElementInput(DataElement de)
   {
   }
 
    
    private void handleSelection(IStructuredSelection es)
    {
	if (es != null)
	    {	
		Object object = es.getFirstElement();
		
		if (object != null)
		    {
			boolean changed = false;
			boolean dataElement = false;

			DataStore dataStore = _plugin.getCurrentDataStore();
			if (object instanceof IResource)
			    {		
				if (object instanceof ResourceElement)
				    {		    
					ResourceElement theElement = (ResourceElement)object;
					dataStore = ((ResourceElement)object).getDataStore();		
					changed = _plugin.setCurrentProject(theElement);		
				    }
				else if (object instanceof Repository)
				    {
					dataStore = ((Repository)object).getDataStore();		
					changed = _plugin.setCurrentProject((Repository)object);	
				    }	    
				else 
				    {
					changed = _plugin.setCurrentProject((IResource)object);
				    }  
			    }
             else if (object instanceof DataElement)
			 {
              DataElement theObject = (DataElement)object;
			  DataElement theParent = _api.getProjectFor(theObject);

			  if (theParent != null)
			      {
				  IProject project = _api.findProjectResource(theParent);
				  if (project != null)
				      {
					  dataStore = theParent.getDataStore();
					  changed = _plugin.setCurrentProject(project);					  
					  dataElement = true;
				      }
			      }
			  
                         }
			
			if (changed)
			    {	
				if (dataStore != null)
				    {
					_plugin.setCurrentDataStore(dataStore);					  
					dataStore.getDomainNotifier().enable(true);
				    }
			    }
			initInput(dataStore);		    
			
		    }	
	    }
    }

    public void selectionChanged(IWorkbenchPart part, ISelection sel) 
    {
   	if ((part instanceof ResourceNavigator) || 
            (part instanceof CppProjectsViewPart)
	    )
	    {
		if  (part.getSite().getPage() == getSite().getPage())
		    {
			if (sel instanceof IStructuredSelection)
			    {
				IStructuredSelection es= (IStructuredSelection) sel;
				handleSelection(es);
			    }  
		    }
	    }
    }
    
    public void dispose()
    {
        IWorkbench aWorkbench = _plugin.getWorkbench();
        IWorkbenchWindow win= aWorkbench.getActiveWorkbenchWindow();
        win.getSelectionService().removeSelectionListener(this);
	
	CppProjectNotifier notifier = _api.getProjectNotifier();
	notifier.removeProjectListener(this);

	if (_viewer != null)
	    _viewer.dispose();
        super.dispose();
    }
        
  public void setFocus()
    {  
	DataStore current = _plugin.getCurrentDataStore();
	if (current != _currentDataStore)
	    {	
		_currentDataStore = current;	
		initInput(_currentDataStore);    	
	    }
	
	super.setFocus();
    }  
    
    public void projectChanged(CppProjectEvent event)
    {
	int type = event.getType();
	IProject project = event.getProject();
	switch (type)
	    {
	    case CppProjectEvent.OPEN:
		{
		    initInput(_plugin.getCurrentDataStore());
		}
		break;
	    case CppProjectEvent.CLOSE:
	    case CppProjectEvent.DELETE:
		{
		    initInput(_plugin.getCurrentDataStore());
		    setFocus();
		}
		break;
		
	    case CppProjectEvent.COMMAND:
		{
		    updateStatusLine(event);
		    updateSelectionStatus(event);
		}
		break;
		
	    case CppProjectEvent.VIEW_CHANGE:
		{
		    updateViewBackground();		
		    updateViewForeground();		
		    updateViewFont();
		}
		break;
		
		
	    default:
		break;
	    }
    }
        
    protected void updateSelectionStatus(CppProjectEvent event)
    {
	if (event.getType() == CppProjectEvent.COMMAND)
	    {
		if (event.getStatus() == CppProjectEvent.DONE)
		    {
			_viewer.enableSelection(true);
		    }
		else if (event.getStatus() == CppProjectEvent.START)
		    {
			_viewer.enableSelection(false);			
		    }
	    } 
    }
    
    protected void updateStatusLine(CppProjectEvent event)
    {
	IStatusLineManager mgr = getViewSite().getActionBars().getStatusLineManager();
	IProgressMonitor pm = mgr.getProgressMonitor(); 
	try
	{
	if (event.getType() == CppProjectEvent.COMMAND)
	    {
		DataElement commandStatus = event.getObject();
		if (commandStatus != null)
		    {
		DataElement commandObject = commandStatus.getParent();
		
		if ((commandObject != null) && (event.getProject() != null))
		    {
			if (event.getStatus() == CppProjectEvent.DONE)
			    {
				pm.done();

				String commandStr = commandObject.getValue() + " running on " + 
				    event.getProject().getName();
				commandStr += " is complete";
				mgr.setMessage(commandStr);

			    }
			else if (event.getStatus() == CppProjectEvent.START)
			    {
				String commandStr = commandObject.getValue() + " running on " + event.getProject().getName();
				mgr.setMessage(commandStr);

				pm.beginTask(commandStr, IProgressMonitor.UNKNOWN);
			    }
		    }
		    }
	    }   
	}
	catch (Exception e)
	{
	  //System.out.println(e);
	  	
	}
    }
    
     public void fillLocalToolBar() 
  	{
  		super.fillLocalToolBar();
    	if (!_isLocked)
	    {
			IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		
				ImageDescriptor image = _plugin.getImageDescriptor("lock");
			
				_lockAction = new LockViewAction("Lock View", image);
				_lockAction.setChecked(_viewer.isLocked());
				toolBarManager.add(_lockAction);

	    }
  	}
  	
  	public void lock(boolean flag)
    {
	if (_viewer.isLocked() != flag)
	    {
		_viewer.toggleLock();

		if (flag == true)
		    {
			if (_lockAction != null)
			    {
				IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
				toolBarManager.removeAll();
			    }
			_isLocked = flag;
		    }
	    }
    }
}










