package com.ibm.debug.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/util/ExecCommand.java, java-util, eclipse-dev, 20011128
// Version 1.17.1.2 (last modified 11/28/01 16:32:51)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public class ExecCommand
{
  private static String cmd = "";
  private static String[] cmdArray = null;
  private static String windowTitle = "ExecCommand Title";

  public ExecCommand(String cmd, String title, String binDirectory)
  {  if(title != null)
        windowTitle = title;
     else
        windowTitle = "ExecCommand" ;
     cmdArray = buildShellCommand(cmd, binDirectory, true);
  }

  public ExecCommand(String cmd, String title, String binDirectory, boolean iconic)
  {  if(title != null)
        windowTitle = title;
     else
        windowTitle = "ExecCommand" ;
     cmdArray = buildShellCommand(cmd, binDirectory, iconic);
  }

  //javaPICLEngineInfo.java
  private static String[] buildShellCommand(String cmd, String binDirectory, boolean iconic)
  {
    String[] shellCommand = null;

    if ( Platform.isNT() || Platform.isWin2000())
    {
       shellCommand = new String[3];

       shellCommand[0] = "cmd";
       shellCommand[1] = "/c";

       if (windowTitle != null)
          shellCommand[2] = "start \"" + windowTitle + "\" " +cmd;
       else
          shellCommand[2] = "start " +cmd;
    }
    else
    if ( Platform.is95() || Platform.is98()  )
    {
       shellCommand = new String[1];

       shellCommand[0] = "command " +cmd;
    }
    else
    if ( Platform.isAIX() || Platform.isLinux() || Platform.isSCO() || Platform.isSolaris() || Platform.isHPUX() || Platform.isOS390() )
    {
       shellCommand = new String[3];

       shellCommand[0] = "/bin/sh";
       shellCommand[1] = "-c";


          String termType = findTerm() ;

          shellCommand[2] = "_LIBPATH=$LIBPATH; export _LIBPATH;" + termType;
          if (termType.indexOf("dtterm") != -1)
               shellCommand[2] += " -title \"" + windowTitle + "\" ";
          else
               shellCommand[2] += " -T \"" + windowTitle + "\" ";
          if (iconic == true) {
            if (termType.startsWith("aixterm"))
               shellCommand[2] += " -i " ;
            else
               shellCommand[2] += " -iconic" ;

          shellCommand[2] += " -n \"" + windowTitle + "\" " ;
         }

         shellCommand[2] += " -e " ;
         if (Platform.isAIX() )
             shellCommand[2] += binDirectory +"derdsetlp " ;
         shellCommand[2] += cmd;
    }
    else
    if ( Platform.isOS2() )
    {
       shellCommand = new String[1];

       shellCommand[0] = "cmd /c start /c " +cmd;
    }

    return shellCommand;
  }

  public static String findTerm()
  {
   Process p = null;
   try {
     /* try dtterm */
     p = Runtime.getRuntime().exec("dtterm -h ");
     return new String("dtterm")  ;
   } catch(SecurityException excp)   {}
     catch(java.io.IOException excp) {}
   try {
     /* try dtterm from another location */
     p = Runtime.getRuntime().exec("/usr/dt/bin/dtterm -h ");
     return new String("/usr/dt/bin/dtterm")  ;
   } catch(SecurityException excp)   {}
     catch(java.io.IOException excp) {}
   try{
     p = Runtime.getRuntime().exec("aixterm -h ");
     return new String("aixterm")  ;
   } catch(SecurityException excp)   {}
     catch(java.io.IOException excp) {}
   try{
     p = Runtime.getRuntime().exec("xterm -h ");
     return new String("xterm")  ;
   } catch(SecurityException excp)   {excp.printStackTrace();}
     catch(java.io.IOException excp) {excp.printStackTrace();}
     System.exit(-1);
     return null ;
  }
  public static String getCommand()
  {   String s = "ExecCommand cmd=";
      for (int i=0; i<cmdArray.length; i++)
         s += cmdArray[i]+" ";
      return s;
  }

  //localHost.java
  public static Process getProcess()
  {   return _proc;
  }
  public static Process _proc = null;
  public static boolean execute()
  {
    try
    {  if (TraceLogger.TRACE.EVT)
       {   for (int i = 0; i < cmdArray.length; i++)
           {   TraceLogger.TRACE.evt(1,
               "   cmdArray[" + i + "]=" + cmdArray[i]);
           }
       }
       _proc = Runtime.getRuntime().exec(cmdArray);
       return true;
    }
    catch(SecurityException excp)
    {  if (TraceLogger.TRACE.ERR) TraceLogger.TRACE.err(
           1, "##### ExecCommand.execute SecurityException="+excp);
       return false;
    }
    catch(java.io.IOException excp)
    {  if (TraceLogger.TRACE.ERR) TraceLogger.TRACE.err(
           1, "##### ExecCommand.execute IOException="+excp);
       return false;
    }
  }

  public static String[] getShellCommand()
  {
      return cmdArray;
  }

}
