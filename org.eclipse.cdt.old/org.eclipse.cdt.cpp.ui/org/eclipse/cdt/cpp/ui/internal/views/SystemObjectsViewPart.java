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

import org.eclipse.swt.widgets.*;
import org.eclipse.core.resources.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.ui.*;

import java.util.*;

public class SystemObjectsViewPart extends ProjectViewPart
{
    public SystemObjectsViewPart()
    {
	super();
    }
 
    protected String getF1HelpId()
    {
	return CppHelpContextIds.SYSTEM_OBJECTS_VIEW;
    }

   
    public void doClear()
    {
	_viewer.setInput(null);
	_viewer.clearView();
	setTitle("System-Objects");
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

    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {
	DataStore dataStore = _plugin.getCurrentDataStore();

	DataElement includedSourceD = dataStore.findObjectDescriptor("Included Source");
	DataElement parsedSourceD = dataStore.findObjectDescriptor("Parsed Source");

	PTDataElementTableContentProvider provider = new PTDataElementTableContentProvider();

	// pass thru these
       	provider.addPTDescriptor(includedSourceD); 
       	provider.addPTDescriptor(parsedSourceD); 

	return new ObjectWindow(parent, ObjectWindow.TABLE, dataStore, _plugin.getImageRegistry(), loader, provider);
    }

    DataElement findParseFiles(DataElement theProjectFile)
    {
	DataStore dataStore = theProjectFile.getDataStore();
	ArrayList ref = theProjectFile.getAssociated("Parse Reference");
	if (ref != null && ref.size() > 0)
	    {
		DataElement parseProject = ((DataElement)ref.get(0)).dereference();
		DataElement sprojectObjects = dataStore.find(parseProject, DE.A_NAME, "System Objects", 1);
		return sprojectObjects;
	    }
	return null;
    }

    public DataElement doSpecificInput(DataElement theElement)
    {
	DataElement theInput = null;
	if (theElement == null)
	{
		return null;	
	}
		
	String type = theElement.getType();	
	if (type.equals("file"))
	    {
		theElement = _api.getProjectFor(theElement);		
	    }

	if (theElement != null && theElement.getType().equals("Project"))
	    {
		theInput = findParseFiles(theElement);
	    }

	if (theInput == null)
	    return null;
	
	
	//Finally just set the input and the title
	if (_viewer.getInput() == theInput)
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
				setTitle(theElement.getName() + " System-Objects");   
		
		 	  	_browseHistory.clear();
     		  	_browseHistory.add(theInput);
     	      	_browsePosition = 0;
     	      	updateActionStates();
	    	}
	    }
	return theInput;
 }
}










