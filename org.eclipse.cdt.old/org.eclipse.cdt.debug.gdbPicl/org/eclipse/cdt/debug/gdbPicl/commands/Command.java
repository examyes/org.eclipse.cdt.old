/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/Command.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:45:13)   (based on Jde 11/2/97 1.25)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;
import com.ibm.debug.connection.*;

/**
 * Abstract class that defines the methods that must be implemented by
 * all EPDC command processors
 */

public abstract class Command
{
  /**
   * Construct a new Command
   */
   public Command(DebugSession debugSession)
   {
      _debugSession = debugSession;
      _rep = null;
   }

  /**
   * Executes the EPDC command and stores the reply in _rep
   * @return true if the JDE should terminate after sending a reply
   */
   public abstract boolean execute(EPDC_EngineSession EPDCSession);

  /**
   * Adds change packets to reply packet
   */
   void addChangePackets()
   {
  //    _debugEngine.getClassManager().addChangesToReply(_rep);
  /* due to short comings of jdk1.1.8 monitor locks on class are
     sometime not released properly when class methods are chained
     together as above. The solution is to break the line into
     few lines of code as below - CMVC 14636
  */
      _debugSession.getModuleManager().addChangesToReply(_rep);
      _debugSession.getThreadManager().addChangesToReply(_rep);
      _debugSession.getBreakpointManager().addChangesToReply(_rep);
      _debugSession.getVariableMonitorManager().addChangesToReply(_rep);
      _debugSession.getRegisterManager().addChangesToReply(_rep);
      _debugSession.getStorageManager().addChangesToReply(_rep);
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(3,"Command.addChangePackets DONE" );
   }

  /**
   * Sends EPDC reply to an EPDC connection.
   */
   public void reply(Connection connection)
   {
      if (_rep != null)
      {
         try
         {
            addChangePackets();
            if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(2,"Command.reply about to output _rep="+_rep );
            _rep.output(connection);
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(2,"Command.reply output DONE" );
         }
         catch (Exception e)
         {
            Gdb.handleException(e);
         }
      }
   }

   // data members
   protected DebugSession _debugSession = null;
   protected EPDC_Reply  _rep;
   protected ERepVersion _versionRep;
}
