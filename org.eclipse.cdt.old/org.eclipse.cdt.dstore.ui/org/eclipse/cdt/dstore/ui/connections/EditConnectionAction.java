package org.eclipse.cdt.dstore.ui.connections;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.ui.dialogs.*;
import org.eclipse.cdt.dstore.ui.connections.*;

import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.resource.*;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;

import java.util.*;
import java.io.*;


public class EditConnectionAction extends CustomAction
{
    private DataElement _host;

    public EditConnectionAction(DataElement subject, String label, DataElement command, DataStore dataStore)
    {
	super(label);
	_host = subject;
    }
    
    public void run()
    {
	Connection connection = ConnectionManager.getInstance().findConnectionFor(_host);

	ConnectDialog dialog = new ConnectDialog("Edit Connection", connection);	      
	dialog.open();
	if (dialog.getReturnCode() != dialog.OK)
	    return;
	
	String name = dialog.getName();
	String host = dialog.getHostIP();
	String port = dialog.getPort();
	String dir  = dialog.getHostDirectory();
	
	connection.setName(name);
	connection.setHost(host);
	connection.setPort(port);
	connection.setDir(dir);
	connection.setIsLocal(dialog.isLocal());
	connection.setIsUsingDaemon(dialog.isUsingDaemon());

	_host.getDataStore().refresh(_host);
    }
}
