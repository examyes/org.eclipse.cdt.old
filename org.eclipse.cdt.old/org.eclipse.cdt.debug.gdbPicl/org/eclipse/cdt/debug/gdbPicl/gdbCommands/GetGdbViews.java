/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.gdbCommands;
import  com.ibm.debug.gdbPicl.*;
import  com.ibm.debug.gdbPicl.objects.*;
import  com.ibm.debug.gdbPicl.gdbCommands.GdbProcess;

import java.util.Hashtable;

/**
 * gets Gdb Threads
 */
public class GetGdbViews  //extends ThreadManager
{
   GdbDebugSession  _debugSession  = null;

  /**
   * Create a new GetGdbViews command object
   */
   public GetGdbViews(GdbDebugSession gdbDebugSession)
   {
     _debugSession = gdbDebugSession;
   }

   public boolean lineBreakpoint(String fileName, String lineNumber)
   {  
       String cmd = "break";
       return breakpointCommand(cmd, fileName+":", lineNumber);
   }
   public boolean methodBreakpoint(String fileName, String methodName)
   {  
       String cmd = "break ";
       return breakpointCommand(cmd, fileName, methodName);
   }
   public boolean clearBreakpoint(String fileName, String lineNumber)
   {  
       String cmd = "clear";
       return breakpointCommand(cmd, fileName+":", lineNumber);
   }
   public boolean watchBreakpoint(String expression)
   {  
       String cmd = "watch ";
       return breakpointCommand(cmd, " ", expression);
   }

   
   public boolean breakpointCommand(String command, String fileName, String location)
   {  
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"GetGdbViews.breakpointCommand command="+command+" fileName="+fileName +" location="+location  );
   
      String cmd = command+" "+fileName+location;
     
      if( !_debugSession.executeGdbCommand(cmd) )
      {   if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(2,"Gdb Debug engine failed to execute the breakpoint command: "+cmd );
          return false;
      }
      _debugSession.addChangesToUiMessages();

      String[] lines = _debugSession.getTextResponseLines();
      if(lines.length<=0)
      {   if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(2,"GetGdbViews.breakpointCommand cmd="+cmd+" responseLines==null" );
          return false;
      }

      String str = lines[0];
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"GetGdbViews lines[0]="+str );
      if(str==null || str.equals("") )
      {   if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(2,"GetGdbViews.breakpointCommand cmd="+cmd+" responseLines[0]==null" );
          return false;
      }

      if( str.startsWith("No ") || str.endsWith("not defined.") )
      {   if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(2,"GetGdbViews.breakpointCommand cmd="+cmd+" responseLines="+str );
          return false;
      }

      return true;
   }

}
