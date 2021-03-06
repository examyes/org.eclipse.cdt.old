package org.eclipse.cdt.dstore.ui.dialogs;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.views.*;
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.widgets.*;
import org.eclipse.cdt.dstore.ui.connections.*;

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

import org.eclipse.cdt.dstore.ui.actions.*;

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

    public Class forName(String source) throws ClassNotFoundException
    {
	return Class.forName(source);       
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

    public void loadCustomActions(IMenuManager menu, DataElement input, DataElement descriptor)
    {
    }

	public String getImageString(String name)
	{
		return null;
	}
	
    public String getImageString(DataElement object)
    {
    		DataStore dataStore   = object.getDataStore();

	String baseDir        = dataStore.getAttribute(DataStoreAttributes.A_PLUGIN_PATH); 			
	String type           = object.getType();

	StringBuffer iconPath = new StringBuffer(baseDir);

	if (type.equals(DE.T_OBJECT_DESCRIPTOR) || 
	    type.equals(DE.T_RELATION_DESCRIPTOR) ||
	    type.equals(DE.T_ABSTRACT_OBJECT_DESCRIPTOR) ||
	    type.equals(DE.T_ABSTRACT_RELATION_DESCRIPTOR))
            {
		type = object.getName();
		String subDir = object.getSource();
		if (subDir.length() > 0)
		    {
			iconPath.append(subDir);
		    }
		else
		    {
			iconPath.append("org.eclipse.cdt.dstore.core");
		    }
            }
	else
            {
		DataElement descriptor = object.getDescriptor();
		if (descriptor != null)
		    {
			String subDir = descriptor.getSource(); 
			if (subDir.length() > 0)
			    {
				iconPath.append(subDir);
			    }
			else
			    {
				iconPath.append("org.eclipse.cdt.dstore.core");
			    }
		    }
		else
		    {
			iconPath.append("org.eclipse.cdt.dstore.ui");
		    }
            }

	iconPath.append(java.io.File.separator);	
	iconPath.append("icons");
	iconPath.append(java.io.File.separator);
	iconPath.append(type);
	iconPath.append(".gif");        

	return iconPath.toString();
  
    }     

}
