/*
 * Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.gdbCommands;
import  org.eclipse.cdt.debug.gdbPicl.*;
import  org.eclipse.cdt.debug.gdbPicl.objects.*;
import  org.eclipse.cdt.debug.gdbPicl.gdbCommands.GdbProcess;

import java.util.Hashtable;

/**
 * gets Gdb Threads
 */
public class GetGdbBreakpoints  //extends ThreadManager
{
   GdbDebugSession  _debugSession  = null;

  /**
   * Create a new GetGdbBreakpoints command object
   */
   public GetGdbBreakpoints(GdbDebugSession gdbDebugSession)
   {
     _debugSession = gdbDebugSession;
   }

   public int lineBreakpoint(String fileName, String lineNumber)
   {  
       String cmd = "break";
       return breakpointCommand(cmd, fileName+":", lineNumber);
   }
   public int methodBreakpoint(String fileName, String methodName)
   {  
       String cmd = "break ";
       return breakpointCommand(cmd, fileName, methodName);
   }
   public int clearBreakpoint(String fileName, String lineNumber)
   {  
       String cmd = "clear";
       return breakpointCommand(cmd, fileName+":", lineNumber);
   }
   public int watchBreakpoint(String expression)
   {  
       String cmd = "watch ";
       return breakpointCommand(cmd, " ", expression);
   }
   
   public int addressBreakpoint(String address)
   {
   	   String cmd = "break *";
      return breakpointCommand(cmd, " ", address);
   }
   
   public int deleteBreakpoint(int bkpID)
   {	
   		String cmd = "delete ";
  		return breakpointCommand(cmd, " ", String.valueOf(bkpID));
   }

   
   public int breakpointCommand(String command, String fileName, String location)
   {  
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"GetGdbBreakpoints.breakpointCommand command="+command+" fileName="+fileName +" location="+location  );
      int returnValue = -1;   
      String cmd = command+" "+fileName+location;
     
      if( !_debugSession.executeGdbCommand(cmd) )
      {   if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(2,"Gdb Debug engine failed to execute the breakpoint command: "+cmd );
          return returnValue;
      }
      _debugSession.addChangesToUiMessages();

      String[] lines = _debugSession.getTextResponseLines();
      if(lines.length<=0)
      {   if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(2,"GetGdbBreakpoints.breakpointCommand cmd="+cmd+" responseLines==null" );
          return returnValue;
      }

      String str = lines[0];
      
	  if (str.startsWith("Note:") && lines.length > 1)
      {
      	str = lines[1];
      }
      
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"GetGdbBreakpoints lines[0]="+str );                    
          
      if(str==null || str.equals("") )
      {   if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(2,"GetGdbBreakpoints.breakpointCommand cmd="+cmd+" responseLines[0]==null" );
          return returnValue;
      }

      if( str.startsWith("No ") || str.endsWith("not defined.") )
      {   if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(2,"GetGdbBreakpoints.breakpointCommand cmd="+cmd+" responseLines="+str );
          return returnValue;
      }
      
      String bkp = "Breakpoint ";
      if (str.startsWith(bkp))
      {
       	 int at = str.indexOf(" at ");
         String bkpID = str.substring(bkp.length(), at);  // breakpoint id
         returnValue = Integer.parseInt(bkpID);
      }

      return returnValue;
   }

}
