package com.ibm.dstore.ui.connections;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */
 
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.ui.connections.*;

import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.extra.internal.extra.*;

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
