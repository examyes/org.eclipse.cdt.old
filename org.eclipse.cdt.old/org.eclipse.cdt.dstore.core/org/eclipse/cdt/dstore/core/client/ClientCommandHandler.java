package org.eclipse.cdt.dstore.core.client;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.*;

import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.util.*;
import org.eclipse.cdt.dstore.core.model.*;

public class ClientCommandHandler extends CommandHandler
{
  private   Sender                _sender;
    private int _requests = 0;

  public ClientCommandHandler(Sender sender)
      {
        super();
        _sender = sender;
      }
 
  public synchronized void sendFile(String fileName, File file)
      {
	  DataElement document = _dataStore.createObject(null, "FILE", fileName, fileName, fileName);
	  _sender.sendFile(document, file, 1);
      }
      
  public synchronized void sendFile(String fileName, byte[] bytes, int size)
  {
	  DataElement document = _dataStore.createObject(null, "FILE", fileName, fileName, fileName);
	  _sender.sendFile(document, bytes, size);  	
  }
   
  public synchronized void sendAppendFile(String fileName, byte[] bytes, int size)
  {
  	DataElement document = _dataStore.createObject(null, "FILE", fileName, fileName, fileName);
  	_sender.sendAppendFile(document, bytes, size);
  }

  public synchronized void sendCommands()
    {
	  DataElement commandRoot = _dataStore.createObject(null, "DOCUMENT", "client.doc." + _requests++);
	  commandRoot.setParent(null);
	  while (_commands.size() > 0)
	      {
		  DataElement command = null;
		  synchronized(_commands)
		      {
			  command =  (DataElement)_commands.get(0);
			  _commands.remove(command);
		      }
		  
		  commandRoot.addNestedData(command, false);
	      }
	  
	  _sender.sendDocument(commandRoot, 3);
    }
}


