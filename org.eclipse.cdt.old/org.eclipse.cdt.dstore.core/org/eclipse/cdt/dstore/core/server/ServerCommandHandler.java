package org.eclipse.cdt.dstore.core.server;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.util.*;
import org.eclipse.cdt.dstore.core.model.*;

import org.eclipse.cdt.dstore.core.miners.miner.*;

import java.io.*;
import java.util.*;
import java.lang.*;

public class ServerCommandHandler extends CommandHandler
{
    private ArrayList            _loaders;
    private MinerLoader          _minerLoader;
    
    public ServerCommandHandler(ArrayList loaders)
    {
	super();
	_loaders = loaders;
    }
        
    public void setDataStore(DataStore dataStore)
    { 
	super.setDataStore(dataStore);
    }
    
    public void loadMiners()
    {
	if (_dataStore != null)
	    {
		if (_minerLoader == null)
		    {
			_minerLoader = new MinerLoader(_dataStore, _loaders);
		    }		
		// load the miners
		_minerLoader.loadMiners();


	    } 
    }
    
    public ArrayList getMiners()
    {
	return _minerLoader.getMiners();
    }
    
    public Miner getMiner(String name) 
    {
	return _minerLoader.getMiner(name);
    }
    
    public void finishMiner(String name)
    {
	_minerLoader.finishMiner(name);
    }
    
    public void finish()
    {
	_minerLoader.finishMiners();
	super.finish();
    }

  public void sendCommands()
    {
        // send commands to the appropriate miners
        while (_commands.size() > 0)
        {
	    DataElement command = null;
	    
	    synchronized(_commands)
		{
		    command =  (DataElement)_commands.get(0);
		    _commands.remove(command);
		}

          DataElement status = _dataStore.find(command, DE.A_TYPE, _dataStore.getLocalizedString("model.status"), 1);	

          String commandSource = command.getSource();	
          String commandName   = command.getName();

	  if (commandName.equals("C_VALIDATE_TICKET"))
	      {
		  DataElement serverTicket   = _dataStore.getTicket();
		  DataElement clientTicket   = (DataElement)command.get(0);
		  String st = serverTicket.getName();
		  String ct = clientTicket.getName();
		  if (ct.equals(st))
		      {
			  serverTicket.setAttribute(DE.A_VALUE, _dataStore.getLocalizedString("model.valid"));
			  clientTicket.setAttribute(DE.A_VALUE, _dataStore.getLocalizedString("model.valid"));
			  
			  DataElement host = _dataStore.getHostRoot();
			  _dataStore.getHashMap().remove(host.getId());
			  host.setAttribute(DE.A_ID, "host." + serverTicket.getName());
			  _dataStore.getHashMap().put(host.getId(), host);
			  _dataStore.update(host);
		      }
		  else
		      {
			  serverTicket.setAttribute(DE.A_VALUE, _dataStore.getLocalizedString("model.invalid"));
			  clientTicket.setAttribute(DE.A_VALUE, _dataStore.getLocalizedString("model.invalid"));
		      }
		  _dataStore.update(clientTicket);
		  status.setAttribute(DE.A_NAME, _dataStore.getLocalizedString("model.done"));
	      }
	  else if (commandName.equals("C_SET"))
	      {
		  DataElement dataObject = (DataElement)command.get(0);			  
		  status.setAttribute(DE.A_NAME, _dataStore.getLocalizedString("model.done"));		
	      }
	  else if (commandName.equals("C_MODIFY"))
	      {
		  DataElement dataObject = (DataElement)command.get(0);			  
		  DataElement original = _dataStore.find(dataObject.getId());
		  original.setAttributes(dataObject.getAttributes());
		  status.setAttribute(DE.A_NAME, _dataStore.getLocalizedString("model.done"));		
	      }
	  else if (commandName.equals("C_SET_HOST"))
	      {
		  DataElement dataObject = (DataElement)command.get(0);	
		  
		  DataElement original = _dataStore.getHostRoot();
		  original.setAttributes(dataObject.getAttributes());
		  
		  _dataStore.setAttribute(DataStoreAttributes.A_LOCAL_PATH, dataObject.getSource());
		  _dataStore.setAttribute(DataStoreAttributes.A_HOST_PATH, dataObject.getSource());
		  status.setAttribute(DE.A_NAME, _dataStore.getLocalizedString("model.done"));		
	      }
	  else if (commandName.equals("C_ADD_MINERS"))
	      {
		  DataElement location = (DataElement)command.get(1);
		  _dataStore.addMinersLocation(location);
		  status.setAttribute(DE.A_NAME, _dataStore.getLocalizedString("model.done"));	
	      }
          else if (commandName.equals("C_SCHEMA"))
	      {
		  loadMiners(); 	  
		  
		  DataElement schemaRoot = _dataStore.getDescriptorRoot();
		  
		  // update all descriptor objects
		  _dataStore.refresh(schemaRoot);		  
		  status.setAttribute(DE.A_NAME, _dataStore.getLocalizedString("model.done"));
	      }
	  else if (_dataStore.validTicket())
	      {
		  if (status != null)
		      {
			  boolean failure = false;
			  ArrayList miners = _minerLoader.getMiners();
			  for (int j = 0; (j < miners.size()) && !failure; j++)
			      {
				  Miner miner = (Miner)miners.get(j);
				  
				  
				  if (commandSource.equals("*") || commandSource.equals(miner.getClass().getName()))
				      {
					  status = miner.command(command);
					  
					  if ((status != null) && 
						  status.getAttribute(DE.A_NAME).equals(_dataStore.getLocalizedString("model.incomplete")))
					      {
						  failure = true;
					      }
				      }		
				  
			      }
		      }
	      }
	  
	  _dataStore.refresh(status);
	  
	}
    }	
    
    public void sendFile(String fileName, File file)
    {
	// look for a file handler before defaulting to datastore	  	
  	_dataStore.saveFile(fileName, file);
    }
    
      
  public void sendFile(String fileName, byte[] bytes, int size, boolean binary)
  {  
  	// look for a file handler before defaulting to datastore
  		
  	_dataStore.saveFile(fileName, bytes, size, binary);
  }
  
  public void sendAppendFile(String fileName, byte[] bytes, int size, boolean binary)
  {
  	// look for a file handler before defaulting to datastore
  	_dataStore.appendToFile(fileName, bytes, size, binary);
  }
  
}
