package com.ibm.dstore.core.server;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.server.*;
import com.ibm.dstore.core.model.*;

import com.ibm.dstore.core.miners.miner.*;

import java.io.*;
import java.util.*;
import java.lang.*;

public class ServerCommandHandler extends CommandHandler
{
    private ArrayList            _miners;
    private ILoader              _loader;

  public ServerCommandHandler(ILoader loader)
    {
      super();

      _loader = loader;
      _miners = new ArrayList();
    }

  public void setDataStore(DataStore dataStore)
  { 
      super.setDataStore(dataStore);
  }
    
    public void loadMiners()
    {
	if (_dataStore != null)
	    {
		MinerLoader minerLoader = new MinerLoader(_dataStore, _loader);
		
		// load the miners
		_miners = minerLoader.loadMiners();
	    } 
    }
    

  public void finish()
  {
    for (int i = 0; i < _miners.size(); i++)
      {
	  Miner miner = (Miner)_miners.get(i);
	  miner.finish();	
      }

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
	  else if (commandName.equals("C_SET_MINERS"))
	      {
		  DataElement location = (DataElement)command.get(1);
		  _dataStore.setMinersLocation(location);
		  status.setAttribute(DE.A_NAME, _dataStore.getLocalizedString("model.done"));	
		  _dataStore.refresh(status);
	      }
          else if (commandName.equals("C_SCHEMA"))
	      {
		  if (_miners.size() == 0)
		      {
			  loadMiners(); 
		      }
		  
		  DataElement schemaRoot = _dataStore.getDescriptorRoot();
		  
		  // update all descriptor objects
		  _dataStore.refresh(schemaRoot);		  
		  status.setAttribute(DE.A_NAME, _dataStore.getLocalizedString("model.done"));
		  _dataStore.refresh(status);
	      }
	  else if (_dataStore.validTicket())
	      {
		  if (status != null)
		      {
			      boolean failure = false;
			      for (int j = 0; (j < _miners.size()) && !failure; j++)
				  {
				      Miner miner = (Miner)_miners.get(j);
				      
				      
				      if (commandSource.equals("*") || commandSource.equals(miner.getClass().getName()))
					  {
					      // System.out.println(commandName);
					      //System.out.println(miner.getName());

					      status = miner.command(command);
					      
					      if ((status != null) && 
						  status.getAttribute(DE.A_NAME).equals(_dataStore.getLocalizedString("model.incomplete")))
						  {
						      failure = true;
						  }

					      // System.out.println("done");
					  }		

				  }
		      }
	      }
	_dataStore.refresh(status);
	
	}
    }	

    public ArrayList getMiners()
  {
    return _miners;
  }

  public Miner getMiners(String name)
  {
    for (int i = 0; i < _miners.size(); i++)
      {
	Miner miner = (Miner)_miners.get(i);
	if (miner.getClass().getName().equals(name))
	  {
	    return miner;	
	  }	
      }

    return null;
  }

  public void finishMiner(String name)
  {
    Miner miner = getMiners(name);
    miner.finish();

    _miners.remove(miner);
  }


  public void sendFile(String fileName, File file)
      {
      }
}
