/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.gdbCommands;

import  org.eclipse.cdt.debug.gdbPicl.*;
import  org.eclipse.cdt.debug.gdbPicl.objects.Module;
import  org.eclipse.cdt.debug.gdbPicl.objects.Part;
import  org.eclipse.cdt.debug.gdbPicl.objects.GdbPart;
import  org.eclipse.cdt.debug.gdbPicl.objects.View;
import  org.eclipse.cdt.debug.gdbPicl.objects.GdbDisassemblyView;
import  org.eclipse.cdt.debug.gdbPicl.objects.GdbThreadComponent;

/**
 * gets Gdb Threads
 */
public class GetGdbModuleParts
{
   GdbDebugSession  _debugSession  = null;
   GdbProcess       _gdbProcess    = null;
   GdbModuleManager _moduleManager = null;
   GdbThreadManager _threadManager = null;

  /**
   * Create a new GetGdbModuleParts. command object
   */
   public GetGdbModuleParts(GdbDebugSession gdbDebugSession)
   {
     _debugSession  = gdbDebugSession;
     _gdbProcess    = _debugSession.getGdbProcess();
     _moduleManager = (GdbModuleManager)_debugSession.getModuleManager();
     _threadManager = (GdbThreadManager)_debugSession.getThreadManager();
   }

  public boolean getMainModule()
  {
    String cmd="info file";
    boolean ok = _debugSession.executeGdbCommand(cmd);
    if(!ok)
        return false;

    String[] lines = _debugSession.getTextResponseLines();
    String keyword = "Local exec file:";
    String fullName = "";
    String shortName = "";
    String startAddress = "";
    String endAddress = "";
    boolean found = false;

    //System.out.println("RW ===== GetGdbModuleParts.java -info file- lines.length: " + lines.length);

    for(int i=0; i<lines.length; i++)
    {  if(lines[i]!=null && !lines[i].equals("") && !lines[i].startsWith(_gdbProcess.MARKER))
       {
          int strt= lines[i].indexOf(keyword);
          if(strt>=0)      // found "Local exec file:"
          {
              _debugSession.addLineToUiMessages(lines[i++]);
              String str = lines[i++];   // next line is '/path/exeName'
              _debugSession.addLineToUiMessages(str);

              int quote = str.indexOf("`");
              if(quote<0)
              {
                  if (Gdb.traceLogger.ERR)
                      Gdb.traceLogger.err(2,"GetGdbModuleParts.getMainModule could not locate main module information, missing: "+"' from str="+str );
                  return false;
              }
              str = str.substring(quote+1);
              quote = str.indexOf("'");
              if(quote<0)
              {
                  if (Gdb.traceLogger.ERR)
                      Gdb.traceLogger.err(2,"GetGdbModuleParts.getMainModule could not locate main module information, missing: "+"' from str="+str );
                  return false;
              }
              str = str.substring(0,quote);
              fullName = str;
              shortName = str;
              int slash = fullName.lastIndexOf("\\");
              if(slash<0) slash = fullName.lastIndexOf("/");
              if(slash>0)
                  shortName = str.substring(slash+1);

              str = lines[i++];    // next line is "Entry point: "
              str = lines[i++];   // next lines are of form "0x1234 - 0x5678"
              keyword = " - ";
              while(i<lines.length && str!=null && !str.equals("") )
              {
                 strt = str.indexOf(keyword);
                 if(strt<=0)
                     break;
                 if(startAddress.equals(""))
                 {
                    int x = str.indexOf("0x");
                    if(x>0)
                       startAddress = str.substring(x,strt);
                 }
                 if (_debugSession.getDataAddress().equals(""))
                 {
                    int isData = str.indexOf(" is .data");
                    if(isData>=0)
                    {
                       int x = str.indexOf("0x");
                       if(x>0)
                       _debugSession.setDataAddress( str.substring(x,strt) );
                       if (Gdb.traceLogger.EVT)
                           Gdb.traceLogger.evt(2,"---------------- GetGdbModuleParts.getMainModule _debugSession._dataAddress="+_debugSession.getDataAddress()+" str="+str );
                    }
                 }
                 str = str.substring(strt +keyword.length());
                 int space = str.indexOf(" is ");
                 if(space<=0)
                     break;
                 str = str.substring(0,space);
                 endAddress = str;
                 _debugSession.addLineToUiMessages(str);
                 str = lines[i++];
              }
              if(endAddress.equals(""))
              {
                 if (Gdb.traceLogger.ERR)
                     Gdb.traceLogger.err(2,"GetGdbModuleParts.getMainModule could not locate main module information, missing: "+"endAddress from str="+str );
                 return false;
              }

              // ALL DONE, create the module
              _moduleManager.addModule(shortName, fullName);
              _debugSession.setCurrentModuleID(_moduleManager.getModuleID(shortName) );
              if (Gdb.traceLogger.EVT)
                  Gdb.traceLogger.evt(1,"<<<<<<<<======== GetGdbModuleParts.getMainModule name="+shortName
                      +" start="+startAddress+" end="+endAddress+" fullFileName="+fullName );
              _moduleManager.setModuleStartFinishAddress(shortName,startAddress,endAddress);
              _moduleManager.setModuleDebuggable(shortName,true);
              if (Gdb.traceLogger.EVT)
                  Gdb.traceLogger.evt(2,"GetGdbModuleParts.getMainModule moduleID="+_debugSession.getCurrentModuleID()+" shortName="+shortName+" fullName=" +fullName+" startAddress="+startAddress+" endAddress="+endAddress );
              found = true;
              _debugSession.cmdResponses.removeAllElements();
              return found;
          }
       }
    }
    if (Gdb.traceLogger.ERR)
        Gdb.traceLogger.err(2,"GetGdbModuleParts.getMainModule could not locate main module information, missing: "+keyword );
    return false;
 }



