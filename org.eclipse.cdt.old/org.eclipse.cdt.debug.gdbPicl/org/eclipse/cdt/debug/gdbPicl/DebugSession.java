/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl;
import  com.ibm.debug.gdbPicl.objects.*;

import java.util.*;
import java.net.*;
import java.io.*;
import java.text.*;
import com.ibm.debug.epdc.EPDC;
import com.ibm.debug.epdc.ERepCommandLogExecute;


// User Events
//     User defined breakpoint
//     User defined watchpoint
//     User code throws exception
//     Program Quit
//     Halt Request

// System Events
//     System defined breakpoint (eg. breakpoint as result of a step request)
//     Thread Death event
//     Special Kicker breakpoint

public abstract class DebugSession
{

  /**
   * Creates RemoteDebugger object.
   */
   public DebugSession(DebugEngine debugEngine)
   {
      _debugEngine = debugEngine;

      _parms               = null;
      _exceptionName       = null;
      _whyStop             = WS_Unknown;
   }


   abstract void createManagers();

   public abstract Part isPartInModule(String partName, String moduleName);

  /**
   * Inform the RemoteDebugger which exceptions we wish to be informed of and
   * which we wish to ignore.
   */
   abstract void initExceptions();

   public abstract int cmdCommandLogExecute(String cmd, ERepCommandLogExecute _rep );

  /**
   * Attempts to perform detach operation from the remote JVM
   * @param processIndex the processIndex of the process we wish to detach from
   * @param processDetachAction if EPDC.ProcessKill then the debuggee is
   * @param errorMsg a string array of size one into which an error
   * message is placed if the operation failed
   * killed, otherwise the debuggee continues to run
   */
   public abstract boolean remoteDetach(int processIndex, int processDetachAction,
      String[] errorMsg);

  /**
   * Terminates the debuggee.  Returns true if the operation succeeded,
   * false otherwise.
   */
   public abstract boolean terminateDebuggee();

  /**
   * Closes the connection to the debuggee. If this DebugSession invoked the
   * debuggee, the debuggee is terminated. Otherwise the debuggee is resumed.
   * Returns true if the operation succeeded, false otherwise.
   */
   public abstract boolean closeDebugger();

  /**
   * Attempts to connect this DebugSession to a running JVM with the host
   * and password specified by pid (see DebugEngine.addHost()).
   * @return boolean true if the attach was successful, false otherwise
   */
   public abstract boolean remoteAttach(int processIndex, String[] errorMsg);

  /**
   * Return true if modification watchpoints are supported, false otherwise.
   */
   public boolean modificationWatchpointsSupported()
   {
      return false;
   }

  /**
   * Returns a list of new part names known by the debugger
   */
   abstract String[] getNewPartsList();

   public abstract String getFullProgramName(String name);






  /**
   * Sets the program which will be executed.  
   *
   * The debugger is started with the <code> DbgStarter </code> class
   * running, which redirectes the debuggee's TCP/IP socket stream
   * into the debuggee's <code> System.in </code> stream.
   *
   * @return true if ProgramName is executable, false if this function does
   *    not have a main method or if the ProgramName does not exist
   */
   public abstract boolean setStartProgramName(String programName, String parms, String[] errorMsg);


  /**
   * Catch this exception
   */
   public abstract void catchException(int index, String exceptionName);


  /**
   * Ignore this exception
   */
   public abstract void ignoreException(int index, String exceptionName);


  /**
   * Executes the program set by the method setStartProgramName() to the main method.
   */
   public abstract void runToMain();


  /**
   * Performs a shallow step debug
   */
   public abstract int cmdStepDebug(String threadName);


  /**
   * Resumes stopped execution until a user event occurs
   * @return the reason for stopping
   */
   public abstract int cmdRun_User();


   /**
    * Tell this debug sesssion to temporarily treat a system breakpoint on
    * this part,line as a user breakpoint.  Only one temporary breakpoint
    * may be set at a time.
    */
   public abstract void setTmpBkpt(int partID, int lineNum);


   /**
    * Tell this debug session to ignore the previous temporary breakpoint
    * set via setTmpBkpt.
    */
   public abstract void clearTmpBkpt();


   public abstract int cmdStepReturn_User(String threadName);


   public abstract int cmdGoTo(String threadID, String lineNum);



  /**
   * Steps the specified thread, stepping into or over method calls
   * as specified.
   * @param threadName thread to step
   * @param stepover whether to step over method calls
   * @returns the reason for stopping, 0 if specified thread does not exist
   */
   public abstract int cmdStep(String threadName, boolean stepover);

  /**
   * Handle "Step Exception" request by reseting the whyStop flag
   */
   public abstract void cmdStepException(String threadName);


