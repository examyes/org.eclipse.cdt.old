package com.ibm.dstore.hosts.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.actions.*;

import com.ibm.dstore.hosts.*;
import com.ibm.dstore.hosts.dialogs.*;

import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*; 

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public class FileTransferAction extends CustomAction
{
    private HostsPlugin _plugin;

    public FileTransferAction(DataElement subject, String label, DataElement command, DataStore dataStore)
    {	
	super(subject, label, command, dataStore);
	_plugin = HostsPlugin.getInstance();
    }

    public void run()
    {
	DataStore localDataStore  = _plugin.getDataStore();
	DataStore remoteDataStore = _subject.getDataStore();

	if (localDataStore != remoteDataStore)
	    {
		DataElement localInput =  localDataStore.getHostRoot().get(0).dereference().getParent();		

		DataElement remoteInput = _subject;
		if (_subject.getType().equals("host"))
		    {
			remoteInput = remoteDataStore.getHostRoot().get(0).dereference().getParent();
		    }

		DataElementFileTransferDialog ftd = new DataElementFileTransferDialog("Transfer Files", 
										      localInput, remoteInput);
		ftd.open();
	    }
    }    
}