  public boolean getCurrentFileLineModule()
  {
     String currentLineNumber = "";
     String currentFileName = "";
     String currentFunctionName = "";
     String currentFrameAddress = "";
     int    currentModuleID = -1;
     String currentModuleName = "";
     int    currentPartID = -1;

    /*
     String[] lines2 = _gdbProcess.readAllLines();
     for(int i=0; i<lines2.length; i++)
     {  String str2 = lines2[i];
       	System.out.println("****** GetGdbModuleParts.java str2:  *******" + str2);
     }
*/

     String cmd = "info stack 1 ";
     _debugSession.addLineToUiMessages(_debugSession.prefix);
     _debugSession.addLineToUiMessages(_debugSession.prefix+cmd);
     if (Gdb.traceLogger.EVT)
         Gdb.traceLogger.evt(3,"GetGdbModuleParts.getCurrentFileLineModule cmd="+cmd );
     boolean ok = _gdbProcess.writeLine(cmd);
     if(!ok)
         _debugSession.uiMessages.addElement( "GdbProcess failed executing="+cmd );

     String[] lines = _gdbProcess.readAllLines();

     String FILE_keyword        = _gdbProcess.MARKER+"frame-source-file";
     String SOURCE_keyword      = _gdbProcess.MARKER+"source";
     String LINE_keyword        = _gdbProcess.MARKER+"frame-source-line";
     String FUNCTION_keyword    = _gdbProcess.MARKER+"frame-function-name";
     String FRAME_START_keyword = _gdbProcess.MARKER+"frame-begin";
     String FRAME_END_keyword   = _gdbProcess.MARKER+"frame-end";
//     String frame = "";

     StringBuffer frame = new StringBuffer(lines.length);

    //System.out.println("RW ===== GetGdbModuleParts.java -info stack 1- lines.length: " + lines.length);
     for(int i=0; i<lines.length; i++)
//     for(int i=0; i<10; i++)
     {  String str = lines[i];
        if (Gdb.traceLogger.DBG)
            Gdb.traceLogger.dbg(3,". . . . . . . .  GetGdbModuleParts.getCurrentFileLineModule str["+i+"]="+str );
        if( str!=null && !str.equals("") )
        {   if( str.equals(FRAME_END_keyword) )
            {
//                _debugSession.addLineToUiMessages(frame);  //addResponseLines
                _debugSession.addLineToUiMessages(frame.toString());  //addResponseLines
                //frame = "";
                	//System.out.println("RW ****** GetGdbModuleParts.java setLength(0)");
                frame.setLength(0);
            }
            else if( str.startsWith(FRAME_START_keyword) )
            {
                str = str.substring(FRAME_START_keyword.length()+1);
                int space = str.lastIndexOf(" ");
                if(space>0)
                {
                     str = str.substring(space+1);
                     currentFrameAddress = str;
                     _debugSession.addLineToUiMessages("frame-begin "+str);  //addResponseLines
                     currentModuleID = _moduleManager.containsAddress(str);
                     if(currentModuleID == -1)
                        currentModuleID = _moduleManager.getModuleID(_debugSession.getProgramName() );
                     if (Gdb.traceLogger.DBG)
                         Gdb.traceLogger.dbg(2,"GetGdbModuleParts.getCurrentFileLineModule currentModuleID="+currentModuleID );
                }

            }
            else if( str.equals(FILE_keyword) )
            {
                currentFileName = lines[i+1];
                if (Gdb.traceLogger.DBG)
                    Gdb.traceLogger.dbg(2,"GetGdbModuleParts.getCurrentFileLineModule currentFileName="+currentFileName );
            }
            else if( str.startsWith(SOURCE_keyword) )
            {
                int slash = str.lastIndexOf("/");
                if(slash>=0)
                    str = str.substring(slash+1);
                int colon = str.indexOf(":");
                if(colon>0)
                {
                   currentFileName = str.substring(0,colon);
                   str = str.substring(colon+1);
                   colon = str.indexOf(":");
                   if(colon>0)
                   {
                      currentLineNumber =  str.substring(0,colon);
                   }
                }
                if (Gdb.traceLogger.DBG)
                    Gdb.traceLogger.dbg(2,"GetGdbModuleParts.getCurrentFileLineModule currentFileName="+currentFileName+" currentLineNumber="+currentLineNumber  );
            }
            else if( str.equals(LINE_keyword) )
            {
                currentLineNumber = lines[i+1];
            }
            else
            {
                if( !str.startsWith(_gdbProcess.MARKER) )
                {
                	
                  // frame += str;
                   frame.append(str);
                }
                else if( str.startsWith(FUNCTION_keyword) )
                {   currentFunctionName =  lines[i+1];
                    //frame += currentFunctionName;
                    frame.append(currentFunctionName);
                    i++;
                }
            }
        }
     }


     int lineNum = 0;
     if(currentLineNumber!=null && currentLineNumber!="")
        lineNum = Integer.parseInt(currentLineNumber);

 	 // get currentModuleName and currentPartID now for debugSession       
     currentModuleName = _moduleManager.getModuleName(currentModuleID);
     
     // in case there is directory info in the filename, get rid of it
     int lastSlash = currentFileName.lastIndexOf("/");
     if (lastSlash != -1)
     {
     	currentFileName = currentFileName.substring(lastSlash+1);
     }
     
     currentPartID = _moduleManager.getPartID(currentModuleID, currentFileName);   
     
     String lastFunctionName = _debugSession.getCurrentFunctionName();     
     
     // update debug session
     // threadManager.updateThreads() depends on some of this info   
     _debugSession.setCurrentFileLineModule(currentLineNumber, currentFileName, currentFunctionName,
                      currentFrameAddress, currentModuleID, currentModuleName, currentPartID );        

     _threadManager.updateThreads();
     if(_threadManager.getThreads()!=null && _threadManager.getThreads().size()>0 )
     {
        GdbThreadComponent tc = null;
//        try
//        {
            tc = (GdbThreadComponent)_threadManager.getThreadComponent(_threadManager.getStoppingThread() );
            if (Gdb.traceLogger.EVT)
                Gdb.traceLogger.evt(2,"GetGdbModuleParts.getCurrentFileLineModule stoppingThread="+_threadManager.getStoppingThread() );
//        }
//        catch (java.lang.ArrayIndexOutOfBoundsException exc)
//        {
//           if (Gdb.traceLogger.ERR)
//               Gdb.traceLogger.err(2,"INTERNAL_ERROR: GetGdbModuleParts.getCurrentFileLineModule array exception" );
//        }
        if(tc!=null)
        {    tc.setLineNumber(lineNum);
             tc.setModuleID(currentModuleID);

// This is not the right place to set PartID for thread component.
// If the part does not exists (partID=0), add the part and THEN set partID
// for the thread component
//             tc.setPartID(currentPartID);

             Part part = (Part)_moduleManager.getPart(currentPartID);
             if(currentPartID==0 || part==null)
             {
             	// find current part name, add part if it does not already
             	// exists in current module.
                part = checkCurrentPart(currentModuleID);
                if (part != null)
                	currentPartID = part.getPartID();
             }

// update partID for thread component
             tc.setPartID(currentPartID);
             if(part!=null)
             {
                View view = part.getView(1);
                if(view!=null)
                {
                    String name = view.getBaseViewFileName();
                }
             }
             
             if (part != null)
             {
	             if (!lastFunctionName.equals(currentFunctionName))
	             {
	   				String address = ((GdbDebugSession)_debugSession)._getGdbFile.convertSourceLineToAddress(currentFileName,currentLineNumber);
					View tempView = ((GdbPart)part).getView(Part.VIEW_DISASSEMBLY);
					if (tempView.isViewVerify())
					{
		             	if (address != null && !((GdbDisassemblyView)tempView).containsAddressInView(address))
		             	{
		             		((GdbPart)part).setPartVerified(false);
		             		((GdbPart)part).setPartChanged(true);
		             		tempView.setViewVerify(false);
		            		((GdbPart)part).verifyViews();
		             	}             		
					}
	             }
             }
         }
     }

     if (Gdb.traceLogger.EVT)
         Gdb.traceLogger.evt(1,"<<<<<<<<<<<<<<<< GetGdbModuleParts.getCurrentFileLineModule module="+currentModuleName+" moduleID="+currentModuleID+" partID="+currentPartID+" fileName="+currentFileName +" lineNumber="+currentLineNumber );

     return ok;
  }

