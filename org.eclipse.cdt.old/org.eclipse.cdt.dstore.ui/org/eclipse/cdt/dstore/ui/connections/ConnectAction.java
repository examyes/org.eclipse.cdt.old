package com.ibm.dstore.ui.connections;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.ui.*;
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

public class ConnectAction extends CustomAction
{
    protected Connection _connection;

    public ConnectAction(DataElement subject, String label, DataElement command, DataStore dataStore)
    {	
        super(subject, label, command, dataStore);
        
        _connection = ConnectionManager.getInstance().findConnectionFor(subject);
        setEnabled(!_connection.isConnected());
      }
    
    public void run()
    {
	DataElement selected = _subject;
	ConnectionStatus status = _connection.connect(_dataStore.getDomainNotifier());
	if (status != null)
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
		else
		    {
		    }
	    }
    }
	
}

