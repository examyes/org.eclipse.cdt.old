package com.ibm.dstore.miners.monitor;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.miners.miner.*;
import java.util.*;
import java.lang.*;
import java.io.*;


public class MonitorMiner extends Miner
  {
      private Monitor _monitor;

   public void extendSchema(DataElement schemaRoot)
    {
    }
 
   public void load()
    {
	_monitor = new Monitor();
	_monitor.setDataStore(_dataStore);
	_monitor.setWaitTime(10000);
	_monitor.start();
    }

      public void finish()
      {
	  _monitor.finish();
	  super.finish();
      }

   public DataElement handleCommand(DataElement theCommand)
    {
     String name          = getCommandName(theCommand);
     DataElement  status  = getCommandStatus(theCommand);
     DataElement  subject = getCommandArgument(theCommand, 0);
 
     status.setAttribute(DE.A_NAME, "done");
     return status;
    }
  }

