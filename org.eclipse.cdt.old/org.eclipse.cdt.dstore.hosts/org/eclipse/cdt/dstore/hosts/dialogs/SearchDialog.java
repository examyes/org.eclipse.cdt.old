package org.eclipse.cdt.dstore.hosts.dialogs;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.hosts.*;
import org.eclipse.cdt.dstore.hosts.actions.*;

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
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;

public abstract class SearchDialog extends org.eclipse.jface.dialogs.Dialog 
    implements Listener
{
    protected StyledText    _searchEntry;

    protected Button       _search;
    protected Button       _cancel;

    protected ObjectWindow _resultViewer;

    protected HostsPlugin  _plugin;
    protected String       _title;
    protected String       _patternLabel;
    protected String       _actionLabel;
    
    protected DataElement  _root;
    protected int          _width;
    protected int          _height;

    protected IActionLoader _actionLoader;

	protected String        _searchText;

    public SearchDialog(String title, DataElement root, String patternLabel, String actionLabel)
    {
	super(null);
	_plugin = HostsPlugin.getInstance();
	_title = title;
	_root = root;

	_patternLabel = patternLabel;
	_actionLabel = actionLabel;
	_height = 250;
	_width = 250;
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
	clayout.numColumns = 4;
	clayout.marginHeight = 5;
	clayout.marginWidth = 5;
	c.setLayout(clayout);

	GridData cgrid = new GridData(GridData.FILL_BOTH);
	c.setLayoutData(cgrid);

	Label searchLabel = new Label(c, SWT.NONE);
	searchLabel.setText(_patternLabel);

	_searchEntry = new StyledText(c, SWT.BORDER | SWT.SINGLE);
	GridData egrid = new GridData(GridData.FILL_HORIZONTAL);
	egrid.widthHint = 100;
	_searchEntry.setLayoutData(egrid);

	_search = new Button(c, SWT.PUSH);
	_search.setText(_actionLabel);
	_search.addListener(SWT.Selection, this);
	GridData sgrid = new GridData(GridData.FILL_HORIZONTAL);
	sgrid.widthHint = 50;
	_search.setLayoutData(sgrid);

	_cancel = new Button(c, SWT.PUSH);
	_cancel.setText("Stop");
	_cancel.addListener(SWT.Selection, this);
	GridData cangrid = new GridData(GridData.FILL_HORIZONTAL);
	cangrid.widthHint = 50;
	_cancel.setLayoutData(cangrid);
	
	_resultViewer = new ObjectWindow(c, ObjectWindow.TABLE, _plugin.getDataStore(), 
					 _plugin.getImageRegistry(), getActionLoader());


	GridLayout vlayout = new GridLayout();
	_resultViewer.setLayout(vlayout);

	GridData lvgrid = new GridData(GridData.FILL_BOTH);
	lvgrid.heightHint = _height;
	lvgrid.widthHint = _width;
	lvgrid.horizontalSpan = 4;
	_resultViewer.setLayoutData(lvgrid);

	_searchEntry.setFocus();
	_searchEntry.addListener(SWT.KeyUp, new Listener() 
		    {
			public void handleEvent(Event e) 
			{
			    if (e.character == '\r') // "enter" key
				{
					_searchText = _searchEntry.getText();
				   handleSearch();
				} 
			}
		});
	getShell().setText(_title);
	return c;
    }

    public void handleEvent(Event e)
    {
	Widget widget = e.widget;
	if (widget == _cancel)
	    {
		DataElement status = _resultViewer.getInput();
		DataStore dataStore = status.getDataStore();
		DataElement cmd = status.getParent();
		DataElement cancelCmd = dataStore.localDescriptorQuery(cmd, "C_CANCEL");
		if (cancelCmd != null)
		    {
			dataStore.command(cancelCmd, cmd);
		    }
	    }
	  else if (widget == _search)
	  {
	  	_searchText = new String(_searchEntry.getText());
	  	handleSearch();	
	  }
    }
    
    public boolean close()
    {
    	_resultViewer.dispose();
    	return super.close();
    }
    
    protected abstract void handleSearch();
}
