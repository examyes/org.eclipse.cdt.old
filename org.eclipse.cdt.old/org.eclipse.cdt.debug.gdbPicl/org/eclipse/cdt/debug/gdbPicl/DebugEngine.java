//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl;
import com.ibm.debug.gdbPicl.commands.*;

import java.io.*;
import java.net.*;
import java.util.*;
import com.ibm.debug.epdc.*;      // EPDC classes
import com.ibm.debug.connection.Connection;


/**
 * The DebugEngine class represents one debug engine.  You may instantiate
 * any number of DebugEngine classes.
 */
public class DebugEngine extends Thread
{
  /**
   * Construct a new DebugEngine from a connection object.
   */
   public DebugEngine(Connection connection)
   {
      _connection       = connection;
      _showDebugOutput  = false;
      _filteredModules  = null;
      _debugSession     = null; // DebugSession is ABSTRACT  //new DebugSession(this);
   }

  /**
   * Sets whether this debug engine will print debug output.
   */
   void setDebugOutput(boolean showDebugOutput)
   {
      _showDebugOutput = showDebugOutput;
   }

  /**
   * Returns whether the this debug engine will print debug output.
   */
   boolean showDebugOutput()
   {
      return _showDebugOutput;
   }

  /**
   * Get the debugger session object
   */
   public DebugSession getDebugSession() {
      return _debugSession;
   }

  /**
   * Get the debugger EPDC_EngineSession object
   */
   public EPDC_EngineSession getSession() {
      return _EPDCSession;
   }

  /**
   * Get a resource string which is to be eventually passed to the front end.
   * The locale used will match the front end's locale setting (passed to
   * is via InitializeDE request).
   */
   public String getResourceString(String key)
   {
      String msg;
      if (_frontEndMessages != null)
      {
         try
         {
            msg = _frontEndMessages.getString(key);
         }
         catch (MissingResourceException e)
         {
             if(key.equals("MISSING_RESOURCE_STRING") )
             {
                if(Gdb.traceLogger.ERR)
                    Gdb.traceLogger.err(2,"MISSING_RESOURCE_STRING: "+key );
                msg = key+" ";
             }else
             {
                msg = getResourceString("MISSING_RESOURCE_STRING")+key;
                if(Gdb.traceLogger.ERR)
                    Gdb.traceLogger.err(2,getResourceString("MISSING_RESOURCE_STRING")+key );
             }
         }
      }
      else
      {
         msg = "MISSING_RESOURCE_BUNDLE_FOR_KEY: "+key;
         if(Gdb.traceLogger.ERR)
             Gdb.traceLogger.err(1,"MISSING_RESOURCE_BUNDLE_FOR_KEY: "+key );
      }
      return msg;
   }

  /**
   * Get a message which will be eventually passed to the front end.
   */
   public String getMessage(String key)
   {
      return getMessage(key, null, null, null, null);
   }

  /**
   * Get a message which will be eventually passed to the front end.
   */
   public String getMessage(String key, Object o1)
   {
      return getMessage(key, o1, null, null, null);
   }

  /**
   * Get a message which will be eventually passed to the front end.
   */
   public String getMessage(String key, Object o1, Object o2)
   {
      return getMessage(key, o1, o2, null, null);
   }

  /**
   * Get a message which will be eventually passed to the front end.
   */
   public String getMessage(String key, Object o1, Object o2,
                            Object o3)
   {
      return getMessage(key, o1, o2, o3, null);
   }

  /**
   * Get a message which will be eventually passed to the front end.
   */
   public String getMessage(String key, Object o1, Object o2,
                            Object o3, Object o4)
   {
      Object args[] = { o1, o2, o3, o4 };
      String format = getResourceString(key);
      String msg = java.text.MessageFormat.format(format, args);
      return msg;
   }

  /**
   * Set the locale for the messages that will be sent to the front end.
   */
   public boolean setLocale(Locale locale)
   {
      try
      {
         // We need the fully qualified name here or else we may not be able to find the resource bundle
         _frontEndMessages =
            ResourceBundle.getBundle("com.ibm.debug.gdbPicl.GdbMessages", locale);
      }
      catch (MissingResourceException e)
      {
         return false;
      }

      if (_frontEndMessages == null)
         return false;
      else
         return true;
   }

