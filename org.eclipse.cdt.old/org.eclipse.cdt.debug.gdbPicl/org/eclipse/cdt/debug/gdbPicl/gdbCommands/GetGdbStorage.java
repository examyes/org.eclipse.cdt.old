/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/gdbCommands/GetGdbStorage.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:43:16)
/////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.gdbCommands;
import  com.ibm.debug.gdbPicl.*;
import  com.ibm.debug.gdbPicl.objects.*;
import  com.ibm.debug.gdbPicl.gdbCommands.GdbProcess;

import java.util.Hashtable;

/**
 * gets Gdb Threads
 */
public class GetGdbStorage
{
   String[] _storageContents     = null;
   String[] _storageAddresses = null;
   int      _maxLines         = 0;
   public String lastEvaluationError = null;
   
   GdbDebugSession  _debugSession  = null;

  /**
   * Create a new GetGdbStorage command object
   */
   public GetGdbStorage(GdbDebugSession gdbDebugSession)
   {
     _debugSession = gdbDebugSession;
   }

   public String[] getStorageContents()
   {  String[] strs = new String[_maxLines]; 
      for(int z=0; z<_maxLines; z++) 
      {   strs[z] = _storageContents[z];  
      }
      return strs; 
   }
   public String[] getStorageAddresses()
   {  String[] strs = new String[_maxLines]; 
      for(int z=0; z<_maxLines; z++) 
      {   strs[z] = _storageAddresses[z];  
      }
      return strs; 
   }

   public int getMaxLines()
   {  return _maxLines; 
   }

   public void updateStorage(String totalColumns, int columnsPerLine, String mode, String wordSize, int charsPerColumn, int thrd, String expr)
   {

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"******************* GetGdbStorage getStorage totalColumns="+totalColumns+" columnsPerLine="+columnsPerLine+" mode="+mode+" wordsize="+wordSize+" charsPerColumn="+charsPerColumn+" thrd="+thrd+" expr="+expr+" ##################"  );

      String cmd;
      boolean ok;

