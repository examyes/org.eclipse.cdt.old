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

public class DictionarySearchDialog extends SearchDialog 
{
    public DictionarySearchDialog(String title)
    {
	super(title, null, "Pattern", "Find");
    }

    public void handleEvent(Event e)
    {
	super.handleEvent(e);
	Widget widget = e.widget;
	if (widget == _search)
	    {
		DataStore dataStore = _plugin.getDataStore();
		DataElement dictionaryData =  dataStore.findMinerInformation("com.ibm.dstore.miners.dictionary.DictionaryMiner");

		DataElement english = dataStore.find(dictionaryData, DE.A_NAME, "english", 1);
		DataElement pattern = dataStore.createObject(null, "pattern", _searchEntry.getText());
		DataElement search = dataStore.localDescriptorQuery(english.getDescriptor(), "C_SEARCH_DICTIONARY", 1);
		if (search != null)
		    {	       
			DataElement status = dataStore.command(search, pattern, english);		
			_resultViewer.setInput(status);
		    }
	    }
    }

}
