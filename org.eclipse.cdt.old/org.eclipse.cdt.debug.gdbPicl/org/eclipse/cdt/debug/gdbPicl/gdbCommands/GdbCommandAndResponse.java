/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/gdbCommands/GdbCommandAndResponse.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:43:15)
/////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.gdbCommands;

import  com.ibm.debug.gdbPicl.*;

/**
 * gets Gdb Threads
 */
public class GdbCommandAndResponse
{
   GdbDebugSession  _debugSession  = null;
   GdbProcess       _gdbProcess    = null;

  /**
   * Create a new GdbCommandAndResponse. command object
   */
   public GdbCommandAndResponse(GdbDebugSession gdbDebugSession)
   {
     _debugSession  = gdbDebugSession;
     _gdbProcess    = _debugSession.getGdbProcess();
   }


  public boolean executeGdbCommand(String cmd)
  {
     if(_gdbProcess==null)
     {   if (Gdb.traceLogger.ERR)
             Gdb.traceLogger.err(1,_debugSession.getResourceString("GDB_PROCESS_NULL") );
         _debugSession.uiMessages.addElement( _debugSession.getResourceString("GDB_PROCESS_NULL") );
         return false;
     }
     _debugSession.cmdResponses.removeAllElements();
     _debugSession.addLineToUiMessages(_debugSession.prefix+cmd);

     if( _gdbProcess.isReady() )
     {
         _debugSession.getGdbResponseLines();
         String[] lines = _debugSession.getTextResponseLines();
         if(lines!=null && lines.length>0)
         {   int length = lines.length;
             for(int i=0; i<length; i++)
             {
                if (Gdb.traceLogger.ERR)
                    Gdb.traceLogger.err(2,"GdbDebugSession.executeGdbCommand discarding pre-existing Gdb output="+ lines[i] );
             }
         }
     }


//     gdbProcess.setAnnotated(true);
     if (Gdb.traceLogger.EVT)
         Gdb.traceLogger.evt(3,"GdbDebugSession.executeGdbCommand cmd="+cmd );
     boolean ok = _gdbProcess.writeLine(cmd);
     if(!ok)
         _debugSession.uiMessages.addElement( _debugSession.getResourceString("GDBPICL_FAILED_TO_EXECUTE_COMMAND")+cmd );
     else
         _debugSession.getGdbResponseLines();
     return ok;
  }

