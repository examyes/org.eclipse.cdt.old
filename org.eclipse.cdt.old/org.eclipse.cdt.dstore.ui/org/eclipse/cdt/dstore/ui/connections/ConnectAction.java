package org.eclipse.cdt.dstore.ui.connections;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.dialogs.*;
import org.eclipse.cdt.dstore.ui.actions.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public abstract class ConnectAction extends ConnectActionDelegate
{
	private DataStoreUIPlugin _plugin = DataStoreUIPlugin.getDefault();
  
    protected void checkEnabledState(IAction action, Connection connection)
    {
   		action.setEnabled(!connection.isConnected());
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
								_plugin.getLocalizedString("dialog.Connection_Failure"), 
								null, msg, 
								MessageDialog.INFORMATION,
								new String[]  { "OK" },
								0);
			
			
				failD.openInformation(new Shell(), 								
					      _plugin.getLocalizedString("dialog.Connection_Failure"), 
					      msg);          
		    }
		else
		    {
		    	selected.setDepth(2);
		    }
	    }
    }	
}

