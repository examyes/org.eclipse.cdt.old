package com.ibm.dstore.core.model;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.util.*;
import com.ibm.dstore.extra.internal.extra.*;

import java.util.*;
import java.lang.*;
import java.io.*;

public abstract class CommandHandler extends Handler
{
  protected   ArrayList             _commands;
  private     CommandGenerator      _commandGenerator;

  public CommandHandler()
  {
    super();
    _commands = new ArrayList();
    _commandGenerator = new CommandGenerator();
  }

  public void setDataStore(DataStore dataStore)
  {
    super.setDataStore(dataStore);
    _commandGenerator.setDataStore(dataStore);
  }

  public DataStore getDataStore()
  {
    return _dataStore;
  }

    public void addCommand(DataElement command, boolean immediate)
    {
	synchronized(_commands)
	    {
		if (!_commands.contains(command))
		    {
			if (immediate)
			    {
				_commands.add(0, command);			  
			    }
			else
			    {
				_commands.add(command);
			    }
		    }
	    }
    }

    public void handle()
      { 
          if (!_commands.isEmpty())
          { 
	      sendCommands();
          }
      }

  public DataElement command(DataElement commandDescriptor, ArrayList arguments, DataElement object, 
			     boolean refArg, boolean immediate)
      {
	DataElement command = _commandGenerator.generateCommand(commandDescriptor, arguments, object, refArg);
	return command(command, immediate);
      }

  public DataElement command(DataElement commandDescriptor, DataElement objectDescriptor, DataElement object, 
			     boolean refArg, boolean immediate)
      {
	DataElement command = _commandGenerator.generateCommand(commandDescriptor, objectDescriptor, object, refArg);
	return command(command, immediate);
      }

  public DataElement command(DataElement commandDescriptor, DataElement object, boolean refArg)
      {
	DataElement command = _commandGenerator.generateCommand(commandDescriptor, object, refArg);
	return command(command);
      }

  public DataElement command(DataElement cmd)
    {
	return command(cmd, false);
    }

  public DataElement command(DataElement cmd, boolean immediate)
      {	
	DataElement status = null;
        if ((cmd != null) && _dataStore != null)
	    {
		status = _dataStore.find(cmd, DE.A_TYPE, _dataStore.getLocalizedString("model.status"), 1);		
		if (status != null && !status.getName().equals(_dataStore.getLocalizedString("model.done")))
		    //if (status != null)
		    {
			addCommand(cmd, immediate);		  
		    }
	    }
	
	return status;
      }

    public synchronized void cancelAllCommands()
    {
	DataElement log = _dataStore.getLogRoot();
	for (int i = 0; i < _commands.size(); i++)
	    {
		log.removeNestedData((DataElement)_commands.get(i));		
	    }
	
	_commands.clear();
    } 

 
  // implemented by extending classes
  public abstract void sendCommands();
  public abstract void sendFile(String fileName, File file);
}
