/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;
import  com.ibm.debug.gdbPicl.gdbCommands.GdbProcess;

import com.ibm.debug.epdc.*;
import java.util.Vector;

public class GdbThread
{
      Vector _gdbStackFrames = new Vector();
      GdbDebugSession _gdbDebugSession = null;
      GdbThreadComponent _gdbThreadComponent = null;
      boolean _current = false;
      String _threadName = "";
      int    _intThreadID = 0;
      int    _systemTID = 0;
      String _functionName = "";
      String _fileName = "";
      String _frameAddress = "";
      String _fileLine = "";
      int    _moduleID = -1;
      int    _status = EPDC.StdThdUnknown;

      public GdbThread(GdbDebugSession gdbDebugSession, boolean current, int intThreadID, String threadName, String systemTID, String functionName,String fileName,String frameAddress, String fileLine, int moduleID)
      {
         _gdbDebugSession = gdbDebugSession;
         _current = current;
         _intThreadID = intThreadID;
//         try { _systemTID = Integer.parseInt(threadID); } catch(java.lang.NumberFormatException exc) { ; }
//         _systemTID = _intThreadID;
         int radix=10;
         int hex = systemTID.indexOf("0x");
         if(hex>=0)
         {
            radix=16;
            systemTID = systemTID.substring(hex+2);
         }
         try { _systemTID = Integer.parseInt(systemTID,radix); } catch(java.lang.NumberFormatException exc) { ; }

//         _threadName = String.valueOf(_systemTID);
         _threadName = threadName;
         _functionName = functionName;
         _fileName = fileName;
         _frameAddress = frameAddress;
         _fileLine = fileLine;
         _moduleID = moduleID;
         _gdbThreadComponent = new GdbThreadComponent(_gdbDebugSession, _intThreadID, this );
      }
      public GdbThread(GdbDebugSession gdbDebugSession,int intThreadID, String threadName, String systemTID )
      {  this(gdbDebugSession,false,intThreadID,threadName,systemTID,"","","","",-1);
      }

      public void update(String systemTID,String functionName,String fileName,String frameAddress,String fileLine,int moduleID)
      {
         try { _systemTID = Integer.parseInt(systemTID); } catch(java.lang.NumberFormatException exc) { ; }
         _functionName = functionName;
         _fileName = fileName;
         _frameAddress = frameAddress;
         _fileLine = fileLine;
         _moduleID = moduleID;
      }

      public String getStatus()
      { String s = "unimplemented_status_feature";
        return s;
      }
      public int getSystemTID()
      {   return _systemTID;  }
      public int getIntThreadID()
      {   return _intThreadID;  }
//      void setModuleID(int i)
//      { _moduleID = i; }
      public void setGdbThreadComponent(GdbThreadComponent tc)
      { _gdbThreadComponent = tc; }
      public GdbThreadComponent getGdbThreadComponent()
      {  return _gdbThreadComponent; }
      void setCurrentFunction(String name)
      { _functionName = name; }
      void setCurrentFile(String name)
      { _fileName = name; }
      public String getCurrentLine()
      {  return _fileLine; }
      public String getThreadName()
      {  return _threadName; }
      void setCurrentLine(String str)
      { _fileLine = str; }
      void setCurrentStatus(int i)
      { _status = i; }
      void setIsCurrentThread(boolean b)
      { _current = b; }
      public boolean isCurrentThread()
      { return _current; }

      GdbStackFrame[] getStack(boolean ignoreStackTracking)
      {
         GdbStackFrame gdbStackFrame = new GdbStackFrame(0,_intThreadID,0,_fileName,_functionName, _frameAddress, _fileLine, _moduleID );
         GdbStackFrame[] frames = new GdbStackFrame[1];
         frames[0] = gdbStackFrame;
         if(_gdbThreadComponent==null)
         {
            if (Gdb.traceLogger.ERR)
                Gdb.traceLogger.err(1,"############ GdbThread.getStack threadComponent==NULL ###############" );
            return frames;
         }
         if (Gdb.traceLogger.EVT)
             Gdb.traceLogger.evt(2,"GdbThread.getStack intThreadID="+_intThreadID+" DU="+_gdbThreadComponent._DU+" stackTracking="+_gdbThreadComponent._stackTracking+" stackReported="+_gdbThreadComponent._stackReported );
             
         if (!ignoreStackTracking)             
         {
	         if(!_gdbThreadComponent._stackTracking)
	         {
				//System.out.println("GdbThread.getStack **IGNORING _stackTracking==FALSE " );
	            return frames;
	         }
	      }

         Vector SFs = getStackFrames();
         if(SFs==null || SFs.size()==0)
         {
            if (Gdb.traceLogger.EVT)
                Gdb.traceLogger.evt(1,"GdbThread.getStack getStackFrames==null" );
            return frames;
         }

         frames = new GdbStackFrame[SFs.size()];
         for(int i=0; i<SFs.size(); i++)
         {
            frames[i] = (GdbStackFrame)SFs.elementAt(i);
         }
         return frames;
      }


