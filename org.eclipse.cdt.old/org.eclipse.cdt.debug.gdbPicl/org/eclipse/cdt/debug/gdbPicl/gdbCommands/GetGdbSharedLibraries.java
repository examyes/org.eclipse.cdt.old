//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.gdbCommands;

import  com.ibm.debug.gdbPicl.*;
import  com.ibm.debug.gdbPicl.gdbCommands.GdbProcess;

/**
 * gets Gdb Threads
 */
public class GetGdbSharedLibraries  //extends ThreadManager
{
   private static final int MAX_SHARED_LIBRARIES = 30;
   public class ModuleInfo 
   { public String name = null;
     public String fullFileName = null;
     public String startAddress = null;
     public String endAddress = null;
   }
   ModuleInfo[] _moduleInfo = null;

   GdbProcess       _gdbProcess    = null;
   GdbDebugSession  _debugSession  = null;
   GdbModuleManager _moduleManager = null;

  /**
   * Create a new GetGdbSharedLibraries. command object
   */
   public GetGdbSharedLibraries(GdbDebugSession gdbDebugSession)
   {
     _debugSession = gdbDebugSession;
     _gdbProcess = _debugSession.getGdbProcess();
   }

  public ModuleInfo[] updateSharedLibraries()
  {
     _moduleInfo = new ModuleInfo[MAX_SHARED_LIBRARIES];
     int indx = -1;

     if (Gdb.traceLogger.DBG) 
         Gdb.traceLogger.dbg(1,"GetGdbSharedLibraries.updateSharedLibraries" );

     String cmd = "info sharedlibrary ";
     boolean ok = _debugSession.executeGdbCommand(cmd);
     if(!ok)
         return _moduleInfo;
 
     String[] lines = _debugSession.getTextResponseLines();
     Gdb.debugOutput("GdbProcess respones lines.length="+lines.length );

     String fullObjFileName = "??????";
     String objPath = "??????";
     String objFile = "??????";
     boolean NT = false;
     int maxNTAddresses=0;
     int[] addresses = {0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0,0 };
     String PRE_PROMPT_keyword   = _gdbProcess.MARKER+"pre-prompt";
     if(lines!=null && lines.length>0)
        lines[lines.length-1] = null; // remove trailing "(gdb)" prompt
     if(lines.length>=3)  // colum headings, 1+ library names, blank, prompt
     for(int i=0; i<lines.length; i++)
     {  
        if(lines[i]!=null && !lines[i].equals("") )
        {  if( !lines[i].startsWith(_gdbProcess.MARKER))
           {   String str = lines[i];
               _debugSession.addLineToUiMessages(str);
               if (Gdb.traceLogger.EVT) 
                   Gdb.traceLogger.evt(3,"GetGdbSharedLibraries.getSharedLibraries str="+str );
               objFile = "??????";  
               fullObjFileName = "??????";
               objPath = "??????";
               if(str.startsWith("From") || str.startsWith("DLL Name"))
               {  
                  _debugSession.addLineToUiMessages(str);
                  if(str.startsWith("DLL Name"))
                  {   NT = true;
                      int j=i; 
                      while (j<lines.length-1) // read in all 'k' load addresses
                      {  String s = lines[j];
                         if(s==null || s.equals(""))
                            break;
                         int space = s.lastIndexOf(" ");
                         String a = s.substring(space+1);
                         int address = 0;  
                         try { address = Integer.parseInt(a,16); } catch(java.lang.NumberFormatException exc) {}
                         addresses[maxNTAddresses++] = address;
                         lines[j++] = "0x"+a +" 0x"+a +" Yes  "+s.substring(0,space).trim();  // converts NT format into Unix format
                         if(maxNTAddresses>=addresses.length)
                         {  if (Gdb.traceLogger.ERR) 
                                Gdb.traceLogger.err(2,"GetGdbSharedLibraries.getSharedLibraries NT exceeds internal array length="+addresses.length );
                             break;
                         }
                      }
                  }
                  continue;
               }
               if (Gdb.traceLogger.DBG) 
                   Gdb.traceLogger.dbg(2,"GetGdbSharedLibraries.getSharedLibraries str="+str );
               if(!str.startsWith("0x"))
               {  if (Gdb.traceLogger.ERR) 
                      Gdb.traceLogger.err(2,"GetGdbSharedLibraries.getSharedLibraries missing starting '0x' from startAddress of str="+str );
                  continue;
               }

               int x = str.indexOf("0x");
               if(x<0)
               {  if (Gdb.traceLogger.ERR) 
                      Gdb.traceLogger.err(2,"GetGdbSharedLibraries.getSharedLibraries missing '0x' startAddress in str="+str );
                  continue;
               }
               str = str.substring(x);
               int space = str.indexOf(" ");
               if(space<=0)
               {  if (Gdb.traceLogger.ERR) 
                      Gdb.traceLogger.err(2,"GetGdbSharedLibraries.getSharedLibraries missing ' ' after startAddress in str="+str );
                  continue;
               }
               String start = str.substring(0,space);
               str = str.substring(space+1);

               x = str.indexOf("0x");
               if(x<0)
               {  if (Gdb.traceLogger.ERR) 
                      Gdb.traceLogger.err(1,"GetGdbSharedLibraries.getSharedLibraries missing '0x' endAddress in str="+str );
                  continue;
               }
               str = str.substring(x);
               space = str.indexOf(" ");
               if(space<=0)
               {  if (Gdb.traceLogger.ERR) 
                      Gdb.traceLogger.err(2,"GetGdbSharedLibraries.getSharedLibraries missing ' ' after startAddress in str="+str );
                  continue;
               }
               String end = str.substring(0,space);
               str = str.substring(space+1);

               x = str.lastIndexOf(" ");
               if(x<0)
               {  if (Gdb.traceLogger.ERR) 
                      Gdb.traceLogger.err(2,"GetGdbSharedLibraries.getSharedLibraries missing ' XXXXXX' moduleName in str="+str );
                  continue;
               }
               str = str.substring(x+1);
               String moduleName = str;

               int slash = str.lastIndexOf("/");
               objPath = str.substring(0,slash+1);
               objFile = str.substring(slash+1);
               fullObjFileName = objPath+objFile;
               if(NT)
               {  try
                  {  int strt = Integer.parseInt(end.substring(2),16);
                     int tmp = Integer.MAX_VALUE;
                     for(int l=0; l<maxNTAddresses; l++)
                     {  if( addresses[l] > strt && addresses[l] < tmp )
                        {   tmp = addresses[l];
                        }
                     }
                     end = "0x"+Integer.toHexString(tmp-1);
                  }       
                  catch(java.lang.NumberFormatException exc) {}
               }
               if (Gdb.traceLogger.EVT) 
                   Gdb.traceLogger.evt(2,"GetGdbSharedLibraries.getSharedLibraries MODULE objFile="+objFile +" fullObjFileName="+fullObjFileName +" start="+start+" end="+end );
               if(indx++ >= MAX_SHARED_LIBRARIES)
               {
                  if (Gdb.traceLogger.ERR) 
                      Gdb.traceLogger.err(2,"GetGdbSharedLibraries.getSharedLibraries too many sharedLibraries="+indx );
                  return _moduleInfo;
               }
               if(_moduleInfo==null)
               {
                  if (Gdb.traceLogger.ERR) 
                      Gdb.traceLogger.err(2,"GetGdbSharedLibraries.getSharedLibraries _moduleInfo==null" );
                  return _moduleInfo;
               }
               if(_moduleInfo[indx]==null)
               {
                  if (Gdb.traceLogger.ERR) 
                      Gdb.traceLogger.err(2,"GetGdbSharedLibraries.getSharedLibraries _moduleInfo["+indx+"]==null" );
                  _moduleInfo[indx] = new ModuleInfo(); //return _moduleInfo;
               }
               _moduleInfo[indx].name = objFile;
               _moduleInfo[indx].fullFileName = fullObjFileName;
               _moduleInfo[indx].startAddress = start;
               _moduleInfo[indx].endAddress = end;

           }  // end-of line!=marker
           else if( lines[i].equals(PRE_PROMPT_keyword))
           {
              break;
           }
        }  // end-of line!=null
     }  // end-of for lines
     _debugSession.cmdResponses.removeAllElements(); 
     return _moduleInfo;
  }

}
