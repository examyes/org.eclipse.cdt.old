package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.hosts.views.OutputViewer;

import com.ibm.cpp.ui.internal.*;
import com.ibm.cpp.ui.internal.actions.*; 
import com.ibm.cpp.ui.internal.vcm.*;
import com.ibm.cpp.ui.internal.api.*;

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.widgets.ObjectWindow;
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.actions.CustomAction;

import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.ui.resource.*;

import com.ibm.dstore.ui.views.*;

import org.eclipse.help.*;
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

public class ObjectsViewPart extends GenericViewPart 
    implements ICppProjectListener
{
    private   DataStore      _currentDataStore;    
    protected ModelInterface _api;
    protected CppPlugin      _plugin;

    public ObjectsViewPart()
    {
	super();
	_plugin = CppPlugin.getDefault();
	_api = _plugin.getModelInterface();
    }
    
    protected String getF1HelpId()
    {
     return "com.ibm.cpp.ui.objects_view_context";
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
	return new ObjectWindow(parent, 0, dataStore, _plugin.getImageRegistry(), this);
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
			
			if (object instanceof IDataElementContainer)
			    {
				if (object instanceof Repository)
				    {		    
					changed = _plugin.setCurrentProject((Repository)object);		
				    }
				else if (object instanceof ResourceElement)
				    {
					ResourceElement theElement = (ResourceElement)object;
					changed = _plugin.setCurrentProject(theElement);
				    }	
			    }	    
			else if (object instanceof IResource)
			    {		
				if (object instanceof ResourceElement)
				    {		    
					ResourceElement theElement = (ResourceElement)object;
					changed = _plugin.setCurrentProject(theElement);		
				    }
				else if (object instanceof Repository)
				    {
					DataStore dataStore = ((Repository)object).getDataStore();		
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
			  DataElement theParent = theObject;
			  while ((theParent != null) 
				 && (!theParent.getType().equals("Project")) 
				 && (!theParent.getType().equals("directory"))  
				 && (!theParent.getType().equals("data")))
			      theParent = theParent.getParent();

			  if ( (theParent != null) && (!theParent.getType().equals("data")))
			      {
				  DataStore dataStore = theParent.getDataStore();
				  _plugin.setCurrentDataStore(dataStore);
				  if (theParent.getType().equals("Project"))
				      {
					  changed = _plugin.setCurrentProject(theParent);
				      }

				  initDataElementInput(theParent);
				  dataElement = true;
			      }
			  
                         }
			
			if (changed)
			    {	
				DataStore dataStore = _plugin.getCurrentDataStore();
				if (dataStore != null)
				    dataStore.getDomainNotifier().enable(true);
			    }	    

			if(!dataElement)
			 initInput(_plugin.getCurrentDataStore());		
		    }	
	    }
    }

    public void selectionChanged(IWorkbenchPart part, ISelection sel) 
    {
   	if ((part instanceof ResourceNavigator) || 
            (part instanceof CppProjectsViewPart)
	    )
	    {
		if (sel instanceof IStructuredSelection)
		    {
			IStructuredSelection es= (IStructuredSelection) sel;
			handleSelection(es);
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
    
    public IOpenAction getOpenAction()
    {
	if (_openAction == null)
	    {
		_openAction = new com.ibm.cpp.ui.internal.actions.OpenEditorAction(null);
	    }
	return _openAction;
    }
    
    public CustomAction loadAction(String source, String name)
    {
	CustomAction newAction = null;
	try
	    {
		Object[] args = { name};
		Class actionClass = Class.forName(source);
		Constructor constructor = actionClass.getConstructors()[0];
		newAction = (CustomAction)constructor.newInstance(args);
	    }
	catch (ClassNotFoundException e)
	    {
		System.out.println(e);
	    }
	catch (InstantiationException e)
	    { 
		System.out.println(e);
	    }
	catch (IllegalAccessException e)
	    {
		System.out.println(e);	
	    }
	catch (InvocationTargetException e)
	    {
		System.out.println(e);
	    }
	
        return newAction;
    }
    
    public CustomAction loadAction(java.util.List objects, DataElement descriptor)
    {
	return loadAction((DataElement)objects.get(0), descriptor);
    }

    public CustomAction loadAction(DataElement object, DataElement descriptor)
    {
        String name = descriptor.getName();
        String source = descriptor.getSource();
        
        CustomAction newAction = null; 
        try
	    {         
	        Object[] args = {object, name, descriptor, object.getDataStore()};
	
         	Class actionClass = Class.forName(source);
		Constructor constructor = actionClass.getConstructors()[0];
                newAction = (CustomAction)constructor.newInstance(args);
	    }
        catch (ClassNotFoundException e)
	    {
		System.out.println(e);
	    }
        catch (InstantiationException e)
	    {
		System.out.println(e);
	    }
        catch (IllegalAccessException e)
	    {
		System.out.println(e);
	    }
        catch (InvocationTargetException e)
	    {
		System.out.println(e);
	    }
	
        return newAction;
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
	
	if (event.getType() == CppProjectEvent.COMMAND)
	    {
		DataElement commandStatus = event.getObject();
		if (commandStatus != null)
		    {
		DataElement commandObject = commandStatus.getParent();
		
		if ((commandObject != null) && (event.getProject() != null))
		    {
			String commandStr = "Issuing " + commandObject.getValue() + " on " + event.getProject().getName();
			mgr.setMessage(commandStr);
			
			if (event.getStatus() == CppProjectEvent.DONE)
			    {
				//mgr.setCancelEnabled(false);
				pm.done();
			    }
			else if (event.getStatus() == CppProjectEvent.START)
			    {
				//mgr.setCancelEnabled(true);
				pm.beginTask(commandStr, 100);
			    }
			else
			    {
				pm.worked(5);
			    }		 
		    }
		    }
	    }   
    }
}










