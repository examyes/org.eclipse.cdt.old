/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
package com.ibm.debug.gdbPicl;
import  com.ibm.debug.gdbPicl.objects.*;

import java.util.*;
import com.ibm.debug.epdc.*;

/**
 * Manages process threads and creates EPDC program state change packets
 */
public abstract class ThreadManager extends ComponentManager  //HC
{
   protected int    _stoppingThread = 0;
   public    int    getStoppingThread() { return _stoppingThread; }
   protected int    _currentThread = 0;

  /**
   * Create a new ThreadManager
   * @param session A reference to the DebugSession which created this 
   * ThreadManager
   * @see DebugSession
   */
   public ThreadManager(DebugSession debugSession) {
      super(debugSession);
      _threads           = new Vector();
      _threadNameIndex   = new Hashtable();
      _callStackThreads  = new Vector();
      _freedStackThreads = new Vector();
      _changed = false;
   }

  /**
   * Return the ThreadComponent corresponding to the given thread DU.
   * Returns null if there is no such thread active.
   */
   public ThreadComponent getThreadComponent(int DU)
   {
      ThreadComponent tc;

      if ( _threads==null || _threads.size()==0)
          return null;

      if ( DU > _threads.size() || _threads.elementAt(DU-1) == null)
          return null;

      tc =  (ThreadComponent )_threads.elementAt(DU-1);
      if (tc.isTerminated())
         return null;
      else
      {
         if (tc.isPartial())
         {
            // The caller requested a thread component but we don't have any
            // info for it yet.  Force an update before we return the handle.
            updateThread(DU);
         }
         return tc;
      }
   }

  /**
   * Returns the size of the call stack for the specified thread DU.
   */
   public int getCallStackSize(int DU)
   {
      ThreadComponent tc;

      tc = getThreadComponent(DU);
      if (tc == null || tc.isTerminated())
         return -1;
      else
         return tc.getCallStackSize();
   }

  /**
   * Return the ThreadComponent corresponding to the given thread name
   */
   ThreadComponent getThreadComponent(String threadName)
   {
      Integer duInt = (Integer) _threadNameIndex.get(threadName);

      if (duInt != null)
      {
         return getThreadComponent(duInt.intValue());
      }
      else
      {
         if (Gdb.traceLogger.DBG) 
             Gdb.traceLogger.dbg(3,"getThreadComponent() : no such thread " + threadName);
         return null;
      }
   }

  /**
   * Update a specific thread
   */
   public abstract void updateThread(int du);

  /**
   * Update list of threads to match threads known by the debugger.
   */
   abstract void updateThreads();    //HC
   
  /**
   * Returns the partid the given thread was last reported in.
   */
   int getPartIdForThread(String threadName)
   {
      if (_threadNameIndex.containsKey(threadName))
      {
         ThreadComponent tc = getThreadComponent(threadName);
         if (tc != null)
            return tc.partID();
         else
            return 0;
      }
      return 0;
   }

  /**
   * Adds thread/stack change packets for this component to a reply packet
   * @param rep The EPDC_Reply to which this ThreadManager will add change
   * change packets to
   * @see ibm.EPDC.EPDC_Reply
   */
   public void addChangesToReply(EPDC_Reply rep) 
   {

      if (_changed == false)
         return;

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(3,".....????????... ThreadManager.addChangesToReply _change="+_changed +" _threads.size()="+_threads.size() );

      _changed = false;

      // get stack change packets
      // NOTE: The C++ SUI doesn't like us to give stack change packets for
      // threads that have died so make sure we don't add any for dead threads
      for (int i=0; i<_callStackThreads.size(); i++) 
      {
         ThreadComponent tc = 
            (ThreadComponent) _callStackThreads.elementAt(i);

         if (tc.isPartial())
         {
            updateThread(tc.getDU());
         }

         if (!tc.isTerminated())
         {
            rep.addStackChangePacket(tc.getEPDCStack());
         }
      }

      // Add program state change packets
      for (int i=0; i<_threads.size(); i++) 
      {
         ThreadComponent tc = (ThreadComponent) _threads.elementAt(i);
         if (tc != null && tc.hasChanged())
         {
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(2,"................ ThreadManager.addChangesToReply CHANGED threadComponent DU="+tc.getDU() );
            rep.addThreadChangePacket(tc.getEPDCThread());
         }
      }

      for (int i=0; i<_freedStackThreads.size(); i++) 
      {
         ThreadComponent tc = 
            (ThreadComponent) _freedStackThreads.elementAt(i);
         if (!tc.isTerminated())
         {
            rep.addStackChangePacket(new ERepGetChangedStack(tc.getDU(), 
               EPDC.STACK_ENTRY_DELETE));
         }
      }
      _freedStackThreads.removeAllElements();

   }

