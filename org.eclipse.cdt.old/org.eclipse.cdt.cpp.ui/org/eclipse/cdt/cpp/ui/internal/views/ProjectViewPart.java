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


public abstract class ProjectViewPart extends ObjectsViewPart implements ISelectionListener
{
    private IProject _input = null;

    public ProjectViewPart()
    {
	super();
    }
    
    public void createPartControl(Composite parent)
    {
	super.createPartControl(parent);   
    }
    
    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {
	DataStore dataStore = _plugin.getCurrentDataStore();
	return new ObjectWindow(parent, 0, dataStore, _plugin.getImageRegistry(), this, true);
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
		else if (!project.isOpen())
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
  doSpecificInput(projectParseInformation);
 }
 
    public abstract void doClear();
 
  public void doInput(IProject project)
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
      
      doSpecificInput(projectParseInformation);
  } 	
    
 public abstract void doSpecificInput(DataElement projectParseInformation);
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
		    if (event.getStatus() == CppProjectEvent.START || 
			event.getStatus() == CppProjectEvent.DONE)
			{
			    doInput(project);
			}
		}
		super.projectChanged(event);
		break;

	    default:
		super.projectChanged(event);
		break;
	    }
    }

 
}










