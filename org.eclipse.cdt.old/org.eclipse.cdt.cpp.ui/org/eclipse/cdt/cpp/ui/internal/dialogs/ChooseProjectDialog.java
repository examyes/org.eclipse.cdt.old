package com.ibm.cpp.ui.internal.dialogs;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.cpp.ui.internal.*;
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

import java.util.List;

public class ChooseProjectDialog extends org.eclipse.jface.dialogs.Dialog
    implements Listener
{
    private ObjectWindow _viewer;
    private DataElement  _input;
    private String       _title;
    private CppPlugin    _plugin;

    private IStructuredSelection _selection;

    public ChooseProjectDialog(String title, DataElement input)
    {
	super(null);
	_input = input;
	_title = title;
	_plugin = CppPlugin.getDefault();
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
	return _selection.toList();
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
	_viewer = new ObjectWindow(c, 0, dataStore, _plugin.getImageRegistry(), null);	
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

    public void handleEvent(Event e)
    {
    }
}
