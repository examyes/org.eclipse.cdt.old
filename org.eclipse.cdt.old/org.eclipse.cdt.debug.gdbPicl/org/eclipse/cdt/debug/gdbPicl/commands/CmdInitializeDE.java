/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
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
      String codePage = _req.codePage();
      String locale   = _req.locale();

      // We have to make sure we tell EStdString objects to encode in the
      // same code page the front end expects.

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"front end using code page:"+codePage);

      if (frontendEncoding == EPDC.StrEncode_Ext_ASCII)
      {
        // In this case, we should tell epdc to encode to whatever the
        // code page field tells us to.

        if (Gdb.traceLogger.DBG) 
            Gdb.traceLogger.dbg(1,"front end encoding:Ext_ASCII");
        // We are not sure at this point whether the code page requested by
        // the front end is supported by the java runtime.  We will try to use
        // it and catch the error.
        try
        {
           if (Gdb.traceLogger.DBG) 
               Gdb.traceLogger.dbg(1,"   testing code page:"+codePage);
           byte[] testCodePage = "testCodePage".getBytes(codePage);
        }
        catch (Exception e)
        {
           // Either UnsupportedEncodingException or IllegalArgumentException

           if (Gdb.traceLogger.DBG) 
               Gdb.traceLogger.dbg(1,"   code page is not supported");
           // This encoding is not supported, we will detect any code pages
           // for which we know we have a replacement here.
           if (codePage.equalsIgnoreCase("ibm-1386"))
           {
              if (Gdb.traceLogger.DBG) 
                  Gdb.traceLogger.dbg(1,"   switching to ibm-1381");
              codePage = "ibm-1381";
           }
           else if (codePage.equalsIgnoreCase("ibm-1363"))
           {
               if (Gdb.traceLogger.DBG) 
                   Gdb.traceLogger.dbg(1,"   switching to EUC_KR");
              codePage = "EUC_KR";
           }
           else if (codePage.equalsIgnoreCase("ibm-1252"))
           {
               if (Gdb.traceLogger.DBG) 
                   Gdb.traceLogger.dbg(1,"   switching to Cp1252");
              codePage = "Cp1252";
           }
           else
           {
              // Fallback to whatever the runtime's default encoding is and also
              // switch the locale to the platform's default.
              String defaultEncoding = System.getProperty("file.encoding");
              codePage = defaultEncoding;

              Locale localeObj = Locale.getDefault();
              String language = localeObj.getLanguage();
              String country  = localeObj.getCountry();
              String variant  = localeObj.getVariant();

              if (country !=null && country.length() > 0 &&
                  language!=null && language.length() > 0 &&
                  variant != null && variant.length() > 0)
                 locale = language + "_" + country + "_" + variant;
              else if (country != null && country.length() > 0 &&
                       language!= null && language.length() > 0)
                 locale = language + "_" + country;
              else if (language != null && language.length() > 0)
                 locale = language;
              else
                 locale = "en_US";

               if (Gdb.traceLogger.DBG) 
                   Gdb.traceLogger.dbg(1,"   switching codepage to platform default:"+defaultEncoding);
               if (Gdb.traceLogger.DBG) 
                   Gdb.traceLogger.dbg(1,"   switching locale to platform default:"+locale);
           }
        }

        if (Gdb.traceLogger.DBG) 
            Gdb.traceLogger.dbg(1,"back end using code page:"+codePage);
        EStdString.setEncoding(codePage);
      }
      else if (frontendEncoding == EPDC.StrEncode_UTF8)
      {
        // In this case, we should tell epdc to encode int UTF8 only
        // (!!! is the codePage field useless in this case?)

        if (Gdb.traceLogger.DBG) 
            Gdb.traceLogger.dbg(1,"front end encoding:UTF8");
        EStdString.setEncoding("UTF8");
      }
      else
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
      _EPDCSession._debugEnginePlatformID = EPDC.PLATFORM_ID_AIX;

      _EPDCSession._defaultSettings       = EPDC.DebuggerBusyBoxEnable;
      _EPDCSession._PMDebuggingAction     = EPDC.NoPaint;
      _EPDCSession._PMDebuggingColor      = EPDC.Black;
      _EPDCSession._PMDebuggingMode       = EPDC.Synchronous;
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

      short[] reps1 = { Gdb.REPINDEX_UNICODE, Gdb.REPINDEX_DECIMAL,
                        Gdb.REPINDEX_HEXADECIMAL, Gdb.REPINDEX_ASCII };

      _EPDCSession._repInfo.addTypesAndReps(
         new ERepTypesAndRepsGetNext(Gdb.TYPEINDEX_CHARACTER,
           _debugSession.getResourceString("TYPENAME_CHARACTER_TEXT"),
               Gdb.REPINDEX_ASCII,reps1));

      short[] reps2 = { Gdb.REPINDEX_IEEEFLOAT, Gdb.REPINDEX_FLOATINGPOINT };

      _EPDCSession._repInfo.addTypesAndReps(
         new ERepTypesAndRepsGetNext(Gdb.TYPEINDEX_FLOAT,
           _debugSession.getResourceString("TYPENAME_FLOAT_TEXT")
               ,Gdb.REPINDEX_FLOATINGPOINT,reps2));

      short[] reps3 = { Gdb.REPINDEX_IEEEFLOAT, Gdb.REPINDEX_FLOATINGPOINT };
      _EPDCSession._repInfo.addTypesAndReps(
         new ERepTypesAndRepsGetNext(Gdb.TYPEINDEX_DOUBLE,
           _debugSession.getResourceString("TYPENAME_DOUBLE_TEXT"),
               Gdb.REPINDEX_FLOATINGPOINT,reps3));

      short[] reps4 = { Gdb.REPINDEX_DECIMAL, Gdb.REPINDEX_HEXADECIMAL};
      _EPDCSession._repInfo.addTypesAndReps(
         new ERepTypesAndRepsGetNext(Gdb.TYPEINDEX_INTEGER,
           _debugSession.getResourceString("TYPENAME_INTEGER_TEXT"),
               Gdb.REPINDEX_DECIMAL,reps4));

      // Should also add the following types some time in the future
      // "Class Reference"
      // "Interface Reference"
      // "Array Reference"

