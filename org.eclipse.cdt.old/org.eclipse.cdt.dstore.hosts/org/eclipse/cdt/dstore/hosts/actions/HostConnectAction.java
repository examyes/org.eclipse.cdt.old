package com.ibm.dstore.hosts.actions;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.hosts.*;
import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.connections.*;
import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.ui.actions.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public class HostConnectAction extends ConnectAction
{
  public HostConnectAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);
      }
 
  public void run()
      {
	  super.run();

	  DataStore dataStore = _connection.getDataStore();
	  if (dataStore != null && _connection.isConnected())
	      {
		  HostsPlugin plugin = HostsPlugin.getInstance();
		  plugin.extendSchema(dataStore.getDescriptorRoot());
	      }
      }  
}