  /**
   * Handle "Examine Exception" request by reseting the whyStop flag
   */
   public abstract void cmdExamineException(String threadName);


  /**
   * Returns reason for last debugger callback
   */
   public abstract int whyStop();

  /**
   * Returns text to add to the reason for last debugger callback
   */
   public String whyStopMsg()
   {
      return _whyStopMsg;
   }

   /**
    * Add to the list of satisfied breakpoints/watchpoints encountered
    */
   void addBreakpointHit(Breakpoint bp)
   {
     if (_breakpointsEncountered == null)
       _breakpointsEncountered = new Vector();

     _breakpointsEncountered.addElement(bp);
   }

   /**
    * Add to the list of satisfied breakpoints/watchpoints encountered
    */
   void clearBreakpointsHit()
   {
     _breakpointsEncountered = null;
   }

   /**
    * Returns Vector of breakpoints/watchpoints that caused the debugger to stop
    */
   public Vector breakpointsHit()
   {
     return _breakpointsEncountered;
   }

   /**
    * Returns the name of the last exception thrown
    */
   public abstract String exceptionName();

   /**
    * Returns name of stopped thread
    */
   public abstract String stopThreadName();

  /**
   * Set a method breakpoint at the specified part and method number
   * @return -1 if the set failed, otherwise returns the line number the
   * method breakpoint was set on
   */
   abstract int setMethodBreakpoint(int partID, int methodIndex);

  /**
   * Returns a list of part names known by the debugger
   */
   public abstract String[] getPartsList();

   /**
    * Return an array of part names that match a name 
    */
   String[] getPartsList(String name)
   {
     return null;
   }

  /**
   * Kill the debuggee (destroys the program thread group).
   * @return true always, we don't care any more about exceptions at this point
   */
   protected  boolean killDebuggee() {
     return true;
   }


   /**
    * Returns true if the last event that caused the debugger to wake up
    * from a debugWait() was a system event. False if it was a user event.
    */
   protected boolean wasSystemEvent()
   {
      if (_whyStop == WS_BkptHit || _whyStop == WS_KickerResumed)
         return true;
      return false;
   }

   /**
    */
   public void setLastPartSetFailed(boolean failed, String srcFile)
   {
      _lastPartSetFailed  = failed;
      _lastPartSetSrcFile = srcFile;
   }

   /**
    * Returns the source file name that the last CmdPartSet request failed
    * to find.  (Used by CmdViewSearchPath)
    */
   public String getLastPartSetSrcFile()
   {
      return _lastPartSetSrcFile;
   }

   /**
    * Returns whether the last CmdPartSet request this engine processed
    * failed or not.  (Used by CmdViewSearchPath)
    */
   public boolean getLastPartSetFailed()
   {
      return _lastPartSetFailed;
   }

  /**
   * Add the appropriate method filters for the deferred line/method
   * breakpoints.
   * @param deferredBkp The deferred breakpoint object
   */
   void prepareDeferredBreakpointMethodFilter(Breakpoint bkp) {}

   /**
    * Sets the command the user last requested and the depth of the call stack
    * at the time.  This information is used by kicker handler to reposition
    * the user back to the correct line if necessary.
    */
   public abstract void setLastUserCmd(int cmd, int depth);

   /**
    * Gets the command the user last requested.
    */
   abstract int getLastUserCmd(int cmd);

   /**
    * Gets the call stack depth at the time the user last requested execution.
    */
   abstract int getLastUserDepth(int depth);


   /**
    * Returns whether the debug session is currently waiting for a debug
    * event to notify it.
    */
   public abstract boolean isWaiting();


   /**
   * Set a line breakpoint at the specified part and line
   * @return whether the breakpoint was successfully set
   */
   public abstract boolean setLineBreakpoint(int partID, int lineNum);

   /**
   * Remove a breakpoint at a specific location
   */
   public abstract void clearBreakpoint(int partID, int lineNo);

   /**
   * Set a Watchpoint for the specifiec expression
   * @return whether the breakpoint was successfully set
   */
   public abstract boolean setWatchpoint(String expression);
 
  /**
   * Get the debugger ModuleManager object
   */
   public ModuleManager getModuleManager() {
      return _moduleManager;
   }

  /**
   * Get the debugger ThreadManager object
   */
   public ThreadManager getThreadManager() {
      return _threadManager;
   }

  /**
   * Get the debugger RegisterManager object
   */
   public RegisterManager getRegisterManager() {
      return _registerManager;
   }

  /**
   * Get the debugger StorageManager object
   */
   public StorageManager getStorageManager() {
      return _storageManager;
   }

  /**
   * Get the debugger BreakpointManager object
   */
   public BreakpointManager getBreakpointManager() {
      return _breakpointManager;
   }

