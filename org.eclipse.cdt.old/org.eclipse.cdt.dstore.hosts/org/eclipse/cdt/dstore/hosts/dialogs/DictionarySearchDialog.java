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

public class DictionarySearchDialog extends SearchDialog 
{
    public DictionarySearchDialog(String title)
    {
	super(title, null, "Pattern", "Find");
    }

    protected void handleSearch()
    {
	    {
		DataStore dataStore = _plugin.getDataStore();
		DataElement dictionaryData =  dataStore.findMinerInformation("org.eclipse.cdt.dstore.miners.dictionary.DictionaryMiner");

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