  public Part isPartInModule(String partName, String moduleName)
  {
     GdbPart part = null;
     int moduleID = _moduleManager.getModuleID(moduleName);
     if(moduleID<=0)
     {   if (Gdb.traceLogger.ERR)
             Gdb.traceLogger.err(2,"ERROR: GetGdbModuleParts.isPartInModule invalid moduleID==0 for moduleName="+moduleName );
         return part;
     }
     int partID = _moduleManager.getPartID(moduleID,partName);
     if(partID>0)
     {   if (Gdb.traceLogger.ERR)
             Gdb.traceLogger.err(2,"ERROR: GetGdbModuleParts.isPartInModule moduleName="+moduleName+" alrady contains part="+partName );
         part = (GdbPart)_moduleManager.getPart(partID);
         return part;
     }

     String cmd = "maintenance print objfiles ";
     boolean ok = _debugSession.executeGdbCommand(cmd);
     if(!ok)
         return part;

     String[] lines = _debugSession.getTextResponseLines();
     if (Gdb.traceLogger.DBG)
         Gdb.traceLogger.dbg(1,"GdbProcess respones lines.length="+lines.length );

     String PRE_PROMPT_keyword   = _gdbProcess.MARKER+"pre-prompt";
     String fullObjFileName = "??????";
     String objPath = "??????";
     String objFile = "??????";
     boolean processingModule = false;

     for(int i=0; i<lines.length; i++)
     {  if(lines[i]!=null && !lines[i].equals("") )
        {
           if( !lines[i].startsWith(_gdbProcess.MARKER))
           {   String str = lines[i];
               String keyword = "Object file ";        // Object file /lib/ld-linux.so.2:  Objfile at 0x8385190, bfd at 0x835e248, 293 minsyms
               if(str.startsWith(keyword))  // 'Object File'
               {
                  if(processingModule)
                  {   if (Gdb.traceLogger.ERR)
                          Gdb.traceLogger.err(2,"GetGdbModuleParts.isPartInModule FAILED to find part="+partName+" in module="+moduleName );
                      return part;
                  }
                  objFile = "??????";
                  fullObjFileName = "??????";
                  objPath = "??????";
                  int colon = str.indexOf(": ");
                  int slash = str.lastIndexOf("/");
                  if(colon>0 && slash>0)
                  {
                     objFile = str.substring(slash+1,colon);
                  }
                  fullObjFileName = str.substring(keyword.length(),colon).trim();
                  objPath = str.substring(keyword.length(),slash+1);
                  if(objPath.startsWith("//"))
                  {
                     objPath = objPath.charAt(2)+":"+objPath.substring(3);
                  }
                  fullObjFileName = objPath+objFile;
                  if(objFile.equals(moduleName))
                  {    processingModule = true;
                       if (Gdb.traceLogger.EVT)
                           Gdb.traceLogger.evt(2,"GetGdbModuleParts.isPartInModule found moduleName="+objFile+" fullName="+fullObjFileName );
                  }
               }

               else if(str.equals(PRE_PROMPT_keyword))
               {
                  break;
               }

               else if(processingModule)
               {
                 while(str.length()>1)   // str of fileNames
                 {
                    int at = str.indexOf(" at ");
                    if( at>0 )
                    {
                       String s = str.substring(0,at);
                       int slash = s.lastIndexOf("/");
                       if(slash<0)
                          slash = s.lastIndexOf("\\");
                       if(slash<0) slash = -1;
                       String fileName = s.substring(slash+1,at);
                       if(slash<0)
                           fileName = fileName.trim();
                       String fullFileName = s.substring(0,at).trim();
                       if(partName.equals(fileName))
                       {
                          if (Gdb.traceLogger.EVT)
                              Gdb.traceLogger.evt(1,"GetGdbModuleParts.isPartInModule found partName="+fileName+" fullPartName="+fullFileName );
                          _moduleManager.addModulePart(objFile,fullObjFileName,fileName,fullFileName);
                          partID = _moduleManager.getPartID(moduleID,fileName);
                          part = (GdbPart)_moduleManager.getPart(partID);
                          if (Gdb.traceLogger.DBG)
                              Gdb.traceLogger.dbg(1,"GetGdbModuleParts.isPartInModule PART moduleID="+moduleID+" partID="+partID +" part="+part.getName()+" isDebuggable="+part.isDebuggable() );
                          if(part!=null && part.isDebuggable() )
                          {
                             Module m = (Module)_moduleManager.getModule(moduleID);
                             m.setIsDebuggable(true);
                             if (Gdb.traceLogger.DBG)
                                 Gdb.traceLogger.dbg(1,"GetGdbModuleParts.isPartInModule setting PART-MODULE "+objFile+" DEBUGGABLE ");
                          }
                          return part;
                      }
                    }

                    int comma = str.indexOf(", ");
                    if(comma>0)
                    {
                        str = str.substring(comma+1);
                    }
                    else
                        str = "";
                  }  // end of while(str>0)
               }  // end of str processing
           }
        }
     }
     if (Gdb.traceLogger.EVT)
         Gdb.traceLogger.evt(1,"GetGdbModuleParts.isPartInModule FAILED to find part="+partName+" in module="+moduleName );
     return part;
  }

