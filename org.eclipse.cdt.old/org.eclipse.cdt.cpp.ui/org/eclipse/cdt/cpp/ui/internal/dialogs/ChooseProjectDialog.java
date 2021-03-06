package org.eclipse.cdt.cpp.ui.internal.dialogs;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.cpp.ui.internal.*;

import org.eclipse.cdt.cpp.ui.internal.views.*;
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

import java.util.List;

public class ChooseProjectDialog extends org.eclipse.jface.dialogs.Dialog
    implements Listener
{
    private ObjectWindow _viewer;
    private DataElement  _input;
    private String       _title;
    private CppPlugin    _plugin;

    private IStructuredSelection _selection;
	private boolean      _useFilter;

    public ChooseProjectDialog(String title, DataElement input)
    {
	super(null);
	_input = input;
	_title = title;
	_plugin = CppPlugin.getDefault();
	_useFilter = true;
    }

    protected void buttonPressed(int buttonId)
    {
	if (OK == buttonId)
	    {	
		setReturnCode(OK);
		_selection =_viewer.getSelection();
	    }
	else if (CANCEL == buttonId)
	    setReturnCode(CANCEL);
	else
	    setReturnCode(buttonId);
	close();
    }

    public List getSelected()
    {
	if (_selection != null)
	    {
		return _selection.toList();
	    }
	else
	    {
		return null;
	    }
    }

	public void useFilter(boolean flag)
	{
		_useFilter = flag;
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
	_viewer = new ObjectWindow(c, ObjectWindow.TREE, dataStore, _plugin.getImageRegistry(), CppActionLoader.getInstance());	
	_viewer.setInput(_input);
	_viewer.fixateOnRelationType("contents");
	if (_useFilter)
	{
		_viewer.fixateOnObjectType("Project Containers");
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
    
    public boolean close()
    {
    	_viewer.dispose();
    	return super.close();
    }

    public void handleEvent(Event e)
    {
    }
}
