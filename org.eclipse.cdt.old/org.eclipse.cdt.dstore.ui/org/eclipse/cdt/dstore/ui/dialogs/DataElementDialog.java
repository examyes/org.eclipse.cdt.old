package com.ibm.dstore.ui.dialogs;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.views.*;
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.widgets.*;
import com.ibm.dstore.ui.connections.*;

import org.eclipse.swt.widgets.*;

import org.eclipse.jface.viewers.*; 
import org.eclipse.jface.action.*; 
import org.eclipse.ui.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.ImageRegistry;

import java.util.*;
import java.lang.reflect.*;

import com.ibm.dstore.ui.actions.*;

public class DataElementDialog extends org.eclipse.jface.dialogs.Dialog implements IActionLoader
{
    private ObjectWindow _viewer;
    private DataElement  _input;
    private String       _title;
    private OpenEditorAction _openAction;
    
    public DataElementDialog(String title, DataElement input)
    {
	super(null);
	_input = input;
	_title = title;
    }

    protected void buttonPressed(int buttonId)
    {
	if (OK == buttonId)
	    {	
		setReturnCode(OK);
	    }
	else if (CANCEL == buttonId)
	    setReturnCode(CANCEL);
	else
	    setReturnCode(buttonId);
	close();
    }

    public Control createDialogArea(Composite parent)
    {
	Composite c = (Composite)super.createDialogArea(parent);

	GridLayout clayout= new GridLayout();
	clayout.numColumns = 2;
	clayout.marginHeight = 2;
	clayout.marginWidth = 2;
	c.setLayout(clayout);

	GridData cgrid = new GridData(GridData.FILL_BOTH);
	c.setLayoutData(cgrid);

	DataStore dataStore = _input.getDataStore();
	_viewer = new ObjectWindow(c, ObjectWindow.TABLE, dataStore, new ImageRegistry(), this);	
	_viewer.setInput(_input);

	GridLayout layout= new GridLayout();
	layout.numColumns = 1;
	layout.marginHeight = 2;
	layout.marginWidth = 2;
	_viewer.setLayout(layout);

	GridData tgrid = new GridData(GridData.FILL_BOTH);
	tgrid.heightHint = 300;
	tgrid.widthHint = 300;
	_viewer.setLayoutData(tgrid);
	
	getShell().setText(_title);

	return c;
    }

    public IOpenAction getOpenAction()
    {
	if (_openAction == null)
	    {
		_openAction = new OpenEditorAction(null);
	    }
	return _openAction;
    }

    public CustomAction getOpenPerspectiveAction()
    {
	return null;
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

}
