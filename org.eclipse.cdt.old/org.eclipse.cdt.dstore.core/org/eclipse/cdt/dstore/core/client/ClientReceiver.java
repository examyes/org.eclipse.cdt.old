package org.eclipse.cdt.dstore.core.client;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.util.*;
import org.eclipse.cdt.dstore.core.model.*;

import java.lang.*;
import java.net.*;
import java.io.*;

public class ClientReceiver extends Receiver
{
  private boolean _receivedHandshake = false;
  
  public ClientReceiver(Socket socket, DataStore dataStore)
      {
        super(socket, dataStore);
      }

  public void handleDocument(DataElement documentObject)
    {
      if (documentObject.getName().equals("exit"))
      {
        _canExit = true;
        _receivedHandshake = false;
      }
      else if (documentObject.getName().equals("accept"))
      {
      	_receivedHandshake = true;	
      }
      else
      {        
      	if (_receivedHandshake)
      	{
		  synchronized(documentObject)
	      {
			  for (int i = 0; i < documentObject.getNestedSize(); i++)
		      {
				  DataElement rootOutput = documentObject.get(i);
				  _dataStore.refresh(rootOutput);
		      }
		     documentObject.removeNestedData();
		     _dataStore.deleteObject(documentObject.getParent(), documentObject);
	      }
      	}
      	else
      	{      	
      		_canExit = true;
      	}	
      }
    }
    
    public void handleError(Exception e) 
    {
		DataElement status = _dataStore.getStatus();
		status.setAttribute(DE.A_NAME, e.getMessage());
		_dataStore.refresh(status);
    }
}
