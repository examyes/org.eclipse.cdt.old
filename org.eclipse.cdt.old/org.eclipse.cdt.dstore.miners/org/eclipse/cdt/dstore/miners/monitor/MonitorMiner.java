package org.eclipse.cdt.dstore.miners.monitor;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.miners.miner.*;
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
     status.setAttribute(DE.A_NAME, "done");
     return status;
    }
  }