  public void updateAllParts()
  {
////////////////////// The code below is not needed if the IDE Famework keeps the list of Files //////////////////////////

     String cmd = "maintenance print objfiles ";
     boolean ok = _debugSession.executeGdbCommand(cmd);
     if(!ok)
         return;

     String PRE_PROMPT_keyword   = _gdbProcess.MARKER+"pre-prompt";

     String[] lines = _debugSession.getTextResponseLines();

     if (Gdb.traceLogger.DBG)
         Gdb.traceLogger.dbg(1,"GdbProcess respones lines.length="+lines.length );
     boolean symtabs = false;
     String fullObjFileName = "??????";
     String objPath = "??????";
     String objFile = "??????";
     for(int i=0; i<lines.length; i++)
     {  if(lines[i]!=null && !lines[i].equals("") )
        {  if( !lines[i].startsWith(_gdbProcess.MARKER))
           {   String str = lines[i];
               String keyword = "Object file ";
               if(str.startsWith("Symtabs:"))
               {
                  symtabs = true;
                  _debugSession.addLineToUiMessages(str);
               }
               else if(str.startsWith(keyword))  // 'Object File'
               {
               	  // new object, set debug availability to false
  				  symtabs = false;	  	           	
                  _debugSession.addLineToUiMessages(str);
                  objFile = "??????";
                  fullObjFileName = "??????";
                  objPath = "??????";
                  symtabs = false;
                  int colon = str.indexOf(": ");
                  int slash = str.lastIndexOf("/");
                  if(colon>0 && slash>0)
                  {
                     objFile = str.substring(slash+1,colon);
                  }
                  fullObjFileName = str.substring(keyword.length(),colon).trim();
                  objPath = str.substring(keyword.length(),slash+1);
                  if(objPath.startsWith("//"))
                  {
                     objPath = objPath.charAt(2)+":"+objPath.substring(3);
                  }
                  fullObjFileName = objPath+objFile;
                  int minsyms = str.indexOf(" minsyms");
                  int comma = str.lastIndexOf(", ");
                  boolean debuggable = false;
                  if(minsyms>0 && comma>0)
                  {
                      String syms = str.substring(comma+2,minsyms);
                      if(!syms.equals("0"))
                         debuggable = true;
                  }
                  if (Gdb.traceLogger.DBG)
                      Gdb.traceLogger.dbg(1,"GetGdbModuleParts.updateAllParts MODULE objFile="+objFile +" debuggable="+debuggable +" fullObjFileName="+fullObjFileName  );
                  _moduleManager.addModule(objFile,fullObjFileName);
                  if(debuggable)
                  {
                      Module m = (Module)_moduleManager.getModule(objFile);
                      m.setIsDebuggable(true);
                      if (Gdb.traceLogger.DBG)
                          Gdb.traceLogger.dbg(1,"GetGdbModuleParts.updateAllParts setting MODULE "+objFile+" DEBUGGABLE ");
                  }


               }
               else if(str.equals(PRE_PROMPT_keyword))
               {
                  break;
               }

               else
               {
                 while(str.length()>1)
                 {
                  int at = str.indexOf(" at ");
                  if(!objPath.startsWith("/lib") && at>0)
                  {
                     String s = str.substring(0,at);
                     int slash = s.lastIndexOf("/");
                     if(slash<0)
                        slash = s.lastIndexOf("\\");
                     if(slash<0) slash = -1;
                     String fileName = s.substring(slash+1,at);
                     if(slash<0)
                         fileName = fileName.trim();
                     String fullFileName = s.substring(0,at).trim();
                     if(symtabs)
                     {
                        {
                          if(fileName.indexOf(".c")>0 || fileName.indexOf(".C")>0 || fileName.indexOf(".cpp")>0 || fileName.indexOf(".CPP")>0)
                          {
                           if(isFile(fileName))           // enable this if only wantto find/show listable files
                           {
                              _debugSession.addLineToUiMessages(str);
                              if (Gdb.traceLogger.DBG)
                                  Gdb.traceLogger.dbg(1,"GetGdbModuleParts.updateAllParts SYMTABS PART fileName="+fileName +" fullFileName="+fullFileName );
                              _moduleManager.addModulePart(objFile,fullObjFileName,fileName,fullFileName);
                              int moduleID = _moduleManager.getModuleID(objFile);
                              int partID = _moduleManager.getPartID(moduleID,fileName);
                              GdbPart part = (GdbPart)_moduleManager.getPart(partID);
                              if (Gdb.traceLogger.DBG)
                                  Gdb.traceLogger.dbg(1,"GetGdbModuleParts.updateAllParts SYMTABS PART moduleID="+moduleID+" partID="+partID +" part="+part.getName()+" isDebuggable="+part.isDebuggable() );
                              if(part!=null && part.isDebuggable() )
                              {
                                  Module m = (Module)_moduleManager.getModule(moduleID);
                                  m.setIsDebuggable(true);
                                  if (Gdb.traceLogger.DBG)
                                      Gdb.traceLogger.dbg(1,"GetGdbModuleParts.updateAllParts setting SYMTABS PART-MODULE "+objFile+" DEBUGGABLE ");
                              }
                           }
/*
                           else
                           {
                              if (Gdb.traceLogger.EVT)
                                  Gdb.traceLogger.evt(2,"#### GetGdbModuleParts.updateAllParts FORCING (cannot list) SYMTABS PART="+fileName );
                              _moduleManager.addModulePart(objFile,fullObjFileName,fileName,fullFileName);

                              symtabs = false;
                              int moduleID = _moduleManager.getModuleID(objFile);
                              int partID = _moduleManager.getPartID(moduleID,fileName);
                              if(partID>0)
                              {
                                if (Gdb.traceLogger.DBG)
                                    Gdb.traceLogger.dbg(1,"GetGdbModuleParts.updateAllParts SYMTABS PART moduleID="+moduleID+" partID="+partID+" partName="+fileName );
                              }
                           }
*/
                          }
                        }
                     }

/*
                     else   // !symtabs
                     {
                       if(fileName.indexOf(".c")>0 || fileName.indexOf(".C")>0 || fileName.indexOf(".cpp")>0 || fileName.indexOf(".CPP")>0)
                       {
                         //if(isFile(fileName))
                           {
                              if (Gdb.traceLogger.DBG)
                                  Gdb.traceLogger.dbg(1,"GetGdbModuleParts.updateAllParts PartialSymtab NON_SYMTABS PART fileName="+fileName +" fullFileName="+fullFileName );
                              _moduleManager.addModulePart(objFile,fullObjFileName,fileName,fullFileName);
                              //symtabs = true;
                              int moduleID = _moduleManager.getModuleID(objFile);
                              int partID = _moduleManager.getPartID(moduleID,fileName);
                              if (Gdb.traceLogger.DBG)
                                  Gdb.traceLogger.dbg(1,"GetGdbModuleParts.updateAllParts PartialSymtab NON_SYMTABS PART moduleID="+moduleID+" partID="+partID  );
                              GdbPart part = (GdbPart)_moduleManager.getPart(partID);
                              if(part!=null && part.isDebuggable() )
                              {
                                  GdbModule m = (GdbModule)_moduleManager.getModule(moduleID);
                                  m.setIsDebuggable(true);
                                  if (Gdb.traceLogger.DBG)
                                      Gdb.traceLogger.dbg(1,"GetGdbModuleParts.updateAllParts setting PartialSymtab NON_SYMTABS MODULE-PART "+objFile+" DEBUGGABLE ");
                              }
                           }
//
                           else
                           {

                               if (Gdb.traceLogger.EVT)
                                   Gdb.traceLogger.evt(2,"#### GetGdbModuleParts.updateAllParts PartialSymtab FORCING NON_SYMTABS '.c' PART="+fileName+" MODULE="+objFile );
                              _moduleManager.addModulePart(objFile,fullObjFileName,fileName,fullFileName);

                              int moduleID = _moduleManager.getModuleID(objFile);
                              int partID = _moduleManager.getPartID(moduleID,fileName);
                              if(partID>0)
                              {
                                 //GdbPart part = (GdbPart)_moduleManager.getPart(partID);
                                 if (Gdb.traceLogger.DBG)
                                     Gdb.traceLogger.dbg(1,"GetGdbModuleParts.updateAllParts PartialSymtab PART moduleID="+moduleID+" partID="+partID+" partName="+fileName );
                              }
                           }
//
                        }
                     }
*/
                  }
                  int comma = str.indexOf(", ");
                  if(comma>0)
                  {
                      str = str.substring(comma+1);
                  }
                  else
                      str = "";
                 } // end of while(str)
               } // end of if(symtabs)
           }
        }
     }
     _debugSession.cmdResponses.removeAllElements();
  }