// ***************************************************************************
//    Data representations.
// ***************************************************************************

      _repNames = new String[10];

      _repNames[Gdb.REPINDEX_DECIMAL]       =_debugSession.getResourceString("REPNAME_DECIMAL_TEXT");
      _repNames[Gdb.REPINDEX_HEXADECIMAL]   =_debugSession.getResourceString("REPNAME_HEXADECIMAL_TEXT");
      _repNames[Gdb.REPINDEX_BOOLEAN]       =_debugSession.getResourceString("REPNAME_BOOLEAN_TEXT");
      _repNames[Gdb.REPINDEX_ASCII]         =_debugSession.getResourceString("REPNAME_ASCII_TEXT");
      _repNames[Gdb.REPINDEX_UNICODE]       =_debugSession.getResourceString("REPNAME_UNICODE_TEXT");
      _repNames[Gdb.REPINDEX_STRING]        =_debugSession.getResourceString("REPNAME_STRING_TEXT");
      _repNames[Gdb.REPINDEX_FLOATINGPOINT] =_debugSession.getResourceString("REPNAME_FLOATINGPOINT_TEXT");
      _repNames[Gdb.REPINDEX_ARRAY]         =_debugSession.getResourceString("REPNAME_ARRAY_TEXT");
      _repNames[Gdb.REPINDEX_IEEEFLOAT]     =_debugSession.getResourceString("REPNAME_IEEEFLOAT_TEXT");

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
//         EPDC.FCT_LOAD_BREAKPOINT           | // support load bkpts
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
//         EPDC.FCT_EXCEPTION_FILTER  |  // support exception filtering
//         EPDC.FCT_EXCEPTION_STEP    |  // support step exception
         EPDC.FCT_EXCEPTION_RUN     |  // support run exception
         0,

         // Stack Options
         0);

      _EPDCSession._helpFileName     = helpFileName();
      _EPDCSession._tutorialFileName = tutorialFileName();
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

   String helpFileName()
   {
      String helpFileName;

      String filePrefix = _req.filePrefix();

      if (filePrefix == null)
         filePrefix = "iwz";

      helpFileName = filePrefix + "d" + "h" + "j" + nlsCode() +
                     DEBUGGER_RELEASE_NUMBER + ".hlp";

      return helpFileName;
   }

   String tutorialFileName()
   {
      String tutorialFileName;

      String filePrefix = _req.filePrefix();

      if (filePrefix == null)
         filePrefix = "iwz";

      tutorialFileName = filePrefix + "d" + "i" + "j" + nlsCode() +
                         DEBUGGER_RELEASE_NUMBER+ ".inf";

      return tutorialFileName;
   }


   // Data members
   private EReqInitializeDE   _req;
   private EPDC_EngineSession _EPDCSession;
   private String[] _repNames;

   final static String DEBUGGER_RELEASE_NUMBER = "1";
}
