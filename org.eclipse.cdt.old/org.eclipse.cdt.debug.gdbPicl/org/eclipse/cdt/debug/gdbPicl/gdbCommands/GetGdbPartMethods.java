/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.gdbCommands;

import  org.eclipse.cdt.debug.gdbPicl.*;
import  org.eclipse.cdt.debug.gdbPicl.objects.MethodInfo;
import  org.eclipse.cdt.debug.gdbPicl.objects.GdbPart;

/**
 * gets Gdb Threads
 */
public class GetGdbPartMethods
{
   private static final int MAX_METHODS = 50;
   private static final int MAX_SHARED_LIBRARIES = 30;
   public class ModuleInfo 
   { public String name = null;
     public String fullFileName = null;
     public String startAddress = null;
     public String endAddress = null;
   }
   ModuleInfo[] _moduleInfo = null;

   GdbDebugSession  _debugSession  = null;
   GdbModuleManager _moduleManager = null;

  /**
   * Create a new GetGdbPartMethods. command object
   */
   public GetGdbPartMethods(GdbDebugSession gdbDebugSession)
   {
     _debugSession = gdbDebugSession;
     _moduleManager = (GdbModuleManager)_debugSession.getModuleManager();
   }

  public MethodInfo[] getPartMethods(GdbPart part)
  {
     if(!part.isDebuggable())
        return null;

////////////////////// The code below is not needed if the IDE Famework keeps the list of methods //////////////////////////
     if (Gdb.traceLogger.EVT) 
         Gdb.traceLogger.evt(1,"################ GetGdbPartMethods.getPartMethods - time consuming - for DEBUGGABLE part="+part.getName() );


     MethodInfo[] methods = new MethodInfo[MAX_METHODS+1];
     int index =-1;

     String cmd = "info functions";
     boolean ok = _debugSession.executeGdbCommand(cmd);
     if( !ok )
        return new MethodInfo[0];
 

     if(_debugSession.cmdResponses.size()>0 )
     {
        int length = _debugSession.cmdResponses.size();
        if(length>_debugSession.MAX_GDB_LINES)
        {  
            if (Gdb.traceLogger.ERR) 
                Gdb.traceLogger.err(2,"GetGdbPartMethods.getPartMethods commandResponse lines>"+_debugSession.MAX_GDB_LINES+", length="+_debugSession.cmdResponses.size() );
            length = 1000;
        }
        String target = "File "+part.getName()+":";
        boolean processingFile = false;
        for(int i=0; i<length; i++)
        {   
           String str = (String)_debugSession.cmdResponses.elementAt(i);
           if(str==null || str.equals(""))
           {   str = " ";
               processingFile = false;
           }
           else
           {  
              if(str.equals(target))
              {
                 processingFile = true;
                 _debugSession.addLineToUiMessages(str);
              }
              else if(processingFile)
              {                               
                 if(str.startsWith("File ") )
                     break;
                 if(str.startsWith("Non-debugging") )
                     break;
                 else
                 {
                    index++;
                    if(index>=MAX_METHODS)
                    {
                       if (Gdb.traceLogger.ERR) 
                           Gdb.traceLogger.err(1,_debugSession.getResourceString("PART_CONTAINS_TOO_MANY_METHODS")+ "("+part.getName()+") "+">"+MAX_METHODS );
                       break;
                    }
                    methods[index] = new MethodInfo();
                    _debugSession.addLineToUiMessages(str);
                    int paren = str.indexOf("(");
                    String name = str.substring(0,paren);
                    int space = name.lastIndexOf(" ");
                    String fullName = str.substring(space+1,str.length()-1);
                    String type = "???";
                    if(space<=0)
                    {
                       if (Gdb.traceLogger.ERR) 
                           Gdb.traceLogger.err(2,"GetGdbPartMethods.getPartMethods missing ' ' from str="+str );
                       continue;
                    }
                    else
                    {
                       type = name.substring(0,space);
                       name = name.substring(space+1);
                       while(name.charAt(0)=='*')
                       {
                          type += "*";
                          name = name.substring(1);
                          fullName = fullName.substring(1);
                       }
                    }
                    methods[index]._name = name;
                    methods[index]._fullName = fullName;
                    methods[index]._returnType = type;
                    methods[index]._lineNum = 1;             // first executable line in the method
                    methods[index]._startLineNum = 1;        // starting line number for the method
                    if (Gdb.traceLogger.EVT) 
                        Gdb.traceLogger.evt(1,"GetGdbPartMethods.getPartMethods part="+part.getName() +" name="+name +" fullName="+fullName +" type="+type  );
                 }
              }
           }
        }
     }
     _debugSession.cmdResponses.removeAllElements();
     _debugSession.addMethodsToPart(part, methods);
     return part.getMethods();
  }