  public Part checkCurrentPart(int moduleID)
  {
     if(moduleID<=0)
     {
        if (Gdb.traceLogger.ERR)
            Gdb.traceLogger.err(2,"GetGdbModuleParts.checkCurrentPart moduleID<=0" );
        return null;
     }

     if (Gdb.traceLogger.EVT)
         Gdb.traceLogger.evt(1,"========>>>>>>>> GetGdbModuleParts.checkCurrentPart" );

     String cmd = "info source ";
     boolean ok = _debugSession.executeGdbCommand(cmd);
     if(!ok)
         return null;

     String[] lines = _debugSession.getTextResponseLines();

     String keyword = "Current source file is ";

	 if (lines.length <= 0)
	 {
         if (Gdb.traceLogger.ERR)
             Gdb.traceLogger.err(2,"GetGdbModuleParts.checkCurrentPart was expecting str="+keyword+", received nothing" );
         return null;
	 }
	 
     String str = lines[0];
     if(!str.startsWith(keyword))
     {
         if (Gdb.traceLogger.ERR)
             Gdb.traceLogger.err(2,"GetGdbModuleParts.checkCurrentPart was expecting str="+keyword+", received str="+str );
         return null;
     }
     str = str.substring( keyword.length() );
     String fileName = str;

     // "Compilation directory is "
     keyword = "Located in ";
     str = lines[2];
     String fullFileName;
     if(!str.startsWith(keyword))
     {
         if (Gdb.traceLogger.ERR)
             Gdb.traceLogger.err(2,"GetGdbModuleParts.checkCurrentPart was expecting str="+keyword+", received str="+str );
//         return null;
		 fullFileName = fileName;
     }
     else
     {
	     str = str.substring( keyword.length() );
	     fullFileName = str;
     }    

// SAM:  not sure what it is doing here... this does not make sense
/*
     //Gdb on Windows allows list mainFile as the current file, regardless of step-into module location.
     Part mainPart = _moduleManager.getModule(1).getPart(1);
     if(mainPart!=null)
     {
        String mainFile = mainPart.getPartName();
        if(mainFile.equals(fileName))
        {
            if (Gdb.traceLogger.EVT)
                Gdb.traceLogger.evt(3,"GetGdbModuleParts.checkCurrentPart IGNORING fileName="+fileName+" == mainFile="+mainFile );
            return mainPart;
        }
     }

     if (Gdb.traceLogger.ERR)
         Gdb.traceLogger.err(3,"???????????????? GetGdbModuleParts.checkCurrentPart ?????? IGNORING fullPartName="+fullFileName+" ???????????????????????????" );
     Part part = _moduleManager.addPart(moduleID, fileName, fileName);
*/
	_moduleManager.checkPart(moduleID, fileName);
	
	// even though the part is created with path info, when querying for it,
	// we only need the filename
	
	int slash = fileName.lastIndexOf("/");
	if (slash != -1)
	{
		fileName = fileName.substring(slash+1);
	}
	
	int id = _moduleManager.getPartID(moduleID, fileName);
	Part part = _moduleManager.getPart(id);

     str = "";
     int partID = 0;
	 if(part!=null)     
     {   
     	 String directory="./";
     	 slash = fullFileName.lastIndexOf("/");
     	 if (slash != -1)
     	 	directory = fullFileName.substring(0, slash+1);
     	 	
     	 part.setFilePath(directory);
     	 	
     	 str=part.getName();
         partID = part.getPartID();
     }
     if (Gdb.traceLogger.EVT)
         Gdb.traceLogger.evt(1,"<<<<<<<<======== GetGdbModuleParts.checkCurrentPart partID="+partID+" partName="+str+" fullPartName="+fullFileName );

     return part;
  }