   Vector getStackFrames()
   {
      GdbProcess _gdbProcess = _gdbDebugSession.getGdbProcess();
      GdbModuleManager mm = (GdbModuleManager)_gdbDebugSession.getModuleManager();
      if(_intThreadID != _gdbDebugSession.getCurrentThreadID()
          && _gdbDebugSession.getCurrentThreadID()!=0  )
      {
         if (Gdb.traceLogger.EVT)
             Gdb.traceLogger.evt(3,"GdbThread.getStackFrames SETTING thread="+_intThreadID+" (currently="+_gdbDebugSession.getCurrentThreadID()+")" );
         _gdbDebugSession.executeGdbCommand("thread "+_intThreadID);
      }
      _gdbDebugSession.cmdResponses.removeAllElements();
      _gdbStackFrames.removeAllElements();
      String cmd = "info stack ";
      boolean ok = _gdbDebugSession.getGdbProcess().writeLine(cmd);
      String[] lines = _gdbDebugSession.getGdbProcess().readAllLines();
      if (Gdb.traceLogger.EVT)
          Gdb.traceLogger.evt(3,"GdbThread.getStackFrames respones lines.length="+lines.length );

      String FRAME_BEGIN_keyword  = _gdbProcess.MARKER+"frame-begin";
      String FRAME_END_keyword    = _gdbProcess.MARKER+"frame-end";
      String FRAME_FUNCTION_NAME  = _gdbProcess.MARKER+"frame-function-name";
      String FRAME_ARGS           = _gdbProcess.MARKER+"frame-args";
      String FRAME_ARGS_END       = _gdbProcess.MARKER+"arg-end";
      String FRAME_SOURCE_BEGIN   = _gdbProcess.MARKER+"frame-source-begin";
      String SOURCE_keyword       = _gdbProcess.MARKER+"source";
      String FRAME_SOURCE_FILE    = _gdbProcess.MARKER+"frame-source-file";
      String FRAME_SOURCE_LINE    = _gdbProcess.MARKER+"frame-source-line";
      String FRAME_SOURCE_WHERE   = _gdbProcess.MARKER+"frame-where";
      String FRAME_ADDRESS        = _gdbProcess.MARKER+"frame-address";
      String FRAME_SOURCE_FROM    =                    "from ";
      String PRE_PROMPT_keyword   = _gdbProcess.MARKER+"pre-prompt";
      String POST_PROMPT_keyword  = _gdbProcess.MARKER+"post-prompt";
      String THREAD = "hread ";

      String frameID = "";
      String functionName = "";
//      String args = "";
      // 	System.out.println("RW ===== GdbThread.java -info stack- lines.length: " + lines.length);
      StringBuffer args = new StringBuffer(lines.length);
      String fileName = "";
      String fileLine = "";
      String frameAddress = "";
      int    moduleID = -1;

      for(int i=0; i<lines.length; i++)
      {
        String str = lines[i];
        if(str!=null && !str.equals("") )
        {
//System.out.println("GdbThread.getStackFrames str="+str );

           if( str.equals(POST_PROMPT_keyword) || str.equals(FRAME_END_keyword) )
           {
              if( str.equals(FRAME_END_keyword) )
              {
//                 String response = "_intThreadID="+_intThreadID+" frameID="+frameID+" (address="+frameAddress+" moduleID="+moduleID+")  in "+functionName+" args="+args+ "  at file "+fileName+":"+fileLine;
                 String response = "_intThreadID="+_intThreadID+" frameID="+frameID+" (address="+frameAddress+" moduleID="+moduleID+")  in "+functionName+" args="+args.toString()+ "  at file "+fileName+":"+fileLine;
                 _gdbDebugSession.addLineToUiMessages(response);
                 if (Gdb.traceLogger.EVT)
                     Gdb.traceLogger.evt(1,"<<<<<<<<-------- GdbThread.getStackFrames "+response );
                 GdbStackFrame gdbStackFrame = new GdbStackFrame(i,_intThreadID,i,fileName,functionName,frameAddress, fileLine, moduleID);
                 _gdbStackFrames.addElement(gdbStackFrame);
              }
              frameID="";
              functionName = "";
              frameAddress = "";
//              args = "";
              args.setLength(0);
              fileName = "";
              fileLine = "";
              moduleID = mm.getModuleID(_gdbDebugSession.getProgramName());

              str = lines[++i];
           }
           else if( str.equals(FRAME_FUNCTION_NAME) )
           {
              str = lines[++i];
              functionName = str;
           }
           else if( str.equals(FRAME_ARGS) )
           {
              while( !str.equals(FRAME_SOURCE_BEGIN) && !str.equals(FRAME_END_keyword) && i<lines.length )
              {
                 str = lines[++i];
                 
                 if ( !str.startsWith(_gdbProcess.MARKER) )
                 {
                    //args += str;
                    args.append(str);
                  	//System.out.println("RW ===== GdbThread.java -info stack- i: " + i);
                  	//System.out.println("RW ===== GdbThread.java -info stack- args: " + args.toString());
                  	
   	                // you may get no argument for this frame
	                if (str.equals(" ()"))
 	                	break;
                 }
              }
              if(str.equals(FRAME_SOURCE_BEGIN) || str.equals(FRAME_END_keyword))
                  --i;
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
           else if( str.startsWith(SOURCE_keyword) ) //??source //E/java-dev/cvt/testcases/TestGnu.c:12:165:beg:0x401037
           {
                int slash = str.lastIndexOf("/");
                if(slash>=0)
                    str = str.substring(slash+1);
                int colon = str.indexOf(":");
                if(colon>0)
                {
                   fileName = str.substring(0,colon);
                   str = str.substring(colon+1);
                   colon = str.indexOf(":");
                   if(colon>0)
                   {
                      fileLine =  str.substring(0,colon);
                   }
                }
//System.out.println("##### GdbThread.getStackFrames fileName="+fileName +" fileLine="+fileLine );
           }
           else if( str.startsWith(FRAME_BEGIN_keyword) )
           {
              int space = str.indexOf(" ");
              if(space<0)
              {
                 if (Gdb.traceLogger.ERR)
                     Gdb.traceLogger.err(2,"GdbThread.getStackFrames MISSING FIRST SPACE str="+str  );
                 continue;
              }
              str = str.substring(space+1);
              space = str.indexOf(" ");
              if(space<0)
              {
                 if (Gdb.traceLogger.ERR)
                     Gdb.traceLogger.err(2,"GdbThread.getStackFrames MISSING SECOND SPACE str="+str  );
                 continue;
              }
              frameID = str.substring(0,space);
              frameAddress = str.substring(space+1);
              moduleID = mm.containsAddress(frameAddress);
              if (Gdb.traceLogger.DBG)
                  Gdb.traceLogger.dbg(2,"GdbThread.getStackFrames intThreadID="+_intThreadID+" frameID="+frameID+" frameAddress="+frameAddress+" moduleID="+moduleID );
           }
           else if( str.equals(FRAME_ADDRESS) )
           {
              frameAddress = lines[++i];
              moduleID = mm.containsAddress(frameAddress);
           }
           else
           {
//System.out.println("GdbThread.getStackFrames ########### UNHANDLED str="+str );
           }
        }
      }

      if(_intThreadID != _gdbDebugSession.getCurrentThreadID()
          && _gdbDebugSession.getCurrentThreadID()!=0  )
      {
         if (Gdb.traceLogger.EVT)
             Gdb.traceLogger.evt(3,"GdbThread.getStackFrames RE-SETTING thread="+_gdbDebugSession.getCurrentThreadID() );
         _gdbDebugSession.executeGdbCommand("thread "+_gdbDebugSession.getCurrentThreadID());
      }
      return _gdbStackFrames;
   }



}

