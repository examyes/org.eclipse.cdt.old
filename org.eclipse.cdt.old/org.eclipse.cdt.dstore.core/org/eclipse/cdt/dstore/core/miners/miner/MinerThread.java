package org.eclipse.cdt.dstore.core.miners.miner;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

public abstract class MinerThread extends Thread
{
    private volatile Thread minerThread;
    protected boolean _isCancelled;

    public MinerThread()
    {
	super();
	_isCancelled = false;
    }

    public synchronized void stopThread() 
    {
	if (minerThread != null)
	    {
		_isCancelled = true;

		try
		    {
			minerThread = null;
		    }
		catch (Exception e)
		    {
			System.out.println(e);
		    }
		
	    }
	notify();
    }
 
 public void run() 
 {
  Thread thisThread = Thread.currentThread();
  minerThread = thisThread;
  //thisThread.setPriority(thisThread.getPriority()+1);
  
  //This function lets derived classes do some initialization
  initializeThread();
    
  while (minerThread != null &&
	 minerThread == thisThread && 
	 minerThread.isAlive() && 
	 !_isCancelled)
  {
      try 
	  { 
	      thisThread.sleep(1);
	      // yield();
	  }
      catch (InterruptedException e) 
	  {
	      System.out.println(e);
	  }
      
      //This function is where the Threads do real work, and return false when finished
      if ( !doThreadedWork() )
	  {
		try
		    {
			minerThread = null;
		    }
		catch (Exception e)
		    {
			System.out.println(e);
		    }		
	  }
  }

  //This function lets derived classes cleanup or whatever
  cleanupThread();
 }
 
 public abstract void    initializeThread();
 public abstract boolean doThreadedWork();
 public abstract void    cleanupThread();
}



