package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.*;

import com.ibm.dstore.core.model.*;

import org.eclipse.jface.action.*;
import org.eclipse.core.resources.*; 
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.ui.*;
import java.util.*;
import org.eclipse.jface.resource.*;

public abstract class ProjectViewPart extends ObjectsViewPart implements ISelectionListener
{
    private IProject _input = null;
    private DataElement _specificInput = null;
	protected ArrayList _browseHistory;
	protected int _browsePosition;
	
	private HomeAction _homeAction = null;
	private BackAction _backAction = null;
	private ForwardAction _forwardAction = null;
	private DrillAction _drillAction = null;

    public ProjectViewPart()
    {
		super();
		_browseHistory = new ArrayList();
		_browsePosition = 0;
    }
    
    public void createPartControl(Composite parent)
    {
	super.createPartControl(parent);   
    }
    
    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {
	DataStore dataStore = _plugin.getCurrentDataStore();
	return new ObjectWindow(parent, ObjectWindow.TABLE, dataStore, _plugin.getImageRegistry(), loader);
    }
    
    public IActionLoader getActionLoader() 
    {
	return CppActionLoader.getInstance();
    }

    public void initInput(DataStore dataStore)
    {
	IProject project = _plugin.getCurrentProject();
	if (project != null)
	    {	
		if (project.isOpen() && _input != project)	  
		    {
			doInput(project);
		    }
		else if (!project.isOpen() && _input == project)
		    {
			doClear();
		    }
	    } 
    }  
 public void initDataElementInput(DataElement theProject)
 {
     ArrayList parseReferences = theProject.getAssociated("Parse Reference");
     if (parseReferences.size() < 1)
	 return;
     
     DataElement projectParseInformation = ((DataElement)parseReferences.get(0)).dereference();
     if (projectParseInformation == null)
	 return;

     _specificInput = doSpecificInput(projectParseInformation);
  
 }
    
    public abstract void doClear();
 
  public void doInput(IProject project)
  {
      if (project != null && project.isOpen())
	  {
	      //Grab the project DataElement
	      DataElement projectObj = _plugin.getModelInterface().findProjectElement(project);
	      
	      if (projectObj == null)
		  { 
		      return;
		  }
	      
	      
	      //Get the reference to the Project's Parse Data
	      ArrayList parseReferences = projectObj.getAssociated("Parse Reference");
	      if (parseReferences.size() < 1)
		  {
		      return;
		  }
	      
	      DataElement projectParseInformation = ((DataElement)parseReferences.get(0)).dereference();
	      if (projectParseInformation == null)
		  return;

	      if (_specificInput != projectParseInformation)
		  {
		      _specificInput = doSpecificInput(projectParseInformation);		  
		  }
		        
		        
		 
	  } 	
  }
    
	 public abstract DataElement doSpecificInput(DataElement projectParseInformation);

 public void projectChanged(CppProjectEvent event)
    {
	int type = event.getType();
	IProject project = event.getProject();
	switch (type)
	    {
	    case CppProjectEvent.OPEN:
		{
		    doInput(project);
		}
		break;
	    case CppProjectEvent.CLOSE:
	    case CppProjectEvent.DELETE:
		{
		    doClear();
		}
		break;
		
	    case CppProjectEvent.COMMAND:
		{
		    if (event.getStatus() == CppProjectEvent.START 
				|| event.getStatus() == CppProjectEvent.DONE
			)
			{
			    doInput(project);
			    
			    DataElement input = _viewer.getInput();		    
			    if (input != null && input.isDeleted())
			    {
			    	_homeAction.run();
			    }
			}

		super.projectChanged(event);

		break;
		}
		
	    default:
		super.projectChanged(event);
		break;
	    }
    }

  class BrowseAction extends Action
 {
 	protected ArrayList _history = null;
 	public BrowseAction(String label, ImageDescriptor des)
 	{
 		super(label, des);
 		
 		_history = _browseHistory;
 		setToolTipText(label);
 	}
 	
 	public void checkEnabledState()
 	{
 	}
 	
 	public void run()
 	{
 	}
 	 	
 	
 	public void setPosition(int i)
 	{
 		setInput((DataElement)_history.get(i));
 	}
 }
 
 class BackAction extends BrowseAction
 {
 	public BackAction(String label, ImageDescriptor des)
 	{
 		super(label, des);
 		
 	}
 	
 	public void checkEnabledState()
 	{
 		setEnabled(false);
 		if (_browseHistory.size() > 1)
 		{
 			if (_browsePosition > 0)
 			{
 				setEnabled(true);
 			}
 		}

 	}
 	
 	public void run()
 	{
 		_browsePosition--;
 		setPosition(_browsePosition);
 		updateActionStates();
 	}
 }
 
 class ForwardAction extends BrowseAction
 {
 	public ForwardAction(String label, ImageDescriptor des)
 	{
 		super(label, des);
 					
 	}
 	
 	public void checkEnabledState()
 	{
 		 setEnabled(false);
 		if (_history.size() > 1)
 		{
 			if (_browsePosition < (_history.size() - 1))
 			{
 				setEnabled(true);
 			}
 		}

 	}
 	
 	public void run()
 	{
 		_browsePosition++;
 		setPosition(_browsePosition);
 		updateActionStates();
 	}
 }
 
 class HomeAction extends BrowseAction
 {
 	public HomeAction(String label, ImageDescriptor des)
 	{
 		super(label, des);
 	}
 	
 	public void run()
 	{
 		if (_history.size() > 0)
 		{
 			DataElement input = (DataElement)_history.get(0);
 			_viewer.setInput(input);	
 			_browsePosition = 0;
 			updateActionStates();
 		}
 	}
 }
 
 class DrillAction extends BrowseAction
 {
 	
 	public DrillAction(String label, ImageDescriptor des)
 	{
 		super(label, des);		
 	}
 	
 	public void checkEnabledState()
 	{
 	}
 	
 	public void run()
 	{
 		DataElement selected = _viewer.getSelected();
 		if (selected != null)
 		{
 			int i = _history.size() - 1;
 			if (_history.size() > 1)
 			{
 				for (; i > _browsePosition; i--)
 				{
 					_history.remove(i);	
 				}
 			}
 			
 			_history.add(selected);
 			_browsePosition = i + 1;
			_viewer.setInput(selected);		
 			updateActionStates();
 		}
 	}
 	
 }
 
  public void updateActionStates()
  {
   if (_homeAction == null)
    fillLocalToolBar();
 	_homeAction.checkEnabledState();
 	_forwardAction.checkEnabledState();
 	_backAction.checkEnabledState();
 	_drillAction.checkEnabledState(); 	
  }
 
  public void fillLocalToolBar()
    {
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		
		
		_homeAction = new HomeAction("Home", _plugin.getImageDescriptor("home"));
		toolBarManager.add(_homeAction);
		_backAction = new BackAction("Back", _plugin.getImageDescriptor("back"));
		toolBarManager.add(_backAction);
		
		_forwardAction = new ForwardAction("Forward", _plugin.getImageDescriptor("forward"));
    	toolBarManager.add(_forwardAction);
				
		_drillAction = new DrillAction("Drill Down Into", _plugin.getImageDescriptor("drill"));	 
    	toolBarManager.add(_drillAction); 
    	
    	updateActionStates();
    	    	
    }
   
}










