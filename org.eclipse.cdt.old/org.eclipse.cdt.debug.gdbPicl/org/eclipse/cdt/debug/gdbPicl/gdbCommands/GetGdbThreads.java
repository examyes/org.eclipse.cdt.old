/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.gdbCommands;
import  com.ibm.debug.gdbPicl.*;
import  com.ibm.debug.gdbPicl.objects.*;
import  com.ibm.debug.gdbPicl.gdbCommands.GdbProcess;

import java.util.Vector;

/**
 * gets Gdb Threads
 */
public class GetGdbThreads
{
   Vector _gdbThreads = null;
   int    stoppingThread = 0;
   int    currentThread = 0;

   GdbProcess       _gdbProcess    = null;
   GdbDebugSession  _debugSession  = null;
   GdbModuleManager _moduleManager = null;

  /**
   * Create a new GetGdbThreads command object
   */
   public GetGdbThreads(GdbDebugSession gdbDebugSession)
   {
     _debugSession = gdbDebugSession;
     _gdbProcess = gdbDebugSession.getGdbProcess();
     _moduleManager =(GdbModuleManager)gdbDebugSession.getModuleManager();
   }

   public Vector getThreads()
   {
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(2,"******************* GetGdbThreads getThreads" );

      int    largestThreadNumber=20;
      _gdbThreads = new Vector(largestThreadNumber+1);
      Object o = null;
      for(int z=0; z<_gdbThreads.capacity(); z++) 
         _gdbThreads.addElement(o);
      String cmd = "info threads ";
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(3,"GetGdbThreads.getThreads cmd="+cmd );

//      boolean ok = _debugSession.executeGdbCommand(cmd);  // this throws away MARKERs !!
      boolean ok = _gdbProcess.writeLine(cmd);
      if(!ok)
          return _gdbThreads;
 
//      String[] lines = _debugSession.getTextResponseLines();
      String[] lines = _gdbProcess.readAllLines();



      String FRAME_BEGIN_keyword  = _gdbProcess.MARKER+"frame-begin";
      String FRAME_END_keyword    = _gdbProcess.MARKER+"frame-end";
      String FRAME_FUNCTION_NAME  = _gdbProcess.MARKER+"frame-function-name";
      String FRAME_SOURCE_FILE    = _gdbProcess.MARKER+"frame-source-file";
      String FRAME_SOURCE_LINE    = _gdbProcess.MARKER+"frame-source-line";
      String FRAME_SOURCE_WHERE   = _gdbProcess.MARKER+"frame-where";
      String FRAME_ADDRESS        = _gdbProcess.MARKER+"frame-address";
      String FRAME_SOURCE_FROM    =                    "from ";
      String PRE_PROMPT_keyword   = _gdbProcess.MARKER+"pre-prompt";
      String POST_PROMPT_keyword  = _gdbProcess.MARKER+"post-prompt";
      String THREAD = "hread ";

      String current = " ";
      String threadName = "";
      String threadID = "";
      String systemTID = "";
      String functionName = "";
      String fileName = "";
      String frameAddress = "";
      String fileLine = "";
      int    moduleID = 0;
      boolean threadsFound = false;

      for(int i=0; i<lines.length; i++)
      { 
        String str = lines[i];
        if(str!=null && !str.equals("") )
        { 
           if( str.equals(POST_PROMPT_keyword) || str.equals(FRAME_END_keyword) )
           {  
              if( str.equals(FRAME_END_keyword) )
              {     
                 threadsFound = true;
                 int intThreadID = -1;
                 try { intThreadID = Integer.valueOf(threadID).intValue(); } 
                 catch(java.lang.NumberFormatException exc) 
                 {
                    if (Gdb.traceLogger.ERR) 
                        Gdb.traceLogger.err(2,"GetGdbThreads.getThreads non-numeric threadID="+threadID  );
                 }
                 if(intThreadID>=largestThreadNumber)
                 {
                     largestThreadNumber = intThreadID+20;
                     _gdbThreads.ensureCapacity(largestThreadNumber+1);
                     for(int z=_gdbThreads.size()-1; z<_gdbThreads.capacity()-1; z++) 
                        _gdbThreads.addElement(o);
                 }
                 threadID = String.valueOf(intThreadID);
                 String response = current+" "+threadID+" (systemTID="+systemTID+")  in "+functionName+"  at file "+fileName+":"+fileLine+" (topThread="+largestThreadNumber+")" ;

                 if(moduleID<=0 && frameAddress.equals("") && !fileName.equals(""))
                    moduleID = _moduleManager.findFirstModuleID(fileName);
                 if(moduleID<=0)
                 {
                    if (Gdb.traceLogger.ERR) 
                        Gdb.traceLogger.err(2,"GetGdbThreads.getThreads INVALID ModuleID=="+moduleID+" current="+current+" threadID="+threadID+" (systemTID="+systemTID+")"+" function="+functionName+" file="+fileName+" line="+fileLine+" frameAddress="+frameAddress ); 
                    moduleID=1;
                 }

                 if (Gdb.traceLogger.EVT) 
                     Gdb.traceLogger.evt(2,"GetGdbThreads.getThreads current="+current+" threadID="+threadID+" (systemTID="+systemTID+")"+" function="+functionName+" file="+fileName+" line="+fileLine+" frameAddress="+frameAddress+" moduleID="+moduleID ); 
                 boolean b = false;
                 if(current.equals("*")) 
                 {   b = true;
                     if(intThreadID>=0)
                        _debugSession.setCurrentThreadID(intThreadID);
                 }
                 GdbThread gdbThread = null;
                 
                 // intThreadID could still be less than 0 here
                 if (intThreadID > 0)
                 {
	                 if(!_gdbThreads.isEmpty() && _gdbThreads.elementAt(intThreadID)!=null)
	                     gdbThread = (GdbThread)_gdbThreads.elementAt(intThreadID);
 
	                 if(gdbThread==null)
	                 {
	                    gdbThread = new GdbThread(_debugSession, b,intThreadID, "systemTID "+systemTID,systemTID,functionName,fileName,frameAddress,fileLine, moduleID);
	                    _gdbThreads.setElementAt(gdbThread,intThreadID);
	                 }
	                 else
	                 {
	                     gdbThread.update(systemTID,functionName,fileName,frameAddress,fileLine, moduleID );
	                 }
                 }
              }
              current = " ";
              threadID="";
              systemTID="";
              functionName = "";
              fileName = "";
              frameAddress = "";
              fileLine = "";
              moduleID = _moduleManager.getModuleID(_debugSession.getProgramName());
              String lastStr = str;

              str = lines[++i];
              
              // for multi-threaded application, when a new thread was added
              if (str.startsWith("[New "))
              {
              	// read next line, that's where the thread id is
              	str = lines[++i];
              }
              
              if(str!=null && !str.equals("")) 
              {             	
                  if(str.charAt(0)=='*')
                  {
                    current = "*";
                  }
                  str = str.substring(2);
                  int space = str.indexOf(" ");
                  threadID = str.substring(0,space);
                  space = str.indexOf(THREAD);
                  str = str.substring(space+THREAD.length()); 
                  space = str.indexOf(" ");
                  systemTID = str.substring(0,space);
              }
           }
           else if( str.equals(FRAME_FUNCTION_NAME) )
           {
              str = lines[++i];
              functionName = str;
           }
           else if( str.equals(FRAME_SOURCE_FILE) )
           {
              str = lines[++i];
              fileName = str;
              int slash = fileName.lastIndexOf("/");
              if(slash>=0)
                 fileName = fileName.substring(slash+1);
           }
           else if( str.equals(FRAME_SOURCE_WHERE) )
           {
              str = lines[++i];
              int from = str.indexOf(FRAME_SOURCE_FROM);
              if(from>=0)
              {   str = str.substring(from+FRAME_SOURCE_FROM.length());
                  fileName = str;
                  int slash = fileName.lastIndexOf("/");
                  if(slash>=0)
                     fileName = fileName.substring(slash+1);
                  fileLine = "0";
              }
           }
           else if( str.equals(FRAME_SOURCE_LINE) )
           {
              str = lines[++i];
              fileLine = str;
           }
           else if( str.equals(FRAME_ADDRESS) )
           {
              frameAddress = lines[++i];
              if(frameAddress==null || frameAddress.equals("") )
                 moduleID = 0;
              else
                 moduleID = _moduleManager.containsAddress(frameAddress);
           }
           else if( str.equals(PRE_PROMPT_keyword) 
                  && threadsFound==false 
                     )
           {
                 threadName = "Main";
                 int intThreadID = 1;
                 systemTID = "0";

                 GdbThread gdbThread = null;
                 if(!_gdbThreads.isEmpty() && _gdbThreads.elementAt(intThreadID)!=null)
                     gdbThread = (GdbThread)_gdbThreads.elementAt(intThreadID);
                 if(gdbThread==null)
                 {
                    boolean b = true; //current
                    gdbThread = new GdbThread(_debugSession,b,intThreadID, threadName,systemTID,functionName,fileName,frameAddress,fileLine, moduleID);
                    _gdbThreads.setElementAt(gdbThread,intThreadID);
                 }
                 functionName = _debugSession.getCurrentFunctionName();
                 fileName = _debugSession.getCurrentFileName();
                 frameAddress = _debugSession.getCurrentFrameAddress();
                 fileLine = _debugSession.getCurrentLineNumber();
                 moduleID = _debugSession.getCurrentModuleID();
                 _debugSession.setCurrentThreadID(1);

                 gdbThread.update(systemTID,functionName,fileName,frameAddress,fileLine, moduleID );
                 if (Gdb.traceLogger.EVT) 
                     Gdb.traceLogger.evt(1,"################ GetGdbThreads.getThreads DUMMY-THREAD-0 functionName="+functionName
                         +" fileName="+fileName+" fileLine="+fileLine+" frameAddress="+frameAddress+" moduleID="+moduleID );
                 break;
           }
        }
      }

      return _gdbThreads;
   }
}
