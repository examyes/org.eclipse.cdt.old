package com.ibm.dstore.core.server;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.util.*;

import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;

public class ServerUpdateHandler extends UpdateHandler
{
  private ArrayList            _senders;
  private CommandGenerator  _commandGenerator;

  public ServerUpdateHandler()
    {
      _senders = new ArrayList();
      _commandGenerator = new CommandGenerator();
    }

  public void setDataStore(DataStore dataStore)
  {
    super.setDataStore(dataStore);
    _commandGenerator.setDataStore(dataStore);
  }


  public void addSender(Sender sender)
    {
      _senders.add(sender);
    }

  public void removeSender(Sender sender)
    {
      _senders.remove(sender);
      if (_senders.size() == 0)
      {
        finish();
      }
    }

  public synchronized void updateFile(String path, byte[] bytes, int size)
    {
	DataElement document = _dataStore.createElement();
	document.reInit("FILE",
			path,
			path,
			path);
	  
	for (int j = 0; j < _senders.size(); j++)
	    { 
		Sender sender = (Sender)_senders.get(j);
		sender.sendFile(document, bytes, size);
	    }	
    }

    public synchronized void updateAppendFile(String path, byte[] bytes, int size)
    {
	DataElement document = _dataStore.createElement();
	document.reInit("FILE",
			path,
			path,
			path);
	
	for (int j = 0; j < _senders.size(); j++)
	    { 
		Sender sender = (Sender)_senders.get(j);
		sender.sendAppendFile(document, bytes, size);
	    }	
    }

  public synchronized void updateFile(File file, DataElement associatedElement)
      {
	  
	  DataElement document = _dataStore.createElement();
	  document.reInit("FILE",
			  file.getAbsolutePath(),
			  file.getAbsolutePath(),
			  file.getAbsolutePath());
	  
	  for (int j = 0; j < _senders.size(); j++)
	      { 
		  Sender sender = (Sender)_senders.get(j);
		  sender.sendFile(document, file, 1);
	      }
      }

  public void sendUpdates()
      {
	  synchronized(_dataObjects)
	      {
		  DataElement document = _dataStore.createObject(null, "DOCUMENT", "doc");
		  DataElement response = _commandGenerator.generateResponse(document, _dataObjects);
		  
		  for (int j = 0; j < _senders.size(); j++)
		      {
			  Sender sender = (Sender)_senders.get(j);
			  sender.sendDocument(document, 5);			  
		      }

		  
		  for (int i = 0; i < _dataObjects.size(); i++)
		      {
			  DataElement obj = (DataElement)_dataObjects.get(i);
			  clean(obj);
		      }

		  _dataObjects.clear();		  
	      }

      }

  // this only happens at the end
  public void removeSenderWith(Socket socket)
    {
      for (int i = 0; i < _senders.size(); i++)
      {
        Sender sender = (Sender)_senders.get(i);
        if (sender.socket() == socket)
        {
          // sender sends last ack before death
	    DataElement document = _dataStore.createElement();
	    document.reInit("DOCUMENT", "exit", "exit", "");
	    sender.sendDocument(document, 2);
	    removeSender(sender);
        }
      }
    }
}
