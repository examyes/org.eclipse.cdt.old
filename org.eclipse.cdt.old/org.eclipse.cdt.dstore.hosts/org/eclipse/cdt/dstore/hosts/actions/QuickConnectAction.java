package com.ibm.dstore.hosts.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
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

public class QuickConnectAction implements Runnable
{
    private String _host;
    private String _port;
    private String _selected;

    public QuickConnectAction(String host, String port)
    {
	_host = host;
	_port = port;
	_selected = null;
    }

    public void run()
    {
	System.out.println("Quick Connect " + _host + ":" + _port);
	HostsPlugin plugin = HostsPlugin.getInstance();
	DataStore dataStore = plugin.getDataStore();

	Connection tempConnection = new Connection("temp",
						   _host, _port, 
						   "root", "", false, true, dataStore.getRoot());

	ConnectionStatus status = tempConnection.connect(dataStore.getDomainNotifier());
	DataStore rmtDataStore = tempConnection.getDataStore();
	if (rmtDataStore != null && tempConnection.isConnected())
	    {
		DataElementFileDialog dialog = new DataElementFileDialog("Select Directory", rmtDataStore.getHostRoot());
		dialog.open();

		DataElement selected = dialog.getSelected();
		_selected = selected.getSource();
		tempConnection.disconnect();
	    }
    }

    public String getSelected()
    {
	return _selected;
    }
}