  /**
   * DebugEngine's run method.  (Use DebugEngine.start() to start an engine)
   */
   public void run()
   {
      if (Gdb.traceLogger.EVT) 
         Gdb.traceLogger.evt(1,"================ DebugEngine.run" );
  
      EPDC_Request   req;
      Command        cmd;

// ***************************************************************************
//    Create component manager objects
// ***************************************************************************
       _debugSession.createManagers();

// ***************************************************************************
//    Set up EPDC Session.  All EPDC session information will be initialized
//    within the CmdInitializeDE request.  The only piece of information we
//    are REQUIRED to set is the version number since EReqVersion will arrive
//    before EReqInitializeDE.
// ***************************************************************************
      _EPDCSession = new EPDC_EngineSession();
      // set the debugengine's version to the max supported.  This will be negotiated when the engine
      // first talks to the model
      // _EPDCSession._debugEngineVersion    = MAX_SUPPORTED_EPDC_VERSION;

      CommandProcessor commandProcessor = new CommandProcessor(_connection, _EPDCSession, _debugSession);
      commandProcessor.start();

// ***************************************************************************
//    This loop waits for an EPDC request to arrive and gives it to the
//    command processor (a separate thread) to execute.  We then loop back
//    up immediately and wait for another request.  If we sent the command
//    processor a terminate program request, we exit our loop.  The command
//    processor will handle the exit and cleanup.  If we detect that the
//    connection to the front end has been lost, we tell the command processor
//    this and exit.  All interaction with the debug session and cleanup is
//    handled by the command processor thread.
// ***************************************************************************
      try
      {
         while (true)
         {
	   req = EPDC_Request.decodeRequestStream(_connection);
	   cmd = getCommand(req);

	   if (cmd == null)
	     continue;
	   else
	     commandProcessor.execute(cmd);

	   if (cmd instanceof CmdTerminateDE)
	   {  
         if (Gdb.traceLogger.EVT) 
             Gdb.traceLogger.evt(2,"DebugEngine.CmdTerminateDE" );
         break;
      }
         }
      }
      catch (EOFException e)
      {
         commandProcessor.connectionLost();
      }
      catch (IOException e)
      {
         commandProcessor.connectionLost();
         //Gdb.msgOutput(Gdb.getConsoleResourceString("CONNECTION_LOST_MSG"));
      }

      // Wait for the Command Processor to exit
      try {
	commandProcessor.join();
      }
      catch (Exception excp) {
	Gdb.handleException(excp);
      }
   }