  /**
   * Get the debugger VariableMonitorManager object
   */
   public VariableMonitorManager getVariableMonitorManager() {
      return _variableMonitorManager;
   }

  /**
   * Get the debugger LocalVariablesMonitorManager object
   */
   public LocalVariablesMonitorManager getLocalVariablesMonitorManager() {
      return _localVariablesMonitorManager;
   }
 
  /**
   * Clear all thread, parts, breakpoints, variable monitors and local variable
   * monitors from the managers.
   */
   public void clearManagers()
   {
      _moduleManager.clearModules();
      _threadManager.clearThreads();
      _breakpointManager.clearBreakpointInfo();
      _variableMonitorManager.clearVariables();
      _localVariablesMonitorManager.clearLocalVariables();
      _registerManager.clearRegisters();
      _storageManager.clearStorage();
   }

   // data members

   public DebugEngine getDebugEngine() { return _debugEngine; }
   public String getResourceString(String key)
   {  return  _debugEngine.getResourceString(key); }

   // References to other Jde components
   protected DebugEngine                  _debugEngine;
   protected ThreadManager                _threadManager;
   protected ModuleManager                _moduleManager;
   protected BreakpointManager            _breakpointManager;
   protected VariableMonitorManager       _variableMonitorManager;
   protected LocalVariablesMonitorManager _localVariablesMonitorManager;
   protected RegisterManager              _registerManager;
   protected StorageManager               _storageManager;
 
   protected String            _parms[];          // array of parameters to pass to run method
   protected int               _whyStop;          // why the program stopped
   protected String            _whyStopMsg;       // additional text to add to the whystop
   public    String            _whyExceptionMsg = null;
   protected String            _exceptionName;    // name of exception thrown
   protected boolean[]         _exceptionsStatus; // false = ignore, true = catch
   protected Vector            _breakpointsEncountered; // user breakpoints or watchpoints 
                                                        // that caused program to stop, if any
   protected int _partStartIndex;
   protected int lastUserCmd   = CmdStepInto;
   protected int lastUserDepth = 0;
   protected boolean isWaiting = false;

   // Last CmdPartSet Information
   // These two variables hold the information for the last part set request
   // that this engine processed.  This information is used by the
   // CmdViewSearchPath command during local source processing.
   protected boolean _lastPartSetFailed  = false;
   protected String  _lastPartSetSrcFile = "";

   // why stop constants
   public static final int WS_Unknown=0;
   public static final int WS_BkptHit=1;                  // System event
   public static final int WS_PgmQuit=2;                  // User Event
   public static final int WS_ExceptionThrown=3;          // User Event
   public static final int WS_UncaughtExceptionThrown=4;  // User Event
   public static final int WS_ThreadDeath=5;              // User Event
   public static final int WS_HaltRequest=6;              // User Event
   public static final int WS_UserBkptHit=7;              // User Event
   public static final int WS_KickerResumed=8;            // System Event
   public static final int WS_RemoteDebuggerIsDead=9;     // User Event
   public static final int WS_PgmDetach=10;               // User Event
   public static final int WS_UserBkptCondErr=11;         // User Event - condition on bkp in error
   public static final int WS_Watchpoint=12;              // User Event
   public static final int WS_TmpBkptHit=13;              // User Event

   public static final int CmdRun              = 1;
   public static final int CmdStepOver         = 2;
   public static final int CmdStepInto         = 3;
   public static final int CmdStepDebug        = 4;
   public static final int CmdStepReturn       = 5;
   public static final int CmdStepException    = 6;
   public static final int CmdExamineException = 7;
   public static final int CommandLogExecute   = 8;
   public static final int CmdGoTo             = 9;

   // Kicker Flow Codes
   public static final int DEBUG_FLOW_DEBUG    = 1;
   public static final int DEBUG_FLOW_PASSTHRU = 0;
   public static final int DEBUG_FLOW_UNKNOWN  =-1;

   static final String DEBUG_FLOW_DEBUG_STRING    = "DEBUG";
   static final String DEBUG_FLOW_PASSTHRU_STRING = "PASSTHRU";
   static final String DEBUG_FLOW_UNKNOWN_STRING  = "UNKNOWN";

   // KICKER_NAME should be set to the fully qualified package name of
   // the kicker class.
   // DEBUG_* should be set to the names of the methods Gdb PICL will set
   // breakpoints in after attaching.
   static final String KICKER_NAME   = "com.ibm.debug.export.Kicker";
   static final String DEBUG_START   = "debug_start";
   static final String DEBUG_STARTABS= "debug_startabs";
   static final String DEBUG_STOP    = "debug_stop";
   static final String DEBUG_SUSPEND = "debug_suspend";
   static final String DEBUG_RESUME  = "debug_resume";
   static final String DEBUG_DETACH  = "debug_detach";
}
