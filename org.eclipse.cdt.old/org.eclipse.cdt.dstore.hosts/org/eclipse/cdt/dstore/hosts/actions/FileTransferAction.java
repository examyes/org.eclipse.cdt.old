package com.ibm.dstore.hosts.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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
	DataElement localInput =  _plugin.getDataStore().getHostRoot().get(0).dereference().getParent();
	DataElement remoteInput = _subject.getDataStore().getHostRoot().get(0).dereference().getParent();

	DataElementFileTransferDialog ftd = new DataElementFileTransferDialog("Transfer Files", localInput, remoteInput);
	ftd.open();
    }    
}