  public void getGdbResponseLines()
  {
     _debugSession.cmdResponses.removeAllElements();
     String[] lines = _gdbProcess.readAllLines();

     if (Gdb.traceLogger.DBG)
         Gdb.traceLogger.dbg(3,"GdbDebugSession.getGdbResponseLines lines.length="+lines.length );

     String PRE_PROMPT_keyword   = _gdbProcess.MARKER+"pre-prompt";
     String FRAME_BEGIN_keyword  = _gdbProcess.MARKER+"frame-begin";
     String FRAME_END_keyword    = _gdbProcess.MARKER+"frame-end";
     String BP_RECORD_keyword    = _gdbProcess.MARKER+"record";
     String BP_HEADERS_keyword   = _gdbProcess.MARKER+"breakpoints-headers";
     String BP_TABLE_END_keyword = _gdbProcess.MARKER+"breakpoints-table-end";
     String DISPLAY_BEGIN_keyword= _gdbProcess.MARKER+"display-begin";
     String DISPLAY_END_keyword  = _gdbProcess.MARKER+"display-end";
     //String combinedLine = "";

        int length = lines.length;

       	//System.out.println("RW ===== GdbCommandAndResponse.java lines.length: " + length);

        if(length>_debugSession.MAX_GDB_LINES)
        {
           if (Gdb.traceLogger.ERR)
               Gdb.traceLogger.err(1, _debugSession.getResourceString("GDBPICL_COMMAND_PRODUCED_TOO_MANY_RESPONSE_LINES")+length+">"+_debugSession.MAX_GDB_LINES );
        }

     for(int i=0; i<length; i++)
     {  if(lines[i]!=null && !lines[i].equals("") )
        {
           if (Gdb.traceLogger.DBG)
               Gdb.traceLogger.dbg(3,". . . . . . . .  GdbDebugSession.getGdbResponseLines i="+i+" STR="+lines[i] );
           if( !lines[i].startsWith(_gdbProcess.MARKER) )
           {  _debugSession.cmdResponses.addElement(lines[i]);
           }
           else if ( lines[i].equals(PRE_PROMPT_keyword) )
           {
              break;
           }
           else if( lines[i].startsWith(FRAME_BEGIN_keyword) || lines[i].startsWith(BP_HEADERS_keyword)
                ||  lines[i].startsWith(DISPLAY_BEGIN_keyword) )
           {
               //if(!lines[i].startsWith(FRAME_BEGIN_keyword) )
               //    combinedLine = "";
               //String combinedLine = "";
               StringBuffer combinedLine = new StringBuffer(length);
               String frameFunctionName = "";
               String frameFile = "";
               boolean displayMonitor = false;
               for(int j=i; j<length; j++)
               {  if(lines[j]!=null && !lines[j].equals(""))
                  {
                     if (Gdb.traceLogger.DBG)
                         Gdb.traceLogger.dbg(3,". . . . . . . .  GdbDebugSession.getGdbResponseLines j="+j+" STR="+lines[j] );
                     if( !lines[j].startsWith(_gdbProcess.MARKER) )
                     {
                          //combinedLine += lines[j];
                          combinedLine.append(lines[j]);
                          	//System.out.println("RW ===== GdbCommandAndResponse.java combinedLine 1: " + combinedLine.toString());
                     }
                     else if( lines[j].startsWith(DISPLAY_BEGIN_keyword) )
                     {
//                        if (Gdb.traceLogger.DBG)
//                            Gdb.traceLogger.dbg(3,"<<<<<<<<<<<<<< GdbDebugSession.getGdbResponseLines j="+j+" BEGINNING MONITOR" );
                        String exprNumbr = "";
                        String exprName = "";
                        String exprValue = "";
                        exprNumbr = lines[++i];
//                        if (Gdb.traceLogger.DBG)
//                            Gdb.traceLogger.dbg(3,"<<<<<<<<<<<<<< GdbDebugSession.getGdbResponseLines j="+j+" exprNUMBR="+exprNumbr );
                        String str = lines[++i]; // display-number-end
                        str = lines[++i];        // ":"
                          	System.out.println("RW ===== GdbCommandAndResponse.java str: " + str);
                        str = lines[++i];        // display-format
                          	System.out.println("RW ===== GdbCommandAndResponse.java str: " + str);
                        str = lines[++i];        // ""
                          	System.out.println("RW ===== GdbCommandAndResponse.java str: " + str);
                        str = lines[++i];        // "display-expression"
                          	System.out.println("RW ===== GdbCommandAndResponse.java str: " + str);
                        exprName = lines[++i];   // exprName
//                        if (Gdb.traceLogger.DBG)
//                            Gdb.traceLogger.dbg(3,"<<<<<<<<<<<<<< GdbDebugSession.getGdbResponseLines j="+j+" exprNAME="+exprName );
                        str = lines[++i];        // "display-expression-end"
                          	System.out.println("RW ===== GdbCommandAndResponse.java str: " + str);
                        str = lines[++i];        // " = "
                          	System.out.println("RW ===== GdbCommandAndResponse.java str: " + str);
                        str = lines[++i];        // "display-expression"
                          	System.out.println("RW ===== GdbCommandAndResponse.java str: " + str);
                        exprValue = lines[++i];  // exprValue
//                        if (Gdb.traceLogger.DBG)
//                            Gdb.traceLogger.dbg(3,"<<<<<<<<<<<<<< GdbDebugSession.getGdbResponseLines j="+j+" exprVALUE="+exprValue );
                        String s = lines[++i];   // loop until "display-end"
                        while(!s.equals(DISPLAY_END_keyword) && i<length )
                        {
                            if(s!=null && !s.equals("") && !s.startsWith(_gdbProcess.MARKER) )
                            {
                                exprValue += s;
                              	System.out.println("RW ===== GdbCommandAndResponse.java exprValue: " + exprValue);
                            }
                            if(i<(length-1))
                               s = lines[++i];
                            else
                               break;
                        }
                        if (Gdb.traceLogger.DBG)
                            Gdb.traceLogger.dbg(2,"GdbDebugSession.getGdbResponseLines MONITOR exprNumbr="+exprNumbr+" exprName="+exprName+" exprValue="+exprValue );
                        _debugSession.monitorChangedID.addElement(exprNumbr);
                        _debugSession.monitorChangedName.addElement(exprName);
                        _debugSession.monitorChangedValue.addElement(exprValue);
                     }
                     else if( lines[j].startsWith(BP_RECORD_keyword) )
                     {
                         _debugSession.cmdResponses.addElement(combinedLine.toString());
                         if (Gdb.traceLogger.DBG)
                             Gdb.traceLogger.dbg(2,"GdbDebugSession.getGdbResponseLines="+combinedLine.toString() );
                         //combinedLine = "";
                         combinedLine.setLength(0);
                         i = j+1;
                     }
//                     else if( lines[j].startsWith(FRAME_END_keyword) )
//                     {
//                         //combinedLine += lines[j+1];
//                         //i = j+1;
//                     }
                     else if( lines[j].startsWith(FRAME_END_keyword) || lines[j].startsWith(BP_TABLE_END_keyword)
                          ||  lines[j].startsWith(DISPLAY_END_keyword) )
                     {
                        	//System.out.println("RW ===== GdbCommandAndResponse.java combinedLine exit: " + combinedLine.toString());
                         _debugSession.cmdResponses.addElement(combinedLine.toString());
                         if (Gdb.traceLogger.DBG)
                             Gdb.traceLogger.dbg(2,"GdbDebugSession.getGdbResponseLines="+combinedLine.toString() );
                         combinedLine.setLength(0);
                         i = j;//+1;
                         break;
                     }
                 }
              }
           }
        }
     }
     if (Gdb.traceLogger.DBG)
         Gdb.traceLogger.dbg(2,"GdbDebugSession.getGdbResponseLines cmdResponses.size="+_debugSession.cmdResponses.size() );

  }


}
