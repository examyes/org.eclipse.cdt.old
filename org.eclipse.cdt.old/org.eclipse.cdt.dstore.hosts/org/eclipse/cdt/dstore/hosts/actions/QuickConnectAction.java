package com.ibm.dstore.hosts.actions;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.ui.connections.*;

import com.ibm.dstore.hosts.actions.*;

import com.ibm.dstore.extra.internal.extra.*;
import com.ibm.dstore.hosts.dialogs.*;
import com.ibm.dstore.hosts.*;

import com.ibm.dstore.core.*;
import com.ibm.dstore.core.miners.miner.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.util.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.server.*;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*; 
import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.ui.*;

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;

import org.eclipse.swt.widgets.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*;

public class QuickConnectAction implements Runnable
{
    private String _host;
    private String _port;
    private String _selected;
    private String _directory;
    private String _mountedDirectory;

    public QuickConnectAction(String host, String port, String directory)
    {
	_host = host;
	_port = port;
	_directory = directory;
	_selected = null;
    }

    public void run()
    {
	HostsPlugin plugin = HostsPlugin.getInstance();
	DataStore dataStore = plugin.getDataStore();

	Connection tempConnection = new Connection("temp",
						   _host, _port, 
						   "root", _directory, false, true, dataStore.getRoot());


	ConnectionStatus status = tempConnection.connect(dataStore.getDomainNotifier(), "com.ibm.dstore.miners/fs.dat");
	if (status != null)
	    {
		// command cancelled
	    }
	else if (status.isConnected())
	    {
		DataStore rmtDataStore = tempConnection.getDataStore();
		DataElement input = rmtDataStore.getHostRoot().get(0).dereference();
		input.expandChildren();
		DataElementFileDialog dialog = new DataElementFileDialog("Select Directory", 
									 input);
		dialog.open();
		if (dialog.getReturnCode() == dialog.OK)
		    { 
			DataElement selected = dialog.getSelected();
			if (selected != null)
			    {
				_selected = selected.getSource();
			    }
			
		    }
		tempConnection.disconnect();
	    }
	else
	    {		
		String msg = status.getMessage();
		if (!status.isConnected())
		    {
			MessageDialog failD = new MessageDialog(null, 
								"Connection Failure", 
								null, msg, 
								MessageDialog.INFORMATION,
								new String[]  { "OK" },
								0);
			
			
			failD.openInformation(new Shell(), "Connection Failure", msg);          
		    }
	    }
    }

    public String getSelected()
    {
	return _selected;
    }
}
