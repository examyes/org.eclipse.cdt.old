package com.ibm.debug.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/util/Semaphore.java, java-util, eclipse-dev, 20011128
// Version 1.10.1.2 (last modified 11/28/01 16:32:43)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public class Semaphore
{
  public Semaphore()
  {
  }

  public Semaphore(int initialNumberOfUnusedNotifications)
  {
    _numberOfUnusedNotifications = initialNumberOfUnusedNotifications;
  }

  synchronized public void countedWait()
  {
    if (_numberOfUnusedNotifications == 0) // Have to wait:
    {
       ++_numberOfWaitingThreads;
       try
       {
         wait();
       }
       catch(InterruptedException excp)
       {
         --_numberOfWaitingThreads;
       }
    }
    else
      --_numberOfUnusedNotifications; // Don't wait - use one of the unused
  }                                  // notifications instead

  synchronized public void countedWaitInterruptable() throws InterruptedException
  {
    if (_numberOfUnusedNotifications == 0) // Have to wait:
    {
       ++_numberOfWaitingThreads;
       try
       {
         wait();
       }
       catch(InterruptedException excp)
       {
         --_numberOfWaitingThreads;
         throw new InterruptedException(excp.getMessage());
       }
    }
    else
      --_numberOfUnusedNotifications; // Don't wait - use one of the unused
  }                                  // notifications instead

  synchronized public boolean countedWait(int timeout)
  {
    boolean timedout = false;

    if (_numberOfUnusedNotifications == 0) // Have to wait:
    {
       ++_numberOfWaitingThreads;
       try
       {
          int prevNumberOfWaitingThreads = _numberOfWaitingThreads;
          wait(timeout);
          if (prevNumberOfWaitingThreads == _numberOfWaitingThreads)
          {
             timedout = true;
          }
       }
       catch(InterruptedException excp)
       {
         // We timed out
         --_numberOfWaitingThreads;
       }
    }
    else
      --_numberOfUnusedNotifications; // Don't wait - use one of the unused
                                     // notifications instead

    return timedout;
  }

  synchronized public void countedNotify()
  {
    if (_numberOfWaitingThreads > 0)
    {
       --_numberOfWaitingThreads;

       notify();
    }
    else
      ++_numberOfUnusedNotifications;
  }

  synchronized public void resetNotifications()
  {
    _numberOfUnusedNotifications = 0;
  }

  private int _numberOfWaitingThreads;
  private int _numberOfUnusedNotifications;
}
