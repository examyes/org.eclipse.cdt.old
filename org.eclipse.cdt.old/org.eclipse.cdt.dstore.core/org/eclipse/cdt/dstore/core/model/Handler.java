package com.ibm.dstore.core.model;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
//import com.ibm.cpp.core.util.*;
//import com.ibm.cpp.core.extra.*;

import java.util.*;
import java.lang.*;

public abstract class Handler extends Thread
{
  protected   int                   _waitIncrement;
  protected   DataStore             _dataStore;
  private     boolean               _keepRunning;

  public Handler()
  {
    _keepRunning = true;
    _waitIncrement = 100;
    setDaemon(true);
  }

  public void setWaitTime(int value)
      {
        _waitIncrement = value;
      }

  public int getWaitTime()
      {
        return _waitIncrement;
      }

  public void setDataStore(DataStore dataStore)
  {
    _dataStore = dataStore;
  }

    public boolean isFinished()
    {
	return !_keepRunning;
    }  

  public void finish()
  {
      if (_keepRunning)
	  {
	      
	      _waitIncrement = 0;
	      _keepRunning = false;

	      /* causes hang
	      try
		  {
		      interrupt();
		      join();
		  }
	      catch (InterruptedException e)
		  {
		      System.out.println(e);
		  }
	      */
	      handle();
	  }
  }

  public abstract void handle();

  public void run()
  {
    while (_keepRunning)
       {
	try
          {
	      Thread.currentThread().sleep(_waitIncrement);
	      Thread.currentThread().yield();
          }	
	catch (InterruptedException e)
          {
	      e.printStackTrace();
	      finish();
	      return;
          }
	
	handle();
      }
  }
}
