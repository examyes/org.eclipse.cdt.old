package org.eclipse.cdt.dstore.hosts.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.ui.connections.*;

import org.eclipse.cdt.dstore.hosts.actions.*;

import org.eclipse.cdt.dstore.extra.internal.extra.*;
import org.eclipse.cdt.dstore.hosts.dialogs.*;
import org.eclipse.cdt.dstore.hosts.*;

import org.eclipse.cdt.dstore.core.*;
import org.eclipse.cdt.dstore.core.miners.miner.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.util.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.server.*;

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
    private boolean _useDaemon = true;

    public QuickConnectAction(String host, String port, String directory)
    {
	_host = host;
	_port = port;
	_directory = directory;
	_selected = null;
    }

    public QuickConnectAction(String host, String port, String directory, boolean useDaemon)
    {
	_host = host;
	_port = port;
	_directory = directory;
	_selected = null;
	_useDaemon = useDaemon;
    }

    public void run()
    {
	HostsPlugin plugin = HostsPlugin.getInstance();
	DataStore dataStore = plugin.getDataStore();

	DataElement temp = dataStore.getTempRoot();
	Connection tempConnection = new Connection("temp",
						   _host, _port, 
						   "root", _directory, false, _useDaemon, 
						   temp);


	ConnectionStatus status = tempConnection.connect(dataStore.getDomainNotifier(), "org.eclipse.cdt.dstore.miners/fs.dat");
	if (status == null)
	    {
		// command cancelled
	    }
	else if (status.isConnected())
	    {
		DataStore rmtDataStore = tempConnection.getDataStore();
		
		DataElement host = rmtDataStore.getHostRoot();
		
		DataElement input = host.get(0).dereference();
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
