package com.ibm.dstore.miners.settings;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;
import java.util.*;
import java.lang.*;
import java.io.*;


public class SettingsMiner extends Miner
  {
    private DataElement _ubt;
    private DataElement _cbt;
    private DataElement _tl;

   public void load()
    {
      _ubt = _dataStore.createObject(_minerData, "property", "Update Buffer Time");
      _cbt = _dataStore.createObject(_minerData, "property", "Command Buffer Time");
      _tl = _dataStore.createObject(_minerData, "property", "Log Command Times");
      
      _ubt.setAttribute(DE.A_VALUE, "" + _dataStore.getCommandWaitTime());
      _cbt.setAttribute(DE.A_VALUE, "" + _dataStore.getUpdateWaitTime());

      if (_dataStore.logTimes())
      {
        _tl.setAttribute(DE.A_VALUE, "enabled");
      }
      else
      {
        _tl.setAttribute(DE.A_VALUE, "disabled");
      }
    }


   public void extendSchema(DataElement schemaRoot)
    {
      DataElement rootD = _dataStore.find(schemaRoot, DE.A_NAME, "host", 1);

      DataElement setD = createAbstractCommandDescriptor(rootD, "Change Settings", "C_CHANGE_SETTINGS");
      DataElement setUpdate = createCommandDescriptor(setD, "Set Update Buffer Time", "C_SET_UPDATE_WAIT_TIME");
      _dataStore.createObject(setUpdate, "input", "Enter New Wait Time");

      DataElement setCommand = createCommandDescriptor(setD, "Set Command Buffer Time", "C_SET_COMMAND_WAIT_TIME");
      _dataStore.createObject(setCommand, "input", "Enter New Wait Time");


      DataElement timeD = createAbstractCommandDescriptor(setD, "Time Logging", "C_LOG_TIME");
      DataElement etimeD = createCommandDescriptor(timeD, "Enable", "C_ENABLE_LOG_TIME");
      DataElement dtimeD = createCommandDescriptor(timeD, "Disable", "C_DISABLE_LOG_TIME");
    }
 
   public DataElement handleCommand(DataElement theCommand)
    {
     String name          = getCommandName(theCommand);
     DataElement  status  = getCommandStatus(theCommand);
     DataElement  subject = getCommandArgument(theCommand, 0);
     DataElement  value   = getCommandArgument(theCommand, 1);
 
     if (name.equals("C_SET_UPDATE_WAIT_TIME"))
     {
       return handleSetUpdateWaitTime(subject, value, status);	 
     }
     else if (name.equals("C_SET_COMMAND_WAIT_TIME"))
     {
       return handleSetCommandWaitTime(subject, value, status);	 
     }
     else if (name.equals("C_ENABLE_LOG_TIME"))
     {
       System.out.println("enable time log");
       _dataStore.setLogTimes(true);

       _tl.setAttribute(DE.A_VALUE, "enabled");
       _dataStore.update(_minerData);
     }
     else if (name.equals("C_DISABLE_LOG_TIME"))
     {
       System.out.println("disable time log");
       _dataStore.setLogTimes(false);

       _tl.setAttribute(DE.A_VALUE, "disabled");
       _dataStore.update(_minerData);
     }

     status.setAttribute(DE.A_NAME, "done");
     return status;
    }

    public DataElement handleSetUpdateWaitTime(DataElement subject, DataElement value, DataElement status)
    {      
      int waitTime = (new Integer(value.getName())).intValue();
      _dataStore.setUpdateWaitTime(waitTime);
      _ubt.setAttribute(DE.A_VALUE, "" + waitTime);
      _dataStore.update(_minerData);
      status.setAttribute(DE.A_NAME, "done");
      return status;      
    } 
 
    public DataElement handleSetCommandWaitTime(DataElement subject, DataElement value, DataElement status)
    {      
      int waitTime = (new Integer(value.getName())).intValue();
      _dataStore.setCommandWaitTime(waitTime);
      _cbt.setAttribute(DE.A_VALUE, "" + waitTime);
      _dataStore.update(_minerData);
      status.setAttribute(DE.A_NAME, "done");
      return status;      
    } 

    
  }

