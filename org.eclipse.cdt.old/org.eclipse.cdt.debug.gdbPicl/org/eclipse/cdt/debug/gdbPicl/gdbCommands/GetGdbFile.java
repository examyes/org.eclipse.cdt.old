/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/gdbCommands/GetGdbFile.java, gdb, java-dev
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
public class GetGdbFile
{
   GdbDebugSession  _debugSession  = null;

  /**
   * Create a new GetGdbFile command object
   */
   public GetGdbFile(GdbDebugSession gdbDebugSession)
   {
     _debugSession = gdbDebugSession;
   }

   public String convertSourceLineToAddress(String fileName, String line)
   {
      String startAddress = null;

      // find start address of the line
      String cmd = "info line "+fileName+":"+line;
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(3,"GetGdbFile.convertSourceLineToAddress cmd="+cmd );
      boolean ok = _debugSession.executeGdbCommand(cmd);
      if(!ok)
         return startAddress;
 
      String[] lines = _debugSession.getTextResponseLines();
      if(lines.length==0)
          return startAddress;
      String str = lines[0];
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"GetGdbFile.convertSourceLineToAddress start str="+str );
      String keyword = " at address 0x";
      int address = str.indexOf(keyword);
      if(address<0)
          return startAddress;
      str = str.substring(address+keyword.length()-2);  // include '0x' in address
      int end = str.indexOf(" ");
      startAddress = str.substring(0,end);

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"GetGdbFile.convertSourceLineToAddress line="+fileName+":"+line+" address="+startAddress );
      return startAddress;
   }

   public String convertAddressToSourceLine(String address)
   {
      String lineNum = null;

      // find start address of the line
      String cmd = "info line *"+address;
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(3,"GetGdbFile.convertAddressToSourceLine cmd="+cmd );
      boolean ok = _debugSession.executeGdbCommand(cmd);
      if(!ok)
         return lineNum;
 
      String[] lines = _debugSession.getTextResponseLines();
      if(lines.length==0)
          return lineNum;
      String str = lines[0];
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"GetGdbFile.convertAddressToSourceLine start str="+str );
      //Line nn of "FileName" starts at address 0xHHHHHH <fcnMane+nnnn> and ends at 0xHHHHHH <fcnName+nnnn>.
      String keyword = "Line ";
      int line = str.indexOf(keyword);
      if(line!=0)
          return lineNum;
      str = str.substring(keyword.length());
      int end = str.indexOf(" of \"");
      lineNum = str.substring(0,end);

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"GetGdbFile.convertAddressToSourceLine address="+address +" lineNum="+lineNum );
      return lineNum;
   }
