package com.ibm.dstore.hosts.dialogs;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.hosts.*;

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
	_onlyDirectories = true;
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
	_back.setImage(_plugin.getImageDescriptor("up.gif").createImage());	

	_forward = new Button(b, SWT.FLAT);
	_forward.addListener(SWT.Selection, this);
	_forward.setImage(_plugin.getImageDescriptor("down.gif").createImage());	
	
	b.setLayout(new GridLayout());
	GridData data = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
	b.setLayoutData(data);


	DataStore dataStore = _input.getDataStore();
	_viewer = new ObjectWindow(c, 0, dataStore, _plugin.getImageRegistry(), _plugin.getDialogActionLoader());
	
	if (_input.isOfType("Filesystem Objects"))
	    {
		_viewer.setInput(_input);
	    }
	else
	    {
		_viewer.setInput(dataStore.getHostRoot());
	    }

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

    public void handleEvent(Event e)
    {
	Widget widget = e.widget;
	if (widget == _back)
	    {
		DataElement input = _viewer.getInput();
		DataElement parent = input.getParent();
		if (parent.getType().equals("directory") || 
		    parent.getType().equals("device") || 
		    parent.getType().equals("data"))
		    {
			_viewer.setInput(parent);
		    }
	    }
	else if (widget == _forward)
	    {
		DataElement input = _viewer.getSelected();
		_viewer.setInput(input);
	    }
    }
}
