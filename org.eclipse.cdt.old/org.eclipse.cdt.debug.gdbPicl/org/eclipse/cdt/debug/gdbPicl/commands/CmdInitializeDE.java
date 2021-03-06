/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

import java.io.*;
import com.ibm.debug.epdc.*;
import java.util.*;

/**
 * Processes Initialize Debugger Engine command.
 */

// Note:  The locale sent to us from the front end will be honored if possible
// Otherwise, we will fall back to en_US with a message saying so.  If that
// fails, we return with an error and the front end should terminate the
// connection.

public class CmdInitializeDE extends Command
{
   public CmdInitializeDE(DebugSession debugSession, EReqInitializeDE req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Compares the front-end version with the back-end version.
    * If they are not equal, causes debug engine to quit.
    */
   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      String message = null;
      int returnCode = EPDC.ExecRc_OK;

      _EPDCSession = EPDCSession;

      byte frontendEncoding = _req.debugFrontendEncoding();
      String locale   = _req.locale();

      if (frontendEncoding != EPDC.StrEncode_UTF8)
      {
         returnCode = EPDC.ExecRc_Error;
      }

      DebugEngine _debugEngine = _debugSession.getDebugEngine();
      if (!_debugEngine.setLocale(Gdb.localeFromString(locale)))
      {
         message = "Locale \""+locale+"\" not supported by debug engine. Switching to \"en_US\"";
         if (!_debugEngine.setLocale(Gdb.localeFromString("en_US")))
         {
            // We can't go on from here.
            returnCode = EPDC.ExecRc_Error;
            message = "Locale \"en_US\" not supported.  Exiting.";
         }
      }

      // Note: We can't construct a reply before we've initialized the session
      initializeSession(EPDCSession,_debugSession);

      _rep = new ERepInitializeDE(_EPDCSession);
      _rep.setReturnCode(returnCode);
      ((ERepInitializeDE)_rep).setRepNames(_repNames);
      if (message != null)
      {
         _rep.setMessage(message);
      }

      return false;
   }