/*
   public String convertAddressToDisassemblyLine(String targetAddress, String startAddress, String endAddress)
   {
      String lineNum = null;

      String[] disassembly =  getDisassemblyLines(startAddress, endAddress);
      if(disassembly==null || disassembly.length==0)
         return lineNum;

      for(int i=1; i<=disassembly.length; i++)
      {
         String str = disassembly[i-1];
         String keyword = "0x";
         int address = str.indexOf(keyword);
         if(address<0)
             return lineNum;
         str = str.substring(address);
         int end = str.indexOf(" ");
         String lineAddress = str.substring(0,end);
System.out.println("GetGdbFile.convertAddressToDisassemblyLine target="+targetAddress+" i="+i+" lineAddress="+lineAddress );
         if(targetAddress.equals(lineAddress))
            return String.valueOf(i);
      }
      return lineNum;
   }
*/
/*
   public String convertDisassemblyLineToAddress(String lineNum, String startAddress, String endAddress)
   {
      String lineAddress = null;
      int targetLine = Integer.parseInt(lineNum);

      String[] disassembly =  getDisassemblyLines(startAddress, endAddress);
      if(disassembly==null || disassembly.length<targetLine )
         return lineAddress;

      String str = disassembly[targetLine-1];
      String keyword = "0x";
      int address = str.indexOf(keyword);
      if(address<0)
          return lineAddress;
      str = str.substring(address);
      int end = str.indexOf(" ");
      lineAddress = str.substring(0,end);
System.out.println("GetGdbFile.convertDisassemblyLineToAddress lineNum="+lineNum+" lineAddress="+lineAddress );
      return lineAddress;
   }
*/
   public String[] getDisassemblyLines(String startAddress, String endAddress)
   {
      String[] disassembly = null;

      String cmd = "disassemble "+startAddress+" "+endAddress;
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(3,"GetGdbFile.getDissassemblyLines cmd="+cmd );

      boolean ok = _debugSession.executeGdbCommand(cmd);
      if(!ok)
         return disassembly;
 
      String[] lines = _debugSession.getTextResponseLines();
      if(lines.length==0)
          return disassembly;
      String str = lines[0];
      String lastStr = lines[lines.length-1];
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(3,"GetGdbFile.getDissassemblyLines lines[0]="+str );
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(3,"GetGdbFile.getDissassemblyLines lines[last]="+lastStr );
      String keyword = "Dump of assembler code from ";
      int end = 0;
      if(str!=null)
         end = str.indexOf(keyword);
      if(end<0)
      {
          if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(3,"GetGdbFile.getDissassemblyLines incorrect line[0]="+str );
          return disassembly;
      }
      keyword = "End of assembler dump.";
      end = 0;
      if(lastStr!=null)
         end = lastStr.indexOf(keyword);
      if(end<0)
      {
          if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(3,"GetGdbFile.getDissassemblyLines incorrect line[last]="+lastStr );
          return disassembly;
      }

      disassembly = new String[lines.length-2];
      for(int i=1; i<(lines.length-1); i++)
         disassembly[i-1] = lines[i];

      return disassembly;
   }

   public int getTotalLines(String fileName)
   {
      int totalLines = 0;
      if(fileName.indexOf(".h")>0 || fileName.indexOf(".H")>0)
          return totalLines;

      String cmd = "list "+fileName+":99999";
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(3,"GetGdbFile.getTotalLines cmd="+cmd );

      boolean ok = _debugSession.executeGdbCommand(cmd);
      if(!ok)
         return 0;
 
      String[] lines = _debugSession.getTextResponseLines();
      if(lines.length==0)
          return 0;
      String str = lines[0];
      String keyword = " lines.";
      int end = str.indexOf(keyword);
      if(end<0)
          return 0;
      str = str.substring(0,end);
      int strt = str.lastIndexOf(" ");
      str = str.substring(strt+1);
      if(str!=null && !str.equals("") )
      {   try { totalLines = Integer.parseInt(str); } 
          catch(java.lang.NumberFormatException exc){;}
      }

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"GetGdbFile.getTotalLines totalLines="+totalLines );

      return totalLines;
   }

   public class StartEnd 
   {  public String startAddress; 
      public String endAddress;
      StartEnd(String s, String e) 
      {  startAddress=s; 
         endAddress=e; 
      }
   }

   public StartEnd getStartEnd(String fileName, int totalLines)
   {
      StartEnd startEnd = null;
      String startAddress = null;
      String endAddress = null;

      // find start address of file
      String cmd = "info line "+fileName+":1";
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(3,"GetGdbFile.getStartEnd cmd="+cmd );
      boolean ok = _debugSession.executeGdbCommand(cmd);
      if(!ok)
         return startEnd;
 
      String[] lines = _debugSession.getTextResponseLines();
      if(lines.length==0)
          return startEnd;
      String str = lines[0];
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"GetGdbFile.getStartEnd start str="+str );
      //Line nn of "FileName" starts at address 0xHHHHHH <fcnMane+nnnn> and ends at 0xHHHHHH <fcnName+nnnn>.
      String keyword = " at address 0x";
      int address = str.indexOf(keyword);
      if(address<0)
          return startEnd;
      str = str.substring(address+keyword.length()-2);  // include '0x' in address
      int end = str.indexOf(" ");
      startAddress = str.substring(0,end);

      // find end address of file
      cmd = "info line "+fileName+":"+(totalLines-1);
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(3,"GetGdbFile.getTotalLines cmd="+cmd );
      ok = _debugSession.executeGdbCommand(cmd);
      if(!ok)
         return startEnd;
 
      lines = _debugSession.getTextResponseLines();
      if(lines.length==0)
          return startEnd;
      str = lines[0];
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"GetGdbFile.getStartEnd end str="+str );
      //Line nn of "FileName" starts at address 0xHHHHHH <fcnMane+nnnn> and ends at 0xHHHHHH <fcnName+nnnn>.
      keyword = " and ends at 0x";
      address = str.indexOf(keyword);
      if(address<0)
          return startEnd;
      str = str.substring(address+keyword.length()-2);  // include '0x' in address
      end = str.indexOf(" ");
      endAddress = str.substring(0,end);

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"GetGdbFile.getStartEnd start="+startAddress+" end="+endAddress );
      startEnd = new StartEnd(startAddress,endAddress);
      return startEnd;
   }
}