  public boolean isFile(String fileName)
  {
     String cmd = "list "+fileName+":2";
     boolean ok = _gdbProcess.writeLine(cmd);
     if(!ok)
         return false;

     String[] lines = _gdbProcess.readAllLines();

     String keyword1 = "No source file named ";
     String keyword2 = "is out of range for ";
     String keyword3 = "No such file ";
     String PRE_PROMPT_keyword = _gdbProcess.MARKER+"pre-prompt";
     for(int i=0; i<lines.length; i++)
     {  if(lines[i]!=null && !lines[i].equals("") )
        {
           if(lines[i].equals(PRE_PROMPT_keyword))
           {   i++;i++;
               continue;
           }
           if (Gdb.traceLogger.DBG)
               Gdb.traceLogger.dbg(2,"GetGdbModuleParts.isFile fileName="+fileName+" str="+lines[i] );
           if( !lines[i].startsWith(_gdbProcess.MARKER))
           {
              if( !lines[i].startsWith("1") || lines[i].indexOf(keyword2)>0 || lines[i].indexOf(keyword3)>0 )// || lines[i].startsWith(keyword1) )
              {
                 if (Gdb.traceLogger.EVT)
                     Gdb.traceLogger.evt(3,"GetGdbModuleParts.isFile IGNORING '"+cmd+"' response="+lines[i] );
                 return false;
              }
              else
              {  if (Gdb.traceLogger.EVT)
                     Gdb.traceLogger.evt(3,"GetGdbModuleParts.isFile ACCEPTING '"+cmd+"' response="+lines[i] );
                 return true;
              }
           }
        }
     }
     return true;
  }

}
