package com.ibm.dstore.hosts.dialogs;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.hosts.*;
import com.ibm.dstore.hosts.actions.*;

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

public class DictionarySearchDialog extends org.eclipse.jface.dialogs.Dialog 
    implements Listener
{
    private Text         _searchEntry;

    private Button       _search;
    private ObjectWindow _resultViewer;

    private HostsPlugin  _plugin;
    private String       _title;

    public DictionarySearchDialog(String title)
    {
	super(null);
	_plugin = HostsPlugin.getInstance();
	_title = title;
    }

    public Control createDialogArea(Composite parent)
    {
	Composite c = (Composite)super.createDialogArea(parent);

	GridLayout clayout= new GridLayout();
	clayout.numColumns = 3;
	clayout.marginHeight = 5;
	clayout.marginWidth = 5;
	c.setLayout(clayout);

	GridData cgrid = new GridData(GridData.FILL_BOTH);
	c.setLayoutData(cgrid);

	Label searchLabel = new Label(c, SWT.NONE);
	searchLabel.setText("Search");

	_searchEntry = new Text(c, SWT.BORDER);
	GridData egrid = new GridData(GridData.FILL_HORIZONTAL);
	egrid.widthHint = 100;
	//	egrid.horizontalSpan = 2;
	_searchEntry.setLayoutData(egrid);

	_search = new Button(c, SWT.PUSH);
	_search.setText("Run");
	_search.addListener(SWT.Selection, this);
	
	_resultViewer = new ObjectWindow(c, 0, _plugin.getDataStore(), 
					 _plugin.getImageRegistry(), _plugin.getDialogActionLoader(), true);


	GridLayout vlayout = new GridLayout();
	_resultViewer.setLayout(vlayout);

	GridData lvgrid = new GridData(GridData.FILL_BOTH);
	lvgrid.heightHint = 250;
	lvgrid.widthHint = 250;
	lvgrid.horizontalSpan = 3;
	_resultViewer.setLayoutData(lvgrid);

	getShell().setText(_title);
	return c;
    }

    public void handleEvent(Event e)
    {
	Widget widget = e.widget;
	if (widget == _search)
	    {
		DataStore dataStore = _plugin.getDataStore();
		DataElement dictionaryData =  dataStore.findMinerInformation("com.ibm.dstore.miners.dictionary.DictionaryMiner");
		DataElement root = dictionaryData.get(0);
		DataElement pattern = dataStore.createObject(null, "pattern", _searchEntry.getText());
		DataElement search = dataStore.localDescriptorQuery(root.getDescriptor(), "C_SEARCH_DICTIONARY", 1);
		if (search != null)
		    {	       
			DataElement status = dataStore.command(search, pattern, root);		
			_resultViewer.setInput(status);
		    }
	    }
    }

}
