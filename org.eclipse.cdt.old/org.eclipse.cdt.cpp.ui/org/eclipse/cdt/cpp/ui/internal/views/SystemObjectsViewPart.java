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

    public ObjectWindow createViewer(Composite parent, IActionLoader loader)
    {
	DataStore dataStore = _plugin.getCurrentDataStore();

	DataElement includedSourceD = dataStore.findObjectDescriptor("Included Source");

	PTDataElementTableContentProvider provider = new PTDataElementTableContentProvider();

	// pass thru these
       	provider.addPTDescriptor(includedSourceD); 


	return new ObjectWindow(parent, ObjectWindow.TABLE, dataStore, _plugin.getImageRegistry(), loader, provider);
    }

    public void doSpecificInput(DataElement projectParseInformation)
    {
	//Find the System Objects Object under the projectParseInformation
	DataElement systemObjects = projectParseInformation.getDataStore().find(projectParseInformation, DE.A_NAME, "System Objects", 1);
	if (systemObjects == null)
	    return;
	
	
	//Finally just set the input and the title
	if (_viewer.getInput() == systemObjects)
	    {
		_viewer.resetView();
	    }
	else
	    {
		_viewer.setInput(systemObjects);	
		_viewer.selectRelationship("contents");
		setTitle(projectParseInformation.getName() + " System-Objects");   
	    }
	
 }
}










