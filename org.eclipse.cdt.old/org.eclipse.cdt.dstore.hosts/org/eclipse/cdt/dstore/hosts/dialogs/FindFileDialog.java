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
import org.eclipse.swt.events.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;

public class FindFileDialog extends SearchDialog 
{
    public FindFileDialog(String title, DataElement root)
    {
	super(title, root, 
	      "File Pattern", 
	      "Find");

	_width = 350;
    }


    public void handleEvent(Event e)
    {
	super.handleEvent(e);
	
	Widget widget = e.widget;

	if (widget == _search)
	    {
		DataStore dataStore = _root.getDataStore();
		DataElement searchCmd  = dataStore.localDescriptorQuery(_root.getDescriptor(), "C_FIND_FILE", 4);
		DataElement pattern = dataStore.createObject(null, "pattern", _searchEntry.getText());

		if (searchCmd != null)
		    {	       
			DataElement status = dataStore.command(searchCmd, pattern, _root);		
			_resultViewer.setViewer(DE.P_SOURCE_NAME);
			_resultViewer.setInput(status);
		    }
	    }
    }

}
