package com.ibm.dstore.core.server;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.util.*;
import com.ibm.dstore.core.model.*;

import java.lang.*;
import java.net.*;
import java.io.*;

public class ServerReceiver extends Receiver
{
  ConnectionEstablisher _connection;

  public ServerReceiver(Socket socket, ConnectionEstablisher connection)
      {
        super(socket, connection.dataStore());
        _connection = connection;
      }

  public void handleDocument(DataElement documentObject)
    {
      // parse request and determine what is wanted
      for (int a = 0; a < documentObject.getNestedSize(); a++)
      {
        DataElement rootOutput = (DataElement)documentObject.get(a);

	DataElement log = _dataStore.getLogRoot();
	log.addNestedData(rootOutput, false);	
	
        if (rootOutput.getName().equals("C_EXIT"))
        {
          finish();
          _connection.finished(this);
        }
        else
        {
          _dataStore.command(rootOutput);
        }
      }
    }

    /**
     * @see Receiver#finish()
     */
    public void handleError(Exception e) 
    {
	e.printStackTrace();

	_connection.finished(this);
    }

}
