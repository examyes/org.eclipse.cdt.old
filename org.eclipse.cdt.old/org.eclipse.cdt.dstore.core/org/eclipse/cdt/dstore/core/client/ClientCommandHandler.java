package com.ibm.dstore.core.client;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import java.io.*;

import com.ibm.dstore.core.client.*;
import com.ibm.dstore.core.util.*;
import com.ibm.dstore.core.model.*;

public class ClientCommandHandler extends CommandHandler
{
  private   Sender                _sender;

  public ClientCommandHandler(Sender sender)
      {
        super();
        _sender = sender;
      }

  public synchronized void sendFile(String fileName, File file)
      {
	  DataElement document = _dataStore.createElement();
	  document.reInit("FILE", fileName, fileName, fileName);
	  _sender.sendFile(document, file, 2);
      }

  public synchronized void sendCommands()
    {
	  DataElement commandRoot = _dataStore.createElement();
	  commandRoot.reInit("DOCUMENT", "do", "doc", "");
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


