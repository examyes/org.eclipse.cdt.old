package com.ibm.dstore.ui.connections;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.ui.*;
import com.ibm.dstore.ui.dialogs.*;
import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.model.*;
import com.ibm.dstore.ui.actions.*;

import java.io.*; 
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.dialogs.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

public class DisconnectAction extends CustomAction
{
  public DisconnectAction(DataElement subject, String label, DataElement command, DataStore dataStore)
      {	
        super(subject, label, command, dataStore);

        Connection connection = ConnectionManager.getInstance().findConnectionFor(subject);
        setEnabled(connection.isConnected());
      }

  public void run()
      {
        DataElement selected = _subject;
        
        ConnectionManager manager = ConnectionManager.getInstance();
        manager.disconnect(selected);
      }  
}

