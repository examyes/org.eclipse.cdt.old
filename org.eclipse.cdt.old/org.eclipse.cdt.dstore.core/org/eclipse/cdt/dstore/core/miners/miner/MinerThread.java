package com.ibm.dstore.core.miners.miner;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

public abstract class MinerThread extends Thread
{
 private volatile Thread minerThread;
 
 public synchronized void stopThread() 
 {
  minerThread = null;
  notify();
 }
 
 public void run() 
 {
  Thread thisThread = Thread.currentThread();
  minerThread = thisThread;
  //thisThread.setPriority(thisThread.getPriority()+1);
  
  //This function lets derived classes do some initialization
  initializeThread();
    
  while (minerThread == thisThread) 
  {
   //try 
   //{ 
    //thisThread.sleep(0);
    yield();
   //}
   //catch (InterruptedException e) {}
   //This function is where the Threads do real work, and return false when finished
   if ( !doThreadedWork() )
    minerThread = null;
  }
  //This function lets derived classes cleanup or whatever
  cleanupThread();
 }
 
 public abstract void    initializeThread();
 public abstract boolean doThreadedWork();
 public abstract void    cleanupThread();
}



