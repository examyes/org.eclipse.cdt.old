package org.eclipse.cdt.dstore.ui.connections;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.dialogs.*;
import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.ui.actions.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public abstract class DisconnectAction extends ConnectActionDelegate
{      
  protected void checkEnabledState(IAction action, Connection connection)
  {
  	action.setEnabled(connection.isConnected());	
  }

  public void run()
      {
        DataElement selected = _subject;
        
        ConnectionManager manager = getConnectionManager();
        manager.disconnect(selected);
        selected.setDepth(1);
      }  
}

