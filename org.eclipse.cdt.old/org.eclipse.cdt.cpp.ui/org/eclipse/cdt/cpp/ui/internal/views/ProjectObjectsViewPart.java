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
import org.eclipse.core.resources.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.ui.*;

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
   break;
  }
 }



 public void doSpecificInput(DataElement theElement)
 {
     DataElement theInput = null;
     if (theElement.getType().equals("file"))
	 {
	     theInput = findParseFile(theElement);
	 }
     else if (theElement.getType().equals("Project"))
	 {
	     theInput = findParseFiles(theElement);
	 }


     if (theInput == null)
	 return;
     
     //Finally just set the input and the title
     if (_viewer.getInput() == theInput)
	 _viewer.resetView();
     else
	 {
	     _viewer.setInput(theInput);	
	     _viewer.selectRelationship("contents");
	 }
 }

 DataElement findParseFiles(DataElement theProjectFile)
    {
	DataStore dataStore = theProjectFile.getDataStore();
	DataElement parseProject = ((DataElement)(theProjectFile.getAssociated("Parse Reference").get(0))).dereference();
	DataElement projectObjects = dataStore.find(parseProject, DE.A_NAME, "Project Objects", 1);
	return projectObjects;
    }

 DataElement findParseFile(DataElement theProjectFile)
 {
  String projectFileSource1 = theProjectFile.getSource().replace('\\','/');
  String projectFileSource2 = theProjectFile.getSource().replace('/','\\');
  DataStore dataStore = theProjectFile.getDataStore();

  
  while (!(theProjectFile = theProjectFile.getParent()).getType().equals("Project")) 
      {
      }

     DataElement parseProject = ((DataElement)(theProjectFile.getAssociated("Parse Reference").get(0))).dereference();
     DataElement parsedFiles  = dataStore.find(parseProject, DE.A_NAME, "Parsed Files", 1);
     DataElement theParseFile = dataStore.find(parsedFiles, DE.A_SOURCE, projectFileSource1, 1);
     if (theParseFile == null)
	 {
	     theParseFile = dataStore.find(parsedFiles, DE.A_SOURCE, projectFileSource2, 1);
	 }
     
     if (theParseFile == null)
	 {
	     DataElement dummyInput = dataStore.find(dataStore.getTempRoot(), DE.A_NAME, "Non-Parsed File", 1);
	     if (dummyInput != null)
		 {
		     theParseFile = dummyInput;
		     DataElement theMessage = ((DataElement)(theParseFile.getNestedData().get(0)));
		     theMessage.setAttribute(DE.A_VALUE,projectFileSource1  + " has not been parsed.");
		     dataStore.refresh(theMessage);
		 }
	     else
		 {
		     theParseFile = dataStore.createObject(dataStore.getTempRoot(), "Output", "Non-Parsed File");
		     dataStore.createObject(theParseFile, "warning", projectFileSource1 + " has not been parsed.");
		 }
	 }
     
     
     return theParseFile;
 }
}