  /**
   * Returns thread ID for this thread.
   * @return thread ID, 0 if the thread is not found
   */
   public int getThreadDU(String threadName)
   {
      if (threadName!= null)
      {
         if (_threadNameIndex.containsKey(threadName))
         {
            return ((Integer) _threadNameIndex.get(threadName)).intValue();
         }
      }
      return 0;
   }

  /**
   * Get the thread name for this thread ID.
   * @param DU the unique thread identification number
   * @return a string containing the thread name
   */
   public String getThreadName(int DU)
   {
       return ((ThreadComponent) _threads.elementAt(DU-1)).threadName();
   }

  /**
   * Clear list of threads
   */
   void clearThreads()
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"ThreadManager.clearThreads ");
      _threads.removeAllElements();
      _threadNameIndex.clear();
      _callStackThreads.removeAllElements();
      _freedStackThreads.removeAllElements();
   }

  /**
   * Monitor the indicated thread's call stack and send call stack change 
   * packets to SUI when necessary
   * @param DU the unique thread identification number
   */
   public void monitorCallStack(int DU)
   {
      _changed = true;
      _callStackThreads.addElement(getThreadComponent(DU));

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"-------->>>>>>>> ThreadManager.monitorCallStack thread="+DU);

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"ThreadManager.monitorCallStack _callStackThreads.size()="+_callStackThreads.size()+" #########" );

         ThreadComponent tc = (ThreadComponent) _callStackThreads.elementAt(_callStackThreads.size()-1);
         tc.updateFilesOnStack();
   }

  /**
   * Stop monitoring the idnicated thread's call stack
   * @param DU the unique thread identification number
   */
   public void freeCallStackMonitor(int DU)
   {
      _changed = true;
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"ThreadManager.freeCallStackMonitor ");
      ThreadComponent tc = (ThreadComponent) _threads.elementAt(DU-1);
      tc.freeCallStack();
      _freedStackThreads.addElement(_threads.elementAt(DU-1));
      _callStackThreads.removeElement(_threads.elementAt(DU-1));
   }

  /**
   * Freeze a thread (not implemented)
   * @param DU the unique thread identification number
   */
   public void freezeThread(int DU)
   {
      // NOTE: Due to problems with the sun.tools.debug API, the ThreadFreeze
      // and ThreadThaw requests have not been implemented.  These requests
      // are accepted by PICL but are ignored.  The FCT_THREAD_ENABLED bit
      // (see Gdb.java) should remain turned off until this functionality is
      // supported.
      ThreadComponent tc = (ThreadComponent) _threads.elementAt(DU-1);
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(3,"FreezeThread request for DU " +DU+" "+tc.threadName()); 
      tc.setThawed(false);
      _changed = true;
   }

  /**
   * Thaw a thread (not implemented)
   * @param DU the unique thread identification number
   */
   public void thawThread(int DU)
   {
      // NOTE: Due to problems with the sun.tools.debug API, the ThreadFreeze
      // and ThreadThaw requests have not been implemented.  These requests
      // are accepted by PICL but are ignored.  The FCT_THREAD_ENABLED bit
      // (see Gdb.java) should remain turned off until this functionality is
      // supported.
      ThreadComponent tc = (ThreadComponent) _threads.elementAt(DU-1);
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(3,"ThawThread request for DU " +DU+" "+tc.threadName()); 
      tc.setThawed(true);
      _changed = true;
   }

   // data fields

   private Vector       _registerThreads;     // Threads whose registers are being monitored 
   protected boolean    _changed;
   protected Vector     _threads;             // stores threads by thread ID
   public    Vector     getThreads() { return _threads; }
   protected Hashtable  _threadNameIndex;     // indexes names to thread ID
   private Vector       _callStackThreads;    // Threads whose call stack is being monitored 
   private Vector       _freedStackThreads;   // Threads whose call stack has just been freed
}
