/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.gdbCommands;

import  org.eclipse.cdt.debug.gdbPicl.*;

/**
 * gets Gdb Threads
 */
public class GdbCommandAndResponse
{
   GdbDebugSession  _debugSession  = null;
   GdbProcess       _gdbProcess    = null;
   
   StringBuffer classObject = null;
   StringBuffer combinedLine = null;
   
   static String PRE_PROMPT_keyword      = GdbProcess.MARKER+"pre-prompt";
   static String FRAME_BEGIN_keyword     = GdbProcess.MARKER+"frame-begin";
   static String FRAME_END_keyword       = GdbProcess.MARKER+"frame-end";
   static String FRAME_INVALID_keyword   = GdbProcess.MARKER+"frames-invalid";
   static String BP_RECORD_keyword       = GdbProcess.MARKER+"record";
   static String BP_HEADERS_keyword      = GdbProcess.MARKER+"breakpoints-headers";
   static String BP_TABLE_END_keyword    = GdbProcess.MARKER+"breakpoints-table-end";
   static String DISPLAY_BEGIN_keyword   = GdbProcess.MARKER+"display-begin";
   static String DISPLAY_END_keyword     = GdbProcess.MARKER+"display-end";
   static String FIELD_BEGIN_keyword     = GdbProcess.MARKER+"field-begin";
   static String FIELD_NAME_END_keyword  = GdbProcess.MARKER+"field-name-end";
   static String FIELD_VALUE_keyword     = GdbProcess.MARKER+"field-value";
   static String FIELD_END_keyword       = GdbProcess.MARKER+"field-end";
   static String ARRAY_SECTION_BEGIN_keyword = GdbProcess.MARKER+"array-section-begin";
   static String ARRAY_ELEMENT_keyword   = GdbProcess.MARKER+"elt";
   static String ARRAY_SECTION_END_keyword = GdbProcess.MARKER+"array-section-end";



  /**
   * Create a new GdbCommandAndResponse. command object
   */
   public GdbCommandAndResponse(GdbDebugSession gdbDebugSession)
   {
     _debugSession  = gdbDebugSession;
     _gdbProcess    = _debugSession.getGdbProcess();
     classObject = new StringBuffer(_debugSession.MAX_GDB_LINES*80);
	 combinedLine = new StringBuffer(_debugSession.MAX_GDB_LINES*80);
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

        int length = lines.length;
        
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
           {

               if(lines[i]!=null && !lines[i].equals(""))
                  classObject.append(lines[i]);
               
               //  Could be a complex structure, class object, if yes put it all in one line   
               int j = i+1;
               if( (j < length) &&
                   (lines[j].startsWith(FIELD_BEGIN_keyword) || lines[j].startsWith(FIELD_VALUE_keyword) ||
                    lines[j].startsWith(FIELD_NAME_END_keyword) || lines[j].startsWith(FIELD_END_keyword) ||
                    lines[j].startsWith(ARRAY_SECTION_BEGIN_keyword))
                  )
               {
                  j++;
                  while ((j < length) && ((!lines[j].equals("}") || (lines[j].equals("}") && (lines[j+1].startsWith(FIELD_END_keyword) || lines[j+1].startsWith(ARRAY_ELEMENT_keyword)) ))))
                  {
                 	 classObject.append(lines[j]);
                 	 j++;
                 	 if( (j < length) &&
                 	     (lines[j].startsWith(FIELD_BEGIN_keyword) || lines[j].startsWith(FIELD_VALUE_keyword) ||
                          lines[j].startsWith(FIELD_NAME_END_keyword) || lines[j].startsWith(FIELD_END_keyword) ||
                          lines[j].startsWith(ARRAY_ELEMENT_keyword) || lines[j].startsWith(ARRAY_SECTION_BEGIN_keyword) ||
                          lines[j].startsWith(ARRAY_SECTION_END_keyword))
                        )
                     {
                        j++;
                     }     
                   }
                   // append the final "}"
                   if (j < lines.length)
                   {
						classObject.append(lines[j]);
                   }
                   i = j;
               }

               if (Gdb.traceLogger.DBG)
                   Gdb.traceLogger.dbg(3,". . . . . . . .  GdbDebugSession.getGdbResponseLines Object = "+ classObject.toString());
               _debugSession.cmdResponses.addElement(classObject.toString());              
               classObject.setLength(0);
           }
           else if ( lines[i].equals(PRE_PROMPT_keyword) )
           {
              break;
           }
           else if (lines[i].startsWith(FRAME_INVALID_keyword))
           {
				if (lines[i+1].startsWith("Stopped due to shared library event"))
           		{
		   		    _debugSession.cmdResponses.removeAllElements();
		   		    _debugSession.enableDeferredBreakpoints();
					boolean ok = _gdbProcess.writeLine("cont");
					if(ok)
       	            {
       	            	i = 0;
       	            	lines = _gdbProcess.readAllLines();
       	            	continue;
       	            }
           		}
           }
           else if( lines[i].startsWith(FRAME_BEGIN_keyword) || lines[i].startsWith(BP_HEADERS_keyword)
                ||  lines[i].startsWith(DISPLAY_BEGIN_keyword) )
           {
               //if(!lines[i].startsWith(FRAME_BEGIN_keyword) )
               //    combinedLine = "";
               //String combinedLine = "";
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
                          combinedLine.append(lines[j]);
                     }
                     else if( lines[j].startsWith(DISPLAY_BEGIN_keyword) )
                     {
                        if (Gdb.traceLogger.DBG)
                            Gdb.traceLogger.dbg(3,"<<<<<<<<<<<<<< GdbDebugSession.getGdbResponseLines j="+j+" BEGINNING MONITOR" );
                        String exprNumbr = "";
                        String exprName = "";
                        String exprValue = "";
                        exprNumbr = lines[++i];
                        if (Gdb.traceLogger.DBG)
                            Gdb.traceLogger.dbg(3,"<<<<<<<<<<<<<< GdbDebugSession.getGdbResponseLines j="+j+" exprNUMBR="+exprNumbr );
                        String str = lines[++i]; // display-number-end
                        str = lines[++i];        // ":"
                        str = lines[++i];        // display-format
                        str = lines[++i];        // ""
                        str = lines[++i];        // "display-expression"
                        exprName = lines[++i];   // exprName
                        if (Gdb.traceLogger.DBG)
                            Gdb.traceLogger.dbg(3,"<<<<<<<<<<<<<< GdbDebugSession.getGdbResponseLines j="+j+" exprNAME="+exprName );
                        str = lines[++i];        // "display-expression-end"
                        str = lines[++i];        // " = "
                        str = lines[++i];        // "display-expression"
                        exprValue = lines[++i];  // exprValue
//                        if (Gdb.traceLogger.DBG)
//                            Gdb.traceLogger.dbg(3,"<<<<<<<<<<<<<< GdbDebugSession.getGdbResponseLines j="+j+" exprVALUE="+exprValue );
                        String s = lines[++i];   // loop until "display-end"
                        while(!s.equals(DISPLAY_END_keyword) && i<length )
                        {
                            if(s!=null && !s.equals("") && !s.startsWith(_gdbProcess.MARKER) )
                            {
                                exprValue += s;
//                              	System.out.println("RW ===== GdbCommandAndResponse.java exprValue: " + exprValue);
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
//                        	System.out.println("RW ===== GdbCommandAndResponse.java combinedLine exit: " + combinedLine.toString());
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
