/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl;
import com.ibm.debug.gdbPicl.commands.*;

import java.io.*;
import java.util.*;
import com.ibm.debug.connection.*;
import com.ibm.debug.epdc.*;
import com.ibm.debug.util.*;

/**
 * CommandProcessor main class.
 */

// Some notes about this class:
//
// It is imperative that DebugSession's cleanup routine is called before
// we exit the debugger.  Unfortunately, the debuggee must be stopped in order
// for the cleanup to work (running java_g.exe processes will not stop even
// if we tell the remote debugger class to close).  With this in mind
// we need to make sure we are able to cleanup in all of the following scenarios
//
//   1. The debuggee is stopped and the user closes the debugger via close menu
//   2. The debuggee is stopped and the front end process dies/is killed
//   3. The debuggee is running and the user closes the debugge via close menu
//   4. The debuggee is running and the front end process dies/is killed
//
// If the debuggee is stopped, the cleanup routine works fine.  So in the
// cases where the debuggee is running, we simply have to make sure we
// issue a halt request (via interrupt on this thread) before we call
// cleanup.  This will force the previous execute cmd to wakeup and reply
// to the front end.

class CommandProcessor extends Thread
{
  /**
   * Construct a new CommandProcessor
   */

   CommandProcessor(Connection connection, EPDC_EngineSession engineSession, DebugSession debugSession)
   {
      _connection    = connection;
      _engineSession = engineSession;
      _debugSession  = debugSession;
      _semaphore     = new Semaphore();
      _exit          = false;
      _commandQueue  = new Vector();
   }

   public void run()
   {
      Command cmd;
      try
      {
         while (!_exit)
         {
            // An interrupt here means we lost
            _semaphore.countedWaitInterruptable();
            synchronized (_commandQueue)
            {
               cmd = (Command) _commandQueue.firstElement();
               _commandQueue.removeElementAt(0);
            }

            if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(1,"Executing command cmd="+cmd);

            // We will wake up here if we completed the execute OR we were halted
            cmd.execute(_engineSession);

            if (!_exit)
            {
               if (Gdb.traceLogger.DBG) 
                   Gdb.traceLogger.dbg(1,"Sending EPDC reply cmd="+cmd );

               cmd.reply(_connection);
               if (Gdb.traceLogger.DBG) 
                   Gdb.traceLogger.dbg(2,"CommandProcessor.run reply DONE, about to OutputStream.flush" );

               _connection.flush();
            }
            if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(2,"CommandProcessor.run cmd.reply DONE" );

            if (cmd instanceof CmdTerminateDE)
            {
               if (Gdb.traceLogger.EVT) 
                   Gdb.traceLogger.evt(1,"################ CommandProcessor.run cmdTerminateDE !!!" );
               _exit = true;
            }
         }
         if (Gdb.traceLogger.EVT) 
             Gdb.traceLogger.evt(1,"CommandProcessor.run DONE (end of debugging)" );
      }
      catch (InterruptedException e)
      {
      }
//      catch (EOFException e)
//      {
//      }
      catch (IOException e)
      {
      }

      // At this point, we are sure the debuggee is stopped

      // This is our cleanup routine.
      _debugSession.closeDebugger();

      try
      {
         if (_connection != null)
            _connection.close();
      }
      catch (EOFException e)
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"Warning: had trouble trying to close connection");
      }
      catch (IOException e)
      {
         if (Gdb.traceLogger.ERR) 
            Gdb.traceLogger.err(3,"Warning: had trouble trying to close conenction");
      }
   }

   void connectionLost()
   {
      _exit = true;
      // This will either act as a halt in the case where the execute is not done
      // or interrupt the wait on the semaphore.  In either case the debuggee
      // will be stopped and command processor's loop will exit and the cleanup
      // called.
      interrupt();
   }

   public void execute(Command cmd)
   {
      if (cmd instanceof CmdRemoteHalt)
      {
         // Only issue a halt if the execute command is executing.  We don't
         // want to halt the wait on the semaphore!
         if (_debugSession.isWaiting())
         {
            interrupt();
         }
      }
      else
      {
         // We have to protect access to this queue since debug engine is on another thread
         synchronized (_commandQueue)
         {
            _commandQueue.addElement(cmd);
         }
         // Tell the command processor thread there's something to read off the queue
         _semaphore.countedNotify();
      }
   }

   private Connection         _connection;
   private EPDC_EngineSession _engineSession;
   private DebugSession       _debugSession;
   private Semaphore          _semaphore;
   private Command            _cmd;
   private boolean            _exit;
   private Vector             _commandQueue;
}
