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
import org.eclipse.jface.viewers.*;

public abstract class ConnectActionDelegate extends DataElementActionDelegate
{
    protected Connection _connection;
      
    protected abstract ConnectionManager getConnectionManager();
 
 	public void selectionChanged(IAction action, ISelection selection)
  	{
 		super.selectionChanged(action, selection);
 		if (_subject != null)
 		{
 			ConnectionManager connectionManager = getConnectionManager();
 			_connection = connectionManager.findConnectionFor(_subject);
 			checkEnabledState(action, _connection);
 		}
  	}
  	
  	protected abstract void checkEnabledState(IAction action, Connection connection);
    public abstract void run();
}