/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/GdbModuleManager.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:41:53)   (based on Jde 5/1/98 1.43.1.6)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl;
import  com.ibm.debug.gdbPicl.objects.*;

import java.util.*;
import java.io.*;
import com.ibm.debug.epdc.*;

/**
 * Manages the module entry table and the part table
 */
public class GdbModuleManager extends ModuleManager
{
   public GdbModuleManager(GdbDebugSession debugSession)
   {
      super(debugSession);
   }



   /** Update module and parts tables to contain to the known parts */
   void updateParts() 
   {
      if (Gdb.traceLogger.ERR) 
          Gdb.traceLogger.err(1,"######## SHOULD THIS BE UNUSED?? GdbModuleManager.updateParts ????????????????????????????????????");

      String cmd="info files ";
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"================ GdbModuleManager.updateParts cmd=" +cmd );

      GdbDebugSession debugSession = (GdbDebugSession)_debugEngine.getDebugSession();
      boolean ok = debugSession.executeGdbCommand(cmd);
      if(!ok)
          return;
 
      String[] lines = debugSession.getTextResponseLines();
      String keyword = "Symbols from \"";
      int strt = lines[0].indexOf(keyword);
      if(lines.length==0 || strt<0)
      {
          String str = "";  if(lines.length>0) str = lines[0];
          if(Gdb.traceLogger.ERR)
             Gdb.traceLogger.err(2,"GdbModuleManager.updateParts cmd='"+cmd+"' failed, return message="+str  );
          return;
      }

      String fullName = lines[0].substring(strt+keyword.length(), lines[0].length()-2);
      if (Gdb.traceLogger.DBG) 
      {   Gdb.traceLogger.dbg(1,"<<<<<<<<======== GdbModuleManager.updateParts fullName=" +fullName );
         for(int i=0; i<lines.length; i++)
         {
             if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(1,"<<<<<<<<======== GdbModuleManager.updateParts lines=" +lines[i] );
         }
     }

  }


}