  private MethodInfo[] debuggableMethods = null;
  public MethodInfo[] getDebuggableMethods(GdbPart part, MethodInfo[] methods)
  {
    int i = -1;
    for(int j=0; j<methods.length; j++)
    {  if(methods[j]==null)
          break;
       else
          i=j+1;
    }
    if(i<=0)
        return new MethodInfo[0];

    if (Gdb.traceLogger.DBG) 
        Gdb.traceLogger.dbg(1,"GetGdbPartMethods.getDebuggableMethods methods.length="+i );

    debuggableMethods = new MethodInfo[i]; // i = number of Methods
    for(int j=0; j<i; j++)
    {    
        debuggableMethods[j] = methods[j];
        if (Gdb.traceLogger.DBG) 
            Gdb.traceLogger.dbg(3,"GetGdbPartMethods.getDebuggableMethods getting line for method="+methods[j]._name );

        String cmd = "info line "+methods[j]._name;
        boolean ok = _debugSession.executeGdbCommand(cmd);
        if( !ok )
            return new MethodInfo[0];
 

        if(_debugSession.cmdResponses.size()>0 )
        {
           int k = 0; //cmdResponses.size();
           String partName = part.getName();
                      
           if (Gdb.traceLogger.DBG) 
               Gdb.traceLogger.dbg(2,"GetGdbPartMethods.getDebuggableMethods "+cmd+"  line_k="+((String)_debugSession.cmdResponses.elementAt(k)) );
           String line = ((String)_debugSession.cmdResponses.elementAt(k));
           _debugSession.addLineToUiMessages(line);
           int space = line.indexOf("Line ");
           if(space!=0)
           {
              if (Gdb.traceLogger.ERR) 
                  Gdb.traceLogger.err(2,"GetGdbPartMethods.getDebuggableMethods error ('Line') parsing result of cmd="+cmd+" response="+line );
              continue;
           }
           line = line.substring(space+5);
           space = line.indexOf(" ");
           String nmbr = line.substring(0,space);
           int lineNumber = -1;
           try  {  lineNumber = Integer.parseInt(nmbr);  } 
           catch(java.lang.NumberFormatException exc)
           {
              if (Gdb.traceLogger.ERR) 
                  Gdb.traceLogger.err(2,"GetGdbPartMethods.getDebuggableMethods error (int) parsing result of cmd="+cmd+" response="+line );
              continue;
           }
           int quote = line.indexOf(" of \"");
           if(quote<0)
           {
              if (Gdb.traceLogger.ERR) 
                  Gdb.traceLogger.err(2,"GetGdbPartMethods.getDebuggableMethods error (\' of \"FILENAME\') from cmd="+cmd+" response="+line );
              continue;
           }
           line = line.substring(quote+5);
           quote = line.indexOf("\" starts at");
           if(quote<0)
           {
              if (Gdb.traceLogger.ERR) 
                  Gdb.traceLogger.err(2,"GetGdbPartMethods.getDebuggableMethods error ('FILENAME\" starts at') from cmd="+cmd+" response="+line );
              continue;
           }
           line = line.substring(0,quote);
           if (Gdb.traceLogger.DBG) 
               Gdb.traceLogger.dbg(2,"GetGdbPartMethods.getDebuggableMethods TARGET partName="+partName+" ACTUAL fileName="+line );
           if(!partName.equals(line))
           {
              if (Gdb.traceLogger.ERR) 
                  Gdb.traceLogger.err(2,"GetGdbPartMethods.getDebuggableMethods **MISMATCH** TARGET partName="+partName+" ACTUAL fileName="+line );
              continue;
           }
           methods[j]._lineNum = lineNumber;             // first executable line in the method
           methods[j]._startLineNum = lineNumber-1;        // starting line number for the method
           if (Gdb.traceLogger.EVT) 
               Gdb.traceLogger.evt(2,"GetGdbPartMethods.getDebuggableMethods method="+methods[j]._name+" firstExecutableLine="+lineNumber+" firstListingLine="+(lineNumber-1) );
        }
    }

    return debuggableMethods;
  }

 
  public void getMethodsForAllParts()
  {
     MethodInfo[] methods = new MethodInfo[MAX_METHODS];
     int index =-1;

     int moduleID = 0;
     int partID = 0;
     GdbPart part = null;

     String cmd = "info functions";
     boolean ok = _debugSession.executeGdbCommand(cmd);
     if( !ok )
        return;
 
     String[] lines = _debugSession.getTextResponseLines();
     int length = lines.length;

     if(length>0 )
     {
        if(length>_debugSession.MAX_GDB_LINES)
        {  
            if(length>(_debugSession.MAX_GDB_LINES+_debugSession.MAX_GDB_LINES))
            {   if (Gdb.traceLogger.ERR) 
                    Gdb.traceLogger.err(1,_debugSession.getResourceString("DEBUGGEE_CONTAINS_TOO_MANY_METHODS")+length+">"+_debugSession.MAX_GDB_LINES );
                return;
            }
            else
               if (Gdb.traceLogger.ERR) 
                   Gdb.traceLogger.err(2,_debugSession.getResourceString("DEBUGGEE_CONTAINS_TOO_MANY_METHODS")+length+">"+_debugSession.MAX_GDB_LINES );
        }
        String target = "File ";  //+part.getName()+":";
        boolean processingFile = false;
        for(int i=0; i<length; i++)
        {   
           String str = lines[i];
           if(str==null || str.equals(""))
           {   str = " ";
               processingFile = false;
           }
           else
           {  
              if(str.startsWith(target))
              {
                 if(part!=null)
                 {
                     _debugSession.addMethodsToPart(part, methods);
                     part = null;
                     processingFile = false;
                     methods = new MethodInfo[MAX_METHODS];
                     index =-1;
                 }
                 String fullFileName = str.substring(target.length());    //"File "+name
                 fullFileName = fullFileName.substring(0,fullFileName.length()-1);
                 String fileName = fullFileName;
                 int x = fileName.lastIndexOf("/");
                 if(x>=0)
                     fileName = fileName.substring(x+1);
                 moduleID=_moduleManager.getModuleID(fileName,fullFileName);
                 partID = _moduleManager.getPartID(moduleID,fileName);
                 if (Gdb.traceLogger.DBG) 
                     Gdb.traceLogger.dbg(1,"GetGdbPartMethods.getMethodsForAllParts fileName="+fileName +" moduleID="+moduleID +" partID="+partID );
                 if(partID>0)
                 {
                     processingFile = true;
                     _debugSession.addLineToUiMessages(str);
                     part = (GdbPart) _moduleManager.getPart(partID);
                 }
              }
              else if(processingFile)
              {
                 if(str.startsWith("Non-debugging") )
                 {   processingFile = false;
                     _debugSession.addMethodsToPart(part, methods);
                     part = null;
                     methods = new MethodInfo[MAX_METHODS];
                     index =-1;
                     continue;
                 }
                 else
                 {
                
                    index++;
                    if(index>=MAX_METHODS)
                    {
                        String partName = "null";
                        if(part!=null)
                           partName = part.getName();
                        if (Gdb.traceLogger.ERR) 
                            Gdb.traceLogger.err(1,_debugSession.getResourceString("PART_CONTAINS_TOO_MANY_METHODS")+ "("+partName+") "+">"+MAX_METHODS );
                       break;
                    }
                    methods[index] = new MethodInfo();
                    _debugSession.addLineToUiMessages(str);
                    int paren = str.lastIndexOf("(");
                    String name = str.substring(0,paren);
                                      
                   
                    int space = name.lastIndexOf(" ");
                    String fullName = str.substring(space+1,str.length()-1);
                    String type = "???";
                    if(space<=0)
                    {
                       if (Gdb.traceLogger.ERR) 
                           Gdb.traceLogger.err(2,"GetGdbPartMethods.getDebuggableMethods error (' ') from cmd="+cmd+" response="+str );
                    }
                    else
                    {
                       type = name.substring(0,space);
                       name = name.substring(space+1);
                       
                       while(name.charAt(0)=='*')
                       {
                          type += "*";
                          name = name.substring(1);
                          fullName = fullName.substring(1);
                       }
                    }
                    methods[index]._name = name;
                    methods[index]._fullName = fullName;
                    methods[index]._returnType = type;
                    methods[index]._lineNum = 1;             // first executable line in the method
                    methods[index]._startLineNum = 1;        // starting line number for the method
                    if(part!=null)
                    {   if (Gdb.traceLogger.EVT) 
                            Gdb.traceLogger.evt(1,"<<<<<<<<======== GetGdbPartMethods.getMethodsForAllParts name="+name +" fullName="+fullName +" type="+type +" part="+part.getName() );
                    }else
                    {
                        if (Gdb.traceLogger.EVT) 
                            Gdb.traceLogger.evt(1,"<<<<<<<<======== GetGdbPartMethods.getMethodsForAllParts name="+name +" fullName="+fullName +" type="+type +" part==NULL" );
                    }
                 }
              }
           }
        }
     }
     if (Gdb.traceLogger.EVT) 
         Gdb.traceLogger.evt(1,"<<<<<<<<######## GetGdbPartMethods.getMethodsForAllParts DONE"   );
  }

}
