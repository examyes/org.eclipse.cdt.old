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
public class GetGdbRegisters  //extends ThreadManager
{
   String[] _generalNames   = null;
   String[] _generalValues  = null;
   int      _maxGeneral     = 0;
   String[] _floatNames     = null;
   String[] _floatValues    = null;
   int      _maxFloat       = 0;
   
   GdbDebugSession  _debugSession  = null;

  /**
   * Create a new GetGdbRegisters command object
   */
   public GetGdbRegisters(GdbDebugSession gdbDebugSession)
   {
     _debugSession = gdbDebugSession;
   }

   public String[] getGeneralNames()
   {  if(_maxGeneral<=0)
         return null;
      String[] names = new String[_maxGeneral]; 
      for(int z=0; z<_maxGeneral; z++) 
         names[z] = _generalNames[z];  
      return names; 
   }
   public String[] getGeneralValues()
   {  if(_maxGeneral<=0)
         return null;
      String[] values = new String[_maxGeneral]; 
      for(int z=0; z<_maxGeneral; z++) 
         values[z] = _generalValues[z];  
      return values; 
   }
   public String[] getFloatNames()
   {  if(_maxFloat<=0)
         return null;
      String[] names = new String[_maxFloat]; 
      for(int z=0; z<_maxFloat; z++) 
         names[z] = _floatNames[z];  
      return names; 
   }
   public String[] getFloatValues()
   {  if(_maxFloat<=0)
        return null;
      String[] values = new String[_maxFloat]; 
      for(int z=0; z<_maxFloat; z++) 
         values[z] = _floatValues[z];  
      return values; 
   }

   public void updateRegisters(int thrd)
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"******************* GetGdbRegisters getRegisters" );
      String cmd;
      boolean ok;

      if(thrd != _debugSession.getCurrentThreadID() )
      {   if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"GetGdbRegisters SETTING thread="+ thrd );
         cmd = "thread "+Integer.toString(thrd);
         ok = _debugSession.executeGdbCommand(cmd);
         if(!ok)
             return;
 
      }

      cmd = "info all-registers ";
      ok = _debugSession.executeGdbCommand(cmd);
      if(!ok)
          return;
 
      String[] lines = _debugSession.getTextResponseLines();
      if(lines.length<=0)
      {   if (Gdb.traceLogger.ERR)
              Gdb.traceLogger.err(2,"GetGdbRegisters.getRegisters lines==null" );
          return;
      }

      _generalNames  = new String[lines.length];
      _generalValues = new String[lines.length];
      _floatNames    = new String[lines.length];
      _floatValues   = new String[lines.length];

      int    generalIndex = -1;
      int    floatIndex   = -1;
      String name         = "";
      String value        = "";

      for(int i=0; i<lines.length; i++)
      { 
        String str = lines[i];
        if(str!=null && !str.equals("") )
        {   if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(2,"GetGdbRegisters i="+i+" register str: "+str );
            int space = str.indexOf(" ");
            if(space<=0)
            {
               if (Gdb.traceLogger.ERR) 
                   Gdb.traceLogger.err(2,"GetGdbRegisters no SPACE in register str: "+str );
               continue;
            }
            name = str.substring(0,space);
            value = str.substring(space+1).trim();
            if(value.indexOf("(raw ")>=0)
            {
               _floatNames[++floatIndex] = name;
               _floatValues[floatIndex] = value;
            }else
            {
               _generalNames[++generalIndex] = name;
               _generalValues[generalIndex] = value;
            }
        }
      }
      if(thrd!= _debugSession.getCurrentThreadID() )
      {   if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"GetGdbRegisters RE-setting thread=current="+_debugSession.getCurrentThreadID() );
         cmd = "thread "+Integer.toString(_debugSession.getCurrentThreadID());
         ok = _debugSession.executeGdbCommand(cmd);
         if(!ok)
            ;
      }
      _maxGeneral = generalIndex+1;
      _maxFloat =  floatIndex+1;
      return;
   }
}
