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
public class GdbExceptions
{
   String[] _exceptionNames   = null;
   String[] _exceptionDescriptions = null;
   int[]    _exceptionStatus  = null;
   int      _maxExceptions    = 0;
   
   GdbDebugSession  _debugSession  = null;

  /**
   * Create a new GdbExceptions command object
   */
   public GdbExceptions(GdbDebugSession gdbDebugSession)
   {
     _debugSession = gdbDebugSession;
     queryExceptions();
   }

   public int getExceptionMax()
   {  return _maxExceptions; 
   }
   public String getShortName(int index)
   {  
      if(index <0 || index>=_maxExceptions) 
         return null;
      return _exceptionNames[index]; 
   }
   public String getFullName(int index)
   {  
      if(index <0 || index>=_maxExceptions) 
         return null;
      return _exceptionNames[index]+": "+_exceptionDescriptions[index]; 
   }
   public String[] getExceptionNames()
   {  String[] names = new String[_maxExceptions]; 
      for(int z=0; z<_maxExceptions; z++) 
         names[z] = getFullName(z);  
      return names; 
   }
   public int[] getExceptionStatus()
   {  int[] values = new int[_maxExceptions]; 
      for(int z=0; z<_maxExceptions; z++) 
         values[z] = _exceptionStatus[z];  
      return values; 
   }

   public void queryExceptions()
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"******************* GdbExceptions queryExceptions " );

      String cmd = "info signals ";
      boolean ok = _debugSession.executeGdbCommand(cmd);
      if(!ok)
          return;
 
      String[] lines = _debugSession.getTextResponseLines();
      if(lines.length<=0)
      {   if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(2,"GdbExceptions.queryExceptions lines==null" );
          return;
      }

      _exceptionNames  = new String[lines.length];
      _exceptionDescriptions  = new String[lines.length];
      _exceptionStatus = new int[lines.length];

      int    index = -1;
      String name         = "";
      String value        = "";

      for(int i=1; i<lines.length; i++)
      { 
        String str = lines[i];
        if(str!=null && !str.equals("") )
        {   
            int space = findSpaceTab( str );
            if(space<=0)
            {
               if (Gdb.traceLogger.ERR) 
                   Gdb.traceLogger.err(2,"GdbExceptions no SPACE in exception str: "+str );
               break;
            }
            name = str.substring(0,space);
            _exceptionNames[++index] = name;
            str = str.substring(space+1).trim();
            int process = 1;
            if(str.startsWith("No"))
               process = 0;
            _exceptionStatus[index] = process;

            space = findSpaceTab( str );
            if(space<0)
            {  if (Gdb.traceLogger.ERR) 
                   Gdb.traceLogger.err(2,"GdbExceptions no SPACE in exception str: "+str );
               break;
            }
            str = str.substring(space+1).trim();
            int print = 1;
            if(str.startsWith("No"))
               print = 0;

            space = findSpaceTab( str );
            if(space<0)
            {  if (Gdb.traceLogger.ERR) 
                   Gdb.traceLogger.err(2,"GdbExceptions no SPACE in exception str: "+str );
               break;
            }
            str = str.substring(space+1).trim();
            int pass = 1;
            if(str.startsWith("No"))
               pass = 0;

            space = findSpaceTab( str );
            if(space<0)
            {  if (Gdb.traceLogger.ERR) 
                   Gdb.traceLogger.err(2,"GdbExceptions no SPACE in exception str: "+str );
               break;
            }
            str = str.substring(space+1).trim();

            _exceptionDescriptions[index] = str;

            //if (Gdb.traceLogger.DBG) 
            //    Gdb.traceLogger.dbg(2,"GdbExceptions name="+name+" process="+process+" print="+print+" pass="+pass+" str="+str );
        }
        else  // empty string
           break;
      }

      _maxExceptions = index;
      return;
   }
   public int findSpaceTab(String s)
   {  int space = s.indexOf(" ");
      int tab = s.indexOf("\t");
      if(tab>=0 && (tab<space ||space<0) )
           space = tab;
      return space;
   }

   public void ignoreException(int index, String name)
   {
      if( validException(index,name) )
          handleException( index, name, "no");

      return;
   }
   public void catchException(int index, String name)
   {
      if( validException(index,name) )
         handleException( index, name, "yes");

      return;
   }
   public boolean validException(int index, String name)
   {
      if(index<0 || index>=_maxExceptions)
         return false;

      if(getFullName(index).equals(name))
         return true;
      else
         return false;
   }
   public void handleException(int index, String name, String handle)
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"GdbExceptions.handleException="+index+" "+handle+" "+name );

      String shortName = getShortName(index);
      String stop = " stop print ";
      if(handle.equals("no"))
         stop = " nostop noprint ";

      String cmd = "handle " +shortName +stop;
      boolean ok = _debugSession.executeGdbCommand(cmd);
      if(!ok)
          return;
 
      String[] lines = _debugSession.getTextResponseLines();
      if (Gdb.traceLogger.DBG)  if(lines.length>1)
          Gdb.traceLogger.dbg(1,"GdbExceptions.handleException RESPONSE="+lines[1] );

      return;
   }
   public void clearException()
   {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(2,"GdbExceptions.clearException (RESETS SIGNAl==0)");

      String cmd = "signal 0 ";
      boolean ok = _debugSession.executeGdbCommand(cmd);
      if(!ok)
          return;
 
      String[] lines = _debugSession.getTextResponseLines();
      if (Gdb.traceLogger.DBG) if(lines.length>0) 
          Gdb.traceLogger.dbg(1,"GdbExceptions.clearException RESPONSE="+lines[0] );

      return;
   }
}
