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

public class BrowseProcessesDialog extends org.eclipse.jface.dialogs.Dialog
    implements Listener
{
    private ObjectWindow _viewer;
    private DataElement  _input;
    private String       _title;
    private CppPlugin    _plugin;

    private boolean      _useFilter;
    private DataElement  _selection;

    public BrowseProcessesDialog(String title, DataElement input)
    {
	super(null);
	_plugin = CppPlugin.getDefault();

	DataStore dataStore = null;	
	if (input != null)
	    {
		dataStore = input.getDataStore();	
	    }
	else
	    {
		dataStore = _plugin.getCurrentDataStore();
	    }

	_input = dataStore.find(dataStore.getHostRoot(), DE.A_TYPE, "Processes", 1);
		
	_title = title;
	_useFilter = true;
    }

    protected void buttonPressed(int buttonId)
    {
	if (OK == buttonId)
	    {	
		setReturnCode(OK);
		if (_viewer != null)
		    {
			_selection =_viewer.getSelected();
		    }
		else
		    {
			_selection = null;
		    }
	    }
	else if (CANCEL == buttonId)
	    setReturnCode(CANCEL);
	else
	    setReturnCode(buttonId);
	close();
    }

    public DataElement getSelected()
    {
	return _selection;
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

	if (_input != null)
	    {
		DataStore dataStore = _input.getDataStore();	
		_viewer = new ObjectWindow(c, ObjectWindow.TABLE, dataStore, _plugin.getImageRegistry(), CppActionLoader.getInstance());	
		_viewer.setInput(_input);
	
		GridLayout layout= new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		_viewer.setLayout(layout);
		
		GridData tgrid = new GridData(GridData.FILL_BOTH);
		tgrid.heightHint = 200;
		tgrid.widthHint = 500;
		_viewer.setLayoutData(tgrid);
	    }
	else
	    {
	
		GridLayout layout= new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 2;
		layout.marginWidth = 2;

		Composite cnr = new Composite(c, SWT.NONE);
		Label label = new Label(cnr, SWT.NULL);
		label.setText("Unable to obtain process list");
		cnr.setLayout(layout);
		cnr.setLayoutData(new GridData(GridData.FILL_BOTH));
	    }
	
	getShell().setText(_title);

	return c;
    }
    
    public boolean close()
    {
	if (_viewer != null)
	    {
		_viewer.dispose();
	    }
    	return super.close();
    }

    public void handleEvent(Event e)
    {
    }
}
