package com.ibm.dstore.core.client;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.util.*;
import com.ibm.dstore.core.model.*;

import java.lang.*;
import java.net.*;
import java.io.*;

public class ClientReceiver extends Receiver
{
  public ClientReceiver(Socket socket, DataStore dataStore)
      {
        super(socket, dataStore);
      }

  public void handleDocument(DataElement documentObject)
    {
      if (documentObject.getName().equals("exit"))
      {
        _canExit = true;
      }
      else
      {        
        for (int i = 0; i < documentObject.getNestedSize(); i++)
        {
          DataElement rootOutput = documentObject.get(i);
          _dataStore.refresh(rootOutput);
        }

      }
    }
}