   void initializeSession(EPDC_EngineSession _EPDCSession, DebugSession debugSession)
   {
      int i;

// ***************************************************************************
//    Set up EPDC Session information
// ***************************************************************************
      _EPDCSession._debugEngineID         = EPDC.BE_TYPE_PICL;

      // The platform is really JVM, but version7 UIs will fail if
      // PLATFORM_ID_JVM is sent.  Await resolution: see defect 9352.
//      _EPDCSession._debugEnginePlatformID = EPDC.PLATFORM_ID_JVM;
	if (System.getProperty("os.name").equals("AIX"))
	      _EPDCSession._debugEnginePlatformID = EPDC.PLATFORM_ID_AIX;
	else      
	      _EPDCSession._debugEnginePlatformID = EPDC.PLATFORM_ID_LINUX;

      _EPDCSession._defaultSettings       = EPDC.DebuggerBusyBoxEnable;
      _EPDCSession._processDetachAction   = EPDC.ProcessRelease;

// ***************************************************************************
//    Set up views
// ***************************************************************************
      _EPDCSession._viewInfo    = new ERepGetViews[Part.NUM_VIEWS];
      _EPDCSession._viewInfo[Part.VIEW_SOURCE-1] =
         new ERepGetViews(EPDC.Viewtype_prefix, EPDC.View_Class_Source,
                          _debugSession.getResourceString("SOURCE_TEXT"),
                          (byte) (EPDC.LINEBP_CAPABLE | EPDC.MONITOR_CAPABLE));

      _EPDCSession._viewInfo[Part.VIEW_DISASSEMBLY-1] =
         new ERepGetViews(EPDC.Viewtype_prefix, EPDC.View_Class_Disasm,
                         _debugSession.getResourceString("DISASSEMBLY_TEXT"),
                         (byte) (EPDC.LINEBP_CAPABLE | EPDC.MONITOR_CAPABLE));

	  // if mixed view is enabled	
	  if (Part.MIXED_VIEW_ENABLED)
	  {
		 _EPDCSession._viewInfo[Part.VIEW_MIXED-1] =
         new ERepGetViews(EPDC.Viewtype_prefix, EPDC.View_Class_Mixed,
                         _debugSession.getResourceString("MIXED_TEXT"),
                          (byte) (EPDC.LINEBP_CAPABLE | EPDC.MONITOR_CAPABLE));
	  }

// ***************************************************************************
//    Default Representations for Java types.
// ***************************************************************************
      _EPDCSession._repInfo = new ERepTypesNumGet();

      short[] reps = { 	Gdb.REPINDEX_DEFAULT, 
      						Gdb.REPINDEX_DECIMAL,
      						Gdb.REPINDEX_HEXADECIMAL,
      						Gdb.REPINDEX_OCTAL,
      						Gdb.REPINDEX_BINARY};
      _EPDCSession._repInfo.addTypesAndReps(
         new ERepTypesAndRepsGetNext(Gdb.TYPEINDEX_DEFAULT, "Default_Type",
               Gdb.REPINDEX_DEFAULT,reps));

      // Should also add the following types some time in the future
      // "Class Reference"
      // "Interface Reference"
      // "Array Reference"

// ***************************************************************************
//    Data representations.
// ***************************************************************************

      _repNames = new String[6];

      _repNames[Gdb.REPINDEX_DEFAULT]       ="Default";
      _repNames[Gdb.REPINDEX_DECIMAL]       ="Decimal";
      _repNames[Gdb.REPINDEX_HEXADECIMAL]   ="Hex";
      _repNames[Gdb.REPINDEX_OCTAL]         ="Octal";
      _repNames[Gdb.REPINDEX_BINARY]        ="Binary";

      _EPDCSession._languageInfo = new ERepGetLanguages[1];
      _EPDCSession._languageInfo[0] =
          new ERepGetLanguages(EPDC.LANG_CPP, "Cpp");

// ***************************************************************************
//    Exception info
// ***************************************************************************
      String[] exceptionNames = ((GdbDebugSession)_debugSession)._gdbExceptions.getExceptionNames();
      int[]   exceptionStatus = ((GdbDebugSession)_debugSession)._gdbExceptions.getExceptionStatus();
      _EPDCSession._exceptionsInfo =
         new ERepGetExceptions[exceptionNames.length];

      for (i=0;i < exceptionNames.length; i++)
      {
          if (Gdb.traceLogger.EVT) 
              Gdb.traceLogger.evt(3,"CmdInitializeDE.initializeSession i="+i+" exception="+exceptionNames[i]+" exceptionStatus="+exceptionStatus[i] );
          _EPDCSession._exceptionsInfo[i] =
             new ERepGetExceptions(exceptionStatus[i], exceptionNames[i]);
      }

// ***************************************************************************
//    Function customization table
// ***************************************************************************
      _EPDCSession._functCustomTable = new EFunctCustTable(
         // Startup options
//         EPDC.FCT_DEBUG_APPLICATION_INIT | // support debugging app init
         0,

         // General Functions
         EPDC.FCT_MULTIPLE_THREADS       | // support multiple threads
//       EPDC.FCT_FILE_PATH_AVAILABLE    | // support file path
//       EPDC.FCT_FILE_LIST_AVAILABLE    | // support file list
		 EPDC.FCT_INCLUDE_FILES			 | // support stepping into headers
//       EPDC.FCT_CHILD_PROCESSES        | // support child processes
//       EPDC.FCT_PROCESS_LIST_STARTUP   | // support process list startup
//         EPDC.FCT_STARTUP                | // support startup dialog
         0,

         // File Options
//         EPDC.FCT_MODULE_ADD             | // support addition of modules
//       EPDC.FCT_MODULE_REMOVE          | // support removal of modules
         EPDC.FCT_PROCESS_ATTACH         | // support attach to process
         EPDC.FCT_PROCESS_DETACH         | // support detach from process
         EPDC.FCT_PROCESS_DETACH_RELEASE | // support detach release from process
//         EPDC.FCT_FILE_RESTART           | // support restart of program
         EPDC.FCT_LOCAL_SOURCE_FILES     | // support local src files
//         EPDC.FCT_CHANGE_SOURCE_FILE     | // support changing source file
         0,

         // Storage Options
//         EPDC.FCT_STORAGE_ENABLE_TOGGLE      | // support storage monitor enable/disable
//         EPDC.FCT_STORAGE_EXPR_ENABLE_TOGGLE | // support storage monitor expr enable/disable
//         EPDC.FCT_STORAGE_CONTENT_ASCII      | // support ASCII
//         EPDC.FCT_STORAGE_CONTENT_CHAR       | // support character (native code page, DBCS, etc)
		   EPDC.FCT_STORAGE_CONTENT_HEX_CHAR   |
//         EPDC.FCT_STORAGE_CONTENT_16INT      | // support 16-bit int
//         EPDC.FCT_STORAGE_CONTENT_16UINT     | // support 16-bit uint
//         EPDC.FCT_STORAGE_CONTENT_32INT      | // support 32-bit int
//         EPDC.FCT_STORAGE_CONTENT_32UINT     | // support 32-bit uint
//         EPDC.FCT_STORAGE_CONTENT_64INT      | // support 64-bit int
//         EPDC.FCT_STORAGE_CONTENT_64UINT     | // support 64-bit uint
//         EPDC.FCT_STORAGE_CONTENT_32FLOAT    | // support 32-bit float
//         EPDC.FCT_STORAGE_CONTENT_64FLOAT    | // support 64-bit float
//         EPDC.FCT_STORAGE_CONTENT_16PTR      | // support 16-bit ptrs
//         EPDC.FCT_STORAGE_CONTENT_32PTR      | // support 32-bit prs
//         EPDC.FCT_STORAGE_CONTENT_64PTR      | // support 64-bit ptr
//         EPDC.FCT_STORAGE_CONTENT_16INTHEX   | // support 16-bit hex
//         EPDC.FCT_STORAGE_CONTENT_32INTHEX   | // support 32-bit hex
//         EPDC.FCT_STORAGE_CONTENT_64INTHEX   | // support 16-bit hex
         0,

         // Breakpoint Options
         EPDC.FCT_LINE_BREAKPOINT           | // support line bkpts
//       EPDC.FCT_STATEMENT_BREAKPOINT      | // support stmt bkpts
//         EPDC.FCT_FUNCTION_BREAKPOINT       | // support function bkpts
         EPDC.FCT_ADDRESS_BREAKPOINT        | // support address bkpts
//         EPDC.FCT_CHANGE_ADDRESS_BREAKPOINT | // support chg addr bkpts
//       EPDC.FCT_BREAKPOINT_MONITOR_8BYTES | // support monitor 8 bytes
//       EPDC.FCT_BREAKPOINT_MONITOR_4BYTES | // support monitor 4 bytes
//       EPDC.FCT_BREAKPOINT_MONITOR_2BYTES | // support monitor 2 bytes
//       EPDC.FCT_BREAKPOINT_MONITOR_1BYTES | // support monitor 1 bytes
         EPDC.FCT_LOAD_BREAKPOINT           | // support load bkpts
         EPDC.FCT_BREAKPOINT_ENABLE_TOGGLE  | // support enablemt bkpts
         EPDC.FCT_BREAKPOINT_MODIFY         | // support modify bkpts
//       EPDC.FCT_BREAKPOINT_NO_THREADS     | // do not support thread specific breakpoints
         EPDC.FCT_BREAKPOINT_NO_FREQUENCY   | // do not support frequency conditions
//         EPDC.FCT_BREAKPOINT_EXPRESSION     | // conditional expressions
         EPDC.FCT_BREAKPOINT_DEFERRED       | // support deferred bkpts
         0,

         // Monitor Options
         EPDC.FCT_MONITOR_ENABLE_TOGGLE
         ,

         // Windows Options
         EPDC.FCT_LOCAL_VARIABLES           | // support local variables
         EPDC.FCT_STACK                     | // support stack
         EPDC.FCT_REGISTERS                 | // support registers
         EPDC.FCT_STORAGE                   | // support storage
         EPDC.FCT_COMMAND_LOG               | // support commandLog
         EPDC.FCT_PROGRAMIO					|
         0,

         // Run Options
         EPDC.FCT_THREAD_ENABLED      | // support thread enablement
         EPDC.FCT_STEP_INTO           | // support step into
         EPDC.FCT_STEP_OVER           | // support step over
//       EPDC.FCT_STEP_DEBUG          | // support step debug
         EPDC.FCT_STEP_RETURN         | // support step return
         EPDC.FCT_RUN_TO_LOCATION     | // support run to location
         EPDC.FCT_JUMP_TO_LOCATION    | // support step to location
         EPDC.FCT_HALT                | // support run halting
//       EPDC.FCT_STORAGE_USAGE_CHECK | // support storage usage check
         0,

         // Exception Options
         //Exception Retry functionality is not supported. This means the
         //FCT_EXCEPTION_EXAMINE bit must be disabled until this functionality
         //is supported. 
         EPDC.FCT_EXCEPTION_EXAMINE |  // support examine-retry
         EPDC.FCT_EXCEPTION_FILTER  |  // support exception filtering
//         EPDC.FCT_EXCEPTION_STEP    |  // support step exception
         EPDC.FCT_EXCEPTION_RUN     |  // support run exception
         0,

         // Stack Options
         0);

   }

   String nlsCode()
   {
      String locale = _req.locale();

      if (locale == null)
         locale = "en_US";

      locale = locale.toUpperCase();

      String language = "";
      String region = "";

      if (locale.length() >= 2)
         language = locale.substring(0,2);
      if (locale.length() >= 5)
         region   = locale.substring(3,5);

      String nlsCode = "";

      /* stolen from ecnprod.cpp */
      if (language.equals("JA"))
         nlsCode = "j";
      else if (language.equals("ZH") && region.equals("TW"))
         nlsCode = "t";
      else if (language.equals("ZH") && region.equals("CN"))
         nlsCode = "c";
      else if (language.equals("DE"))
         nlsCode = "g";
      else if (language.equals("FR"))
         nlsCode = "f";
      else if (language.equals("IT"))
         nlsCode = "i";
      else if (language.equals("ES"))
         nlsCode = "e";
      else
         nlsCode = "";

      return nlsCode;
   }


   // Data members
   private EReqInitializeDE   _req;
   private EPDC_EngineSession _EPDCSession;
   private String[] _repNames;

   final static String DEBUGGER_RELEASE_NUMBER = "1";
}
