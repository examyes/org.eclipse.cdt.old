package com.ibm.cpp.ui.internal.views;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.cpp.ui.internal.api.*;
import com.ibm.cpp.ui.internal.*;

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.widgets.*;

import com.ibm.dstore.core.model.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;
import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.ui.*;

import java.util.*;

public class ProjectObjectsViewPart extends ProjectViewPart
{
	
    public ProjectObjectsViewPart()
    {
		super();
    }

    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {
	DataStore dataStore = _plugin.getCurrentDataStore();

	DataElement parsedSourceD = dataStore.findObjectDescriptor("Parsed Source");

	PTDataElementTableContentProvider provider = new PTDataElementTableContentProvider();

	// pass thru these
	provider.addPTDescriptor(parsedSourceD);


	return new ObjectWindow(parent, ObjectWindow.TABLE, dataStore, _plugin.getImageRegistry(), loader, provider);
    }
    
    

 public void selectionChanged(IWorkbenchPart part, ISelection sel) 
 {
     if (part instanceof CppProjectsViewPart)
	 {
	     try
		 {
		     Object object = ((IStructuredSelection)sel).getFirstElement();
		     if (object instanceof DataElement)
			 doSpecificInput((DataElement)object);
		 }
	     catch (ClassCastException e)
		 {
		 }	
	 } 
     else if (part instanceof org.eclipse.ui.views.navigator.ResourceNavigator)
	 {

	     try
		 {
		     Object object = ((IStructuredSelection)sel).getFirstElement();
		     if (object instanceof IResource)
			 {
			     IResource res = (IResource)object;
			     DataElement element = _api.findResourceElement(res);
			     if (element != null)
				 {
				     doSpecificInput(element);
				 }
			 }
		 }
	     catch (ClassCastException e)
		 {
		 }	

	 }

 }
     
 protected String getF1HelpId()
 {
  return CppHelpContextIds.PROJECT_OBJECTS_VIEW;
 }
    
 public void doClear()
 {
  _viewer.setInput(null);
  _viewer.clearView();
  setTitle("C/C++ Objects");
 }

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
	      if ((event.getStatus() == CppProjectEvent.START) || (event.getStatus() == CppProjectEvent.DONE))
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
    


 public DataElement doSpecificInput(DataElement theElement)
 {
     DataElement theInput = null;
     if (theElement.getType().equals("file"))
	 {
	     boolean scopeView = false;
	     ArrayList scope = _plugin.readProperty("ScopeProjectObjectsView");
	     if (scope.size() > 0)
		 {
		     String pref = (String)scope.get(0);
		     if (pref.equals("Yes"))
			 {
			     scopeView = true;
			 }
		 }

	     if (scopeView)
		 {
		     theInput = _api.findParseFile(theElement);
		 }
	     else
		 {
		     theElement = _api.getProjectFor(theElement);
		 }
	 }
     
     if (theElement.getType().equals("Project"))
	 {
	     theInput = _api.findParseFiles(theElement);
	 }


     if (theInput == null)
     {
	 	return null;
     }
     
     DataElement oldInput = _viewer.getInput();
     
     //Finally just set the input and the title
     if (oldInput == theInput)
	 {
	     // this is too expensive
	     //_viewer.resetView();
	 }
     else
	 {
	 	 if (!_browseHistory.contains(theInput))
	 	{
	     _viewer.setInput(theInput);	
	     _viewer.selectRelationship("contents");
	     setTitle(theElement.getName() + " Project-Objects");   
	     
	 	      _browseHistory.clear();
     		  _browseHistory.add(theInput);
     	      _browsePosition = 0;
     	      updateActionStates();
	 	}
	 }
	 
	 return theInput;
 }


}








