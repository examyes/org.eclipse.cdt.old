/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
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
        
//        if (i%2 == 0)
//        {
//	        _storageContents[i/2] = "?storage_error?";
//        }	        
       
       _storageContents[i] = "?storage error?";
       _storageAddresses[i] = "00000000";
       
       if (str.startsWith("Cannot access memory at address"))
       {
       		_maxLines--;
       		continue;
       }
       
        if(str!=null && !str.equals("") )
        {
        	if (Gdb.traceLogger.DBG) 
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
            
//            if (i%2 == 0)
//            {
	            address = str.substring(2,space-1);
	            address = fixedSizeString(address,charsPerColumn);
//            }

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
//	        for(int z=0; z<8; z++)
            {
                int hex = data.indexOf("0x");
                if(hex<0)
                {  if (Gdb.traceLogger.ERR) 
                       Gdb.traceLogger.err(2,"GetGdbStorage no '0x' in storage str: "+data );
                       // return with whatever we have... do not set lastEvaluationError
//                   lastEvaluationError += str;
					_maxLines--;
				   lastEvaluationError = null;
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
//                contents = contents + fixedSizeString(s,2);
                contents = contents + fixedSizeString(s,8);
//				contents = contents + s;
                data = data.substring(space+1);
            }
            if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(3,"GetGdbStorage updateStorage line="+i+" address="+address+" contents="+contents );

	            _storageAddresses[i] = address;
	            _storageContents[i] = contents;
			
//			if (i%2 == 0)
//			{
//	            _storageAddresses[i/2] = address;
//	            _storageContents[i/2] = contents;
//			}
//			else
//			{
//				_storageContents[(i-1)/2] += contents;
//			}
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
		  String registerAdd = evaluateAddressOfRegister(expr);
		  
		  if (registerAdd != null && isAddressValid(registerAdd))
		  	return registerAdd;
      	
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
        
      int hex = str.indexOf("0x");
      
      if (hex != -1)
      {
      	  if (hex+10 <= str.length())
      	  {
		      str = str.substring(hex, hex+10);
      	  }		      
		  else
		  {
		  	  str = str.substring(hex);
		  }
		  
	      str = str.trim();
	      
	      if (!isAddressValid(str))
	      {
	      	return null;
	      }
	      else
	      {
		      return str;
	      }		      
      }	      
      
      int space = str.lastIndexOf(" ");
      if(space<0)
      {   
          if (Gdb.traceLogger.ERR) 
              Gdb.traceLogger.err(2,"GetGdbStorage.evaluateAddressOfExpression missing ' ' in response str="+str );
          return null;
      }
      
      // if it's &a
      // $1 = 13243251
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
      
      if (!isAddressValid(str))
      {
      	return null;
      }

      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"GetGdbStorage.evaluateAddressOfExpression str="+str );
      return str;
   }
   
   public String evaluateAddressOfRegister(String expr)
   {
		String str;
		
		if (Gdb.traceLogger.DBG) 
	       Gdb.traceLogger.dbg(2,"******************* GetGdbStorage evaluateAddressOfRegister expr="+expr  );
	
	    lastEvaluationError = null;
	    String cmd = "info register "+expr;
	    boolean ok = _debugSession.executeGdbCommand(cmd);
	    if(!ok)
	        return null;
	 
	
	    String[] lines = _debugSession.getTextResponseLines();
	    if(lines.length<=0)
	    {   if (Gdb.traceLogger.ERR) 
	           Gdb.traceLogger.err(2,"GetGdbStorage.evaluateAddressOfRegister lines==null" );
	        return null;                       
	    }
	
		str = lines[0];
	    if(lines.length>1)
	       str = str + lines[1];
	    if(str.indexOf("invalid register") != -1)
	    {   
			if (Gdb.traceLogger.ERR) 
				Gdb.traceLogger.err(2,"GetGdbStorage.evaluateAddressOfRegister="+expr+" returned error message="+str );
			lastEvaluationError = str;
			return null;
		}
		
		String tab = "\u0009";
		String hex = "0x";
		
		int hexIndex = str.indexOf(hex);
		int lastTab = str.lastIndexOf(tab);
		
		if (hexIndex > 0 && lastTab > 0 && lastTab > hexIndex)
		{
			str = str.substring(hexIndex, lastTab);
		}
		else
		{
			str = null;
		}			
		
		return str;
	}
	
	private boolean isAddressValid(String address)
	{
		String cmd = "x "+address;
	    boolean ok = _debugSession.executeGdbCommand(cmd);
	    if(!ok)
	        return false;
	 
	
	    String[] lines = _debugSession.getTextResponseLines();
	    if(lines.length<=0)
	    {   if (Gdb.traceLogger.ERR) 
	           Gdb.traceLogger.err(1,"Invalid Address: " + address);
	        return false;                       
	    }		
	    
	    for (int i=0; i<lines.length; i++)
	    {
	    	if (lines[i].indexOf("Cannot access memory") != -1)
	    	{
	    		if (Gdb.traceLogger.ERR) 
		           Gdb.traceLogger.err(1,"Invalid Address: " + address);
	    		return false;
	    	}
	    }
	    return true;
	}
}

