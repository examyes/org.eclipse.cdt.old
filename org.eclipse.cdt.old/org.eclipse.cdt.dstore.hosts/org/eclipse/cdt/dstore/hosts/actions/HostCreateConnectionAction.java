 package org.eclipse.cdt.dstore.hosts.actions;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.hosts.*;
 
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.ConvertUtility;
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
 
 
 public class HostCreateConnectionAction extends Action
  {
  	public HostCreateConnectionAction(String title, ImageDescriptor descriptor)
  	{
  		super(title, descriptor);	
  	}
  	
    public void run()
        {
          ConnectDialog dialog = new ConnectDialog("Create New Connection");	      
          dialog.open();
          if (dialog.getReturnCode() != dialog.OK)
            return;
          
          String name = dialog.getName();
          String host = dialog.getHostIP();
          String port = dialog.getPort();
          String dir  = dialog.getHostDirectory();


			HostsPlugin plugin = HostsPlugin.getDefault();
			
		  ConnectionManager mgr = plugin.getConnectionManager();
		  DataStore dataStore = plugin.getDataStore();
		  DataElement xRoot = dataStore.getExternalRoot();
		  
		  			
          Connection con = new Connection(name, host, port, "root",  dir, dialog.isLocal(), dialog.isUsingDaemon(), xRoot);
          mgr.getConnections().add(con);
          DataElement connectionRoot = con.getRoot();
          //dataStore.createReference(xRoot, connectionRoot);
          
        }
  }