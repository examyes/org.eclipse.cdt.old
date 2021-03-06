package org.eclipse.cdt.dstore.hosts.dialogs;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.hosts.*;

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
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;

public class DataElementFileDialog extends org.eclipse.jface.dialogs.Dialog
    implements Listener
{
    private ObjectWindow _viewer;
    private DataElement  _input;
    private HostsPlugin  _plugin;
    private String       _title;

    private boolean      _onlyDirectories;
    private DataElement  _selected;

    private Button       _back;
    private Button       _forward;

    private IActionLoader _actionLoader;

    public DataElementFileDialog(String title, DataElement input)
    {
	super(null);
	_input = input;
	_plugin = HostsPlugin.getInstance();
	_title = title;
	_onlyDirectories = false;
    }

    public DataElementFileDialog(String title, DataElement input, boolean showDirectories)
    {
	super(null);
	_input = input;
	_plugin = HostsPlugin.getInstance();
	_title = title;
	_onlyDirectories = showDirectories;
    }

    protected void buttonPressed(int buttonId)
    {
	if (OK == buttonId)
	    {	
		setReturnCode(OK);
		_selected = _viewer.getSelected();
	    }
	else if (CANCEL == buttonId)
	    setReturnCode(CANCEL);
	else
	    setReturnCode(buttonId);
	close();
    }

    public DataElement getSelected()
    {
	return _selected;
    }

    public void setActionLoader(IActionLoader loader)
    {
	_actionLoader = loader;
    }

    public IActionLoader getActionLoader()
    {
	if (_actionLoader == null)
	    {
		_actionLoader = _plugin.getActionLoader();
	    }

	return _actionLoader;
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

	Composite b = new Composite(c, SWT.NONE);
	_back = new Button(b, SWT.FLAT);
	_back.addListener(SWT.Selection, this);
	_back.setImage(_plugin.getImageDescriptor("up").createImage());	

	_forward = new Button(b, SWT.FLAT);
	_forward.addListener(SWT.Selection, this);
	_forward.setImage(_plugin.getImageDescriptor("down").createImage());	
	
	b.setLayout(new GridLayout());
	GridData data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
	b.setLayoutData(data);


	DataStore dataStore = _input.getDataStore();
	_viewer = new ObjectWindow(c, ObjectWindow.TREE, dataStore, _plugin.getImageRegistry(), getActionLoader());
	
	if (_input.isOfType("Filesystem Objects") || _input.getType().equals("data"))
	    {
		_viewer.setInput(_input);
		_input.expandChildren();
	    }
	else
	    {
		_viewer.setInput(dataStore.getHostRoot());
	    }
	    
	_viewer.fixateOnRelationType("contents");
	if (_onlyDirectories)
	{
		_viewer.fixateOnObjectType("Directories");
	}
	_viewer.setSorter(DE.P_NAME);

	GridLayout layout= new GridLayout();
	layout.numColumns = 1;
	layout.marginHeight = 2;
	layout.marginWidth = 2;
	_viewer.setLayout(layout);

	GridData tgrid = new GridData(GridData.FILL_BOTH);
	tgrid.heightHint = 300;
	tgrid.widthHint = 300;
	_viewer.setLayoutData(tgrid);
    _viewer.getViewer().getControl().addListener(SWT.Selection, this);

	_forward.setEnabled(false);
	
	getShell().setText(_title);

	return c;
    }

    public void handleEvent(Event e)
    {
	Widget widget = e.widget;
	if (widget == _viewer.getViewer().getControl())
	{
		DataElement selection = _viewer.getSelected();
		if (selection != null)	
		{
			_forward.setEnabled(true);	
		}
		else
		{
			_forward.setEnabled(false);	
		}
	}
	else if (widget == _back)
	    {
		DataElement input = _viewer.getInput();
		if (input != null)
		    {

			if (input != null && (input.isOfType("directory") || input.isOfType("device")))
			    {
				DataElement parent = input.getParent();				
				
				if (parent != null && (parent.isOfType("directory") || parent.isOfType("device")))
				    {
					_viewer.setInput(parent);
			
				    }
			    }
		    }
	    }
	else if (widget == _forward)
	    {
		DataElement input = _viewer.getSelected();
		if (input != null)
		    {
			if (input.isOfType("directory") || input.isOfType("device"))
			    {
				_viewer.setInput(input);
				_forward.setEnabled(false);
			    }
		    }
	    }
    }
    
    public boolean close()
    {
    	_viewer.dispose();
    	return super.close();
    }
}