   Command getCommand(EPDC_Request req) {

      // Unknown requests come in as null.  Ignore and continue.
      if (req == null) return null;

      switch(req.requestCode()) {
         case EPDC.Remote_BreakpointClear:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Clear breakpoint \n");
            return new CmdBreakpointClear(_debugSession, (EReqBreakpointClear) req);

         case EPDC.Remote_BreakpointDisable:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Disable breakpoint \n");
            return new CmdBreakpointDisable(_debugSession, (EReqBreakpointDisable) req);

         case EPDC.Remote_BreakpointEnable:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Enable breakpoint \n");
            return new CmdBreakpointEnable(_debugSession, (EReqBreakpointEnable) req);

         case EPDC.Remote_BreakpointLocation:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Location breakpoint \n");
            return new CmdBreakpointLocation(_debugSession, (EReqBreakpointLocation) req);

         case EPDC.Remote_BreakpointEvent:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Event breakpoint \n");
            return new CmdBreakpointEvent(_debugSession, (EReqBreakpointEvent) req);

         case EPDC.Remote_CommandLogExecute:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> CommandLogExecute \n");
            return new CmdCommandLogExecute(_debugSession, (EReqCommandLogExecute) req);

         case EPDC.Remote_ContextConvert:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Context convert \n");
            return new CmdContextConvert(_debugSession, (EReqContextConvert) req);

         case EPDC.Remote_ContextQualGet:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Context qual get \n");
            return new CmdContextQualGet(_debugSession, (EReqContextQualGet) req);

         case EPDC.Remote_EntrySearch:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Entry search \n");
            return new CmdEntrySearch(_debugSession, (EReqEntrySearch) req);

         case EPDC.Remote_EntryWhere:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Entry where \n");
            return new CmdEntryWhere(_debugSession, (EReqEntryWhere) req);

         case EPDC.Remote_ExceptionStatusChange:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Exception status change \n");
            return new CmdExceptionStatusChange(_debugSession, (EReqExceptionStatusChange) req);

         case EPDC.Remote_Execute:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Execute \n");
            return new CmdExecute(_debugSession, (EReqExecute) req);

         case EPDC.Remote_ExpressionDisable:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Expression disable \n");
            return new CmdExpressionDisable(_debugSession, (EReqExpressionDisable) req);

         case EPDC.Remote_ExpressionEnable:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Expression enable \n");
            return new CmdExpressionEnable(_debugSession, (EReqExpressionEnable) req);

         case EPDC.Remote_ExpressionFree:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Expression free \n");
            return new CmdExpressionFree(_debugSession, (EReqExpressionFree) req);

         case EPDC.Remote_Expression:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Expression \n");
            return new CmdExpression(_debugSession, (EReqExpression) req);

         case EPDC.Remote_ExpressionRepTypeSet:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Expresion representation type set \n");
            return new CmdExpressionRepTypeSet(_debugSession, (EReqExpressionRepTypeSet) req);

         case EPDC.Remote_ExpressionSubTree:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Expression subtree \n");
            return new CmdExpressionSubTree(_debugSession, (EReqExpressionSubTree) req);

         case EPDC.Remote_ExpressionSubTreeDelete:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Expression subtree delete \n");
            return new CmdExpressionSubTreeDelete(_debugSession, (EReqExpressionSubTreeDelete) req);

         case EPDC.Remote_ExpressionValueModify:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Expression value modify \n");
            return new CmdExpressionValueModify(_debugSession, (EReqExpressionValueModify)req);

         case EPDC.Remote_Initialize_Debug_Engine:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Initialize debug engine \n");
            return new CmdInitializeDE(_debugSession, (EReqInitializeDE) req);

         case EPDC.Remote_LocalVariable:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Local Variable \n");
            return new CmdLocalVariable(_debugSession, (EReqLocalVariable) req);

         case EPDC.Remote_LocalVariableFree:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Local Variable Free \n");
            return new CmdLocalVariableFree(_debugSession, (EReqLocalVariableFree) req);

         case EPDC.Remote_PartGet:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Get part \n");
            return new CmdPartGet(_debugSession, (EReqPartGet) req);

         case EPDC.Remote_PartOpen:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Part open \n");
            return new CmdPartOpen(_debugSession, (EReqPartOpen) req);

         case EPDC.Remote_PartSet:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Set part \n");
            return new CmdPartSet(_debugSession, (EReqPartSet) req);

         case EPDC.Remote_PreparePgm:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Prepare program \n");
            return new CmdPreparePgm(_debugSession, (EReqPreparePgm) req);

         case EPDC.Remote_ProcessAttach:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Process attach \n");
            return new CmdProcessAttach(_debugSession, (EReqProcessAttach) req);

         case EPDC.Remote_ProcessAttach2:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Process attach2 \n");
            return new CmdProcessAttach2(_debugSession, (EReqProcessAttach2) req);

         case EPDC.Remote_ProcessDetach:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Process detach \n");
            return new CmdProcessDetach(_debugSession, (EReqProcessDetach) req);

         case EPDC.Remote_ProcessDetailsGet:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Process details get \n");
            return new CmdProcessDetailsGet(_debugSession, (EReqProcessDetailsGet) req);

         case EPDC.Remote_ProcessListGet:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Process list get \n");
            return new CmdProcessListGet(_debugSession, (EReqProcessListGet) req);

         case EPDC.Remote_RepForTypeSet:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> RepForType set \n");
            return new CmdRepForTypeSet(_debugSession, (EReqRepForTypeSet) req);

         case EPDC.Remote_Stack:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Stack \n");
            return new CmdStack(_debugSession, (EReqStack) req);

         case EPDC.Remote_StackBuildView:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Stack build view \n");
            return new CmdStackBuildView(_debugSession, (EReqStackBuildView) req);

         case EPDC.Remote_StackDetailsGet:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Get stack details \n");
            return new CmdStackDetailsGet(_debugSession, (EReqStackDetailsGet) req);

         case EPDC.Remote_StackFree:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Free stack \n");
            return new CmdStackFree(_debugSession, (EReqStackFree) req);

         case EPDC.Remote_StartPgm:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Start program \n");
            return new CmdStartPgm(_debugSession, (EReqStartPgm) req);

         case EPDC.Remote_StringFind:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> String find \n");
            return new CmdStringFind(_debugSession, (EReqStringFind) req);

         case EPDC.Remote_Terminate_Debug_Engine:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Terminate debug engine \n");
            return new CmdTerminateDE(_debugSession, (EReqTerminateDE) req);

         case EPDC.Remote_TerminatePgm:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Terminate program \n");
            return new CmdTerminatePgm(_debugSession, (EReqTerminatePgm) req);

         case EPDC.Remote_ThreadFreeze:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Thread Freeze \n");
            return new CmdThreadFreeze(_debugSession, (EReqThreadFreeze) req);

         case EPDC.Remote_ThreadThaw:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Thread Thaw \n");
            return new CmdThreadThaw(_debugSession, (EReqThreadThaw) req);

         case EPDC.Remote_TypesNumGet:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Types Num Get \n");
            return new CmdTypesNumGet(_debugSession, (EReqTypesNumGet) req);

         case EPDC.Remote_ViewsVerify:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Verify views \n");
            return new CmdVerifyView(_debugSession, (EReqVerifyViews) req);

         case EPDC.Remote_Version:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Version \n");
            return new CmdVersion(_debugSession, (EReqVersion) req);

         case EPDC.Remote_Halt:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Remote Halt \n");
            return new CmdRemoteHalt(_debugSession, (EReqRemoteHalt) req);

         case EPDC.Remote_ViewSearchPath:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> View Search Path \n");
            return new CmdViewSearchPath(_debugSession, (EReqViewSearchPath) req);

         case EPDC.Remote_ViewFileInfoSet:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> View File Info Set \n");
            return new CmdViewFileInfoSet(_debugSession, (EReqViewFileInfoSet) req);

         case EPDC.Remote_ThreadInfoGet:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> ThreadInfoGet \n");
            return new CmdThreadInfoGet(_debugSession, (EReqThreadInfoGet) req);

         case EPDC.Remote_Storage2:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Storage2" );
            return new CmdStorage2(_debugSession, (EReqStorage2) req);

         case EPDC.Remote_StorageRangeSet2:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> StorageRangeSet2" );
            return new CmdStorageRangeSet2(_debugSession, (EReqStorageRangeSet2) req);

         case EPDC.Remote_StorageStyleSet:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> StorageStyleSet" );
            return new CmdStorageStyleSet(_debugSession, (EReqStorageStyleSet) req);

         case EPDC.Remote_StorageFree:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> StorageFree" );
            return new CmdStorageFree(_debugSession, (EReqStorageFree) req);

         case EPDC.Remote_StorageEnablementSet:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> StorageEnablementSet" );
            return new CmdStorageEnablementSet(_debugSession, (EReqStorageEnablementSet) req);

         case EPDC.Remote_StorageUpdate:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> StorageUpdate" );
            return new CmdStorageUpdate(_debugSession, (EReqStorageUpdate) req);
            
         case EPDC.Remote_Registers2:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> Registers2 \n");
            return new CmdRegisters2(_debugSession, (EReqRegisters2) req);

         case EPDC.Remote_RegistersFree2:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> RegistersFree2 \n");
            return new CmdRegistersFree2(_debugSession, (EReqRegistersFree2) req);

         case EPDC.Remote_RegistersDetailsGet:
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"EPDC_CMD> RegistersDetailsGet \n");
            return new CmdRegistersDetailsGet(_debugSession, (EReqRegistersDetailsGet) req);

         default:
            if (Gdb.traceLogger.ERR) 
               Gdb.traceLogger.err(1,"UNKNOWN_EPDC_REQUEST_CODE" +Integer.toString(req.requestCode()) );
            if (Gdb.traceLogger.EVT)
               Gdb.traceLogger.evt(1,"EPDC_CMD> Unknown EPDC Request code: " +
                  Integer.toString(req.requestCode()) +" \r" );
            return null;
      }
   }

  /**
   * Clear all thread, parts, breakpoints, variable monitors and local variable
   * monitors from the managers.
   */
   public void clearManagers()
   {
      _debugSession.clearManagers();
   }

   public Vector getSrcPaths() { return _srcPaths; }

   /**
    * This function takes a string representing a list of paths and adds each
    * path to the source search paths list.   Paths in the string are assumed
    * to be separated by the path.separator property from System.getProperties()
    */
   void addSearchPath(String searchPath)
   {
      if (searchPath == null)
         return;

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(3,"Adding search paths: " + searchPath);
      StringTokenizer tokenizer = new StringTokenizer(searchPath, File.pathSeparator, false);
      while (tokenizer.hasMoreTokens())
      {
         String path = tokenizer.nextToken();
         addSrcPath(path);
      }
   }

  /**
   * Add an additional path in which to search for source
   */
   void addSrcPath(String path)
   {
      // Append a trailing file separator if there isn't one already
      if (path.charAt(path.length()-1) != File.separatorChar)
      {
         path += File.separatorChar;
      }

      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(3,"Adding search path: " + path);
      _srcPaths.addElement(path);
   }

   String getSrcPath() { return _jsrcpath; }

   void setSrcPath(String srcpath) { _jsrcpath = srcpath; }

   String getQsourcepath() { return _qsourcepath; }

   void setQsourcepath(String path) { _qsourcepath = path; }

  /**
   * Get the debugger's non debuggable module list
   */
   public Vector getFilteredModules()
   {
      if (_filteredModules == null)
      {
         _filteredModules = new Vector();
         File file = new File(filterFile);

         if (!file.exists() || file.isDirectory())
         {
            addDefaultFilteredModules(_filteredModules);
         }
         else
         {
            if (Gdb.traceLogger.DBG) 
                Gdb.traceLogger.dbg(3,"Opened filter file:" + filterFile);
            try
            {
               BufferedReader reader = new BufferedReader(new FileReader(file));

               while (reader.ready())
               {
                  String line = reader.readLine();
                  if (!line.startsWith("#") && line.length() > 0)
                  {
                     line = line.trim();
                     int index = line.indexOf('*');
                     if (index != -1)
                     {
                        line = line.substring(0,index);
                     }
                     if (Gdb.traceLogger.DBG) 
                         Gdb.traceLogger.dbg(3,"   added "+line);
                     _filteredModules.addElement(line);
                  }
               }
               reader.close();
            }
            catch (FileNotFoundException e)
            {
               addDefaultFilteredModules(_filteredModules);
            }
            catch (IOException e)
            {
               addDefaultFilteredModules(_filteredModules);
            }
         }
      }

      return _filteredModules;
   }
   void addDefaultFilteredModules(Vector filteredModules)
   {
      if (Gdb.traceLogger.DBG) 
         Gdb.traceLogger.dbg(1,"Using default filtered module list");
      filteredModules.addElement("xXyYzZ");
   }


// ***************************************************************************
//  Data members
// ***************************************************************************
    private   boolean                      _exitDE;
    private   boolean                      _showDebugOutput;
    private   String                       _host;
    private   String                       _password;
    protected Connection                   _connection;
    protected ResourceBundle               _frontEndMessages;
    protected DebugSession                 _debugSession;
    protected EPDC_EngineSession           _EPDCSession;
    private   Vector                       _filteredModules;
    String    filterFile = "modules.lst";

    // AB: the following represent the supported versions of EPDC that the engine will handle.
    public static final int MIN_SUPPORTED_EPDC_VERSION = 305;
    public static final int MAX_SUPPORTED_EPDC_VERSION = 307;

    Vector _srcPaths= new Vector();
    String _jsrcpath;
    String _qsourcepath = null;
}