      if(thrd != _debugSession.getCurrentThreadID() )
      {   if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"GetGdbStorage SETTING thread="+ thrd );
         cmd = "thread "+Integer.toString(thrd);
         ok = _debugSession.executeGdbCommand(cmd);
         if(!ok)
             return;
 
      }

      updateStorage(totalColumns, columnsPerLine, mode, wordSize, charsPerColumn, expr );

      if(thrd!= _debugSession.getCurrentThreadID() )
      {   if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"GetGdbStorage RE-setting thread=current="+_debugSession.getCurrentThreadID() );
         cmd = "thread "+Integer.toString(_debugSession.getCurrentThreadID());
         ok = _debugSession.executeGdbCommand(cmd);
         if(!ok)
            ;
      }

      return;
   }

   public void updateStorage(String totalColumns, int columnsPerLine, String mode, String wordSize, int charsPerColumn, String startAddress )
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"******************* GetGdbStorage updateStorage columnsPerLine="+columnsPerLine+" totalColumns="+totalColumns+" mode="+mode+" wordsize="+wordSize+" charsPerColumn="+charsPerColumn+ " startAddress="+startAddress );
      String cmd;
      boolean ok;

      lastEvaluationError = "ERROR decoding storage memory display: ";
      cmd = "x/"+totalColumns+mode+wordSize+" "+startAddress;
      ok = _debugSession.executeGdbCommand(cmd);
      if(!ok)
          return;
 
      String[] lines = _debugSession.getTextResponseLines();
      if(lines.length<=0)
      {   if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"GetGdbStorage.updateStorage lines==null" );
          return;                       
      }

      _maxLines = lines.length;
      _storageAddresses = new String[_maxLines];
      _storageContents = new String[_maxLines];

      String address     = "";
      String data        = "";
      for(int i=0; i<lines.length; i++)
      { 
        String str = lines[i];
        _storageContents[i] = "?storage_error?";
        if(str!=null && !str.equals("") )
        {   if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(2,"GetGdbStorage i="+i+" storage str: "+str );
            int space = str.indexOf(" ");
            int tab = str.indexOf("\t");
            if(tab>=0 && (tab<space || space<0) )
               space = tab;
            if(space<0)
            {
               if (Gdb.traceLogger.ERR) 
                   Gdb.traceLogger.err(2,"GetGdbStorage missing ' ' in storage str: "+str );
               lastEvaluationError += str;
               return;
            }
            if(!str.startsWith("0x"))
            {
               if (Gdb.traceLogger.ERR) 
                   Gdb.traceLogger.err(2,"GetGdbStorage no starting '0x' in storage str: "+str );
               lastEvaluationError += str;
               return;
            }
            address = str.substring(2,space);
            address = fixedSizeString(address,charsPerColumn);

            int colon = str.indexOf(":");
            if(colon<=0)
            {  if (Gdb.traceLogger.ERR) 
                   Gdb.traceLogger.err(2,"GetGdbStorage no ':' in storage str: "+str );
               lastEvaluationError += str;
               return;
            }

            data = str.substring(colon+1)+" ";
            String contents = "";
            for(int z=0; z<columnsPerLine; z++)
            {
                int hex = data.indexOf("0x");
                if(hex<0)
                {  if (Gdb.traceLogger.ERR) 
                       Gdb.traceLogger.err(2,"GetGdbStorage no '0x' in storage str: "+data );
                   lastEvaluationError += str;
                   return;
                }
                hex += 2;
                space = data.indexOf(" ",hex);
                tab = data.indexOf("\t",hex);
                if(tab>=0 && (tab<space || space<0) )
                   space = tab;

                if(space<0)
                {  if (Gdb.traceLogger.ERR) 
                       Gdb.traceLogger.err(2,"GetGdbStorage no ' ' in storage str: "+data.substring(hex) );
                   lastEvaluationError += str;
                   return;
                }
                String s = data.substring(hex,space);
                contents = contents + fixedSizeString(s,8);
                data = data.substring(space+1);
            }
            if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(3,"GetGdbStorage updateStorage line="+i+" address="+address+" contents="+contents );
            _storageAddresses[i] = address;
            _storageContents[i] = contents;
        }
      }
      lastEvaluationError = null;
   }

   private String fixedSizeString(String str, int charsPerColumn)
   {
       int i = charsPerColumn-str.length();
       if(i<0)
       {  if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(3,"GetGdbStorage column string has too many characters="+i );
       }
       else if(i>0)
       {
          for(int z=0; z<i; z++)
            str = "0"+str;
       }
       return str;
   }


   public String evaluateAddressOfExpression(String expr, int thrd)
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"******************* GetGdbStorage evaluateAddressOfExpression expr="+expr+" exprDU="+thrd +" ##################"  );

      String cmd;
      boolean ok;

      if(thrd != _debugSession.getCurrentThreadID() )
      {   if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"GetGdbStorage SETTING thread="+ thrd );
         cmd = "thread "+Integer.toString(thrd);
         ok = _debugSession.executeGdbCommand(cmd);
         if(!ok)
             return null;
 
      }

      String address = evaluateAddressOfExpression(expr );

      if(thrd!= _debugSession.getCurrentThreadID() )
      {   if (Gdb.traceLogger.DBG) 
              Gdb.traceLogger.dbg(3,"GetGdbStorage RE-setting thread=current="+_debugSession.getCurrentThreadID() );
         cmd = "thread "+Integer.toString(_debugSession.getCurrentThreadID());
         ok = _debugSession.executeGdbCommand(cmd);
         if(!ok)
             return address;
 
      }

      return address;
   }

   public String evaluateAddressOfExpression(String expr)
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"******************* GetGdbStorage evaluateAddressOfExpression expr="+expr  );

      lastEvaluationError = null;
      String cmd = "print "+expr;
      boolean ok = _debugSession.executeGdbCommand(cmd);
      if(!ok)
          return null;
 

      String[] lines = _debugSession.getTextResponseLines();
      if(lines.length<=0)
      {   if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"GetGdbStorage.evaluateAddressOfExpression lines==null" );
          return null;                       
      }

      String str = lines[0];
      if(lines.length>1)
         str = str + lines[1];
      if(!str.startsWith("$"))
      {   
          if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(2,"GetGdbStorage.evaluateAddressOfExpression="+expr+" returned error message="+str );
          lastEvaluationError = str;
          return null;
      }
      int equals = str.indexOf("= ");
      if(equals<0)
      {   
          if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(2,"GetGdbStorage.evaluateAddressOfExpression missing '=' in response str="+str );
          return null;
      }
      str = str.substring(equals+1);

      if(str.endsWith(">"))
      {   int z = str.lastIndexOf(" <");
          if(z > 0 )
             str = str.substring(0,z);
      }
      int space = str.lastIndexOf(" ");
      if(space<0)
      {   
          if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(2,"GetGdbStorage.evaluateAddressOfExpression missing ' ' in response str="+str );
          return null;
      }
      str = str.substring(space+1);
      if(!str.startsWith("0x"))
      {
         long decimal = 0;
         try { decimal = Long.parseLong(str); } 
         catch(java.lang.NumberFormatException e)
         {
             if (Gdb.traceLogger.ERR) 
                 Gdb.traceLogger.err(2,"GetGdbStorage.evaluateAddressOfExpression incorrect numeric format in response str="+str );
             return null;
         }
         str = "0x"+Long.toHexString(decimal);
      }

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"GetGdbStorage.evaluateAddressOfExpression str="+str );
      return str;
   }
}
