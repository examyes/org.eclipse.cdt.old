/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/gdbCommands/GetGdbLocals.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:43:15)
/////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.gdbCommands;
import  com.ibm.debug.gdbPicl.*;
import  com.ibm.debug.gdbPicl.objects.*;
import  com.ibm.debug.gdbPicl.gdbCommands.GdbProcess;

import java.util.Hashtable;

/**
 * gets Gdb Threads
 */
public class GetGdbLocals
{
   String[] _locals = null;

   GdbDebugSession  _debugSession  = null;

  /**
   * Create a new GetGdbLocals command object
   */
   public GetGdbLocals(GdbDebugSession gdbDebugSession)
   {
     _debugSession = gdbDebugSession;
   }

   public String[] getLocals(int thrd)
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"******************* GetGdbLocals getLocals" );
      _locals = new String[0];
      String cmd;
      boolean ok;

      if(thrd != _debugSession.getCurrentThreadID() )
      {   if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"GetGdbLocals SETTING thread="+ thrd );
         cmd = "thread "+Integer.toString(thrd);
         ok = _debugSession.executeGdbCommand(cmd);
         if(!ok)
             return _locals;
 
      }

      cmd = "info locals ";
      ok = _debugSession.executeGdbCommand(cmd);
      if(!ok)
          return _locals;
 
      String[] lines = _debugSession.getTextResponseLines();
      if(lines.length<=0)
      {   if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(2,"GetGdbLocals.getLocals lines==null" );
          return _locals;
      }

      _locals = new String[lines.length];

      String local = "";
      int    lastLocal = 0;

      for(int i=0; i<lines.length; i++)
      { 
        String str = lines[i];
        if(str!=null && !str.equals("") && !str.startsWith("No symbol") )
        {   if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(2,"GetGdbLocals i="+i+" local: "+str );
            if(str.indexOf("=")>=0)
            {  _locals[i] = str;
               lastLocal = i;
            }
            else
               _locals[lastLocal] += str;
        }
      }
      if(thrd!= _debugSession.getCurrentThreadID() )
      {   if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"GetGdbLocals RE-setting thread=current="+_debugSession.getCurrentThreadID() );
         cmd = "thread "+Integer.toString(_debugSession.getCurrentThreadID());
         ok = _debugSession.executeGdbCommand(cmd);
         if(!ok)
            ;
      }
      return _locals;
   }
}
