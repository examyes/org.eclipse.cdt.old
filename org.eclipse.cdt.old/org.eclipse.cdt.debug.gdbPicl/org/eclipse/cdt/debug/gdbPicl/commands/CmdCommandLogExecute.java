/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/commands/CmdCommandLogExecute.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:44:22)
/////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.commands;
import com.ibm.debug.gdbPicl.*;
import com.ibm.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;
import com.ibm.debug.util.TraceLogger;

/**
 * Process CommandLogExecute command
 */
public class CmdCommandLogExecute extends Command 
{
   public CmdCommandLogExecute(DebugSession debugSession, EReqCommandLogExecute req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * execute program as specified
    */

   public boolean execute(EPDC_EngineSession EPDCSession)
   {

//////////////////////////////////////////////////////////////////////////////////////
      String commandString = _req.getCommandString();
      if(commandString==null) 
         commandString=" "; 
      if(Gdb.traceLogger.EVT) 
         Gdb.traceLogger.evt(1,"===>>> CmdCommandLogExecute _commandString="+commandString );
      int whyStopXX = EPDC.Why_none;
      int dU = 1234;
      _rep = new ERepCommandLogExecute(dU, whyStopXX);
//      //_rep.setMessage( null );  //"GDB..GDB..GDB..GDB" );
//    ((ERepCommandLogExecute)_rep).setExceptionMsg( null ); //"GDB..EXCEPTION..GDB" );
//    ((ERepCommandLogExecute)_rep).addResponseLine("Response Line #1" );
//    ((ERepCommandLogExecute)_rep).addResponseLine("Response Line #2" );
//            debugSession.setLastUserCmd(DebugSession.CommandLogExecute,0);
            ((GdbDebugSession)_debugSession).cmdCommandLogExecute( commandString, (ERepCommandLogExecute)_rep );

      if(true) return false;//////////////////////////////////////////////////////////////////////////////////////

      // Note: Front end will send 0 if EPDC.Exec_Run was selected
      int DU = 0; //BCS _req.getDU();

      ThreadManager                threadManager                = _debugSession.getThreadManager();
      ModuleManager                classManager                 = _debugSession.getModuleManager();
      BreakpointManager            breakpointManager            = _debugSession.getBreakpointManager();
      VariableMonitorManager       variableMonitorManager       = _debugSession.getVariableMonitorManager();
      LocalVariablesMonitorManager localVariablesMonitorManager = _debugSession.getLocalVariablesMonitorManager();

      if (DU > 0)
      {
         ThreadComponent tc = threadManager.getThreadComponent(DU);
         if (tc != null && tc.isBlocked())
         {
            _rep = new ERepCommandLogExecute(0, 0);
            _rep.setReturnCode(EPDC.ExecRc_ThreadBlocked);
            _rep.setMessage(_debugSession.getResourceString("CANT_STEP_BLOCKED_THREAD"));
            return false;
         }
      }

//BCS      EStdView view = _req.getViewInfo();
//BCS      int partID = view.getPPID();
//BCS      int lineNum = view.getLineNum();

            _debugSession.setLastUserCmd(DebugSession.CommandLogExecute,0);
            _debugSession.cmdCommandLogExecute(threadManager.getThreadName(DU),(ERepCommandLogExecute)_rep);

      int whyStop = 0;

      switch (_debugSession.whyStop()) {
         case DebugSession.WS_BkptHit:
         case DebugSession.WS_UserBkptHit:
         case DebugSession.WS_HaltRequest:
            whyStop = EPDC.Why_break;
            break;

         case DebugSession.WS_Unknown:
         case DebugSession.WS_PgmQuit:
         case DebugSession.WS_PgmDetach:
         case DebugSession.WS_RemoteDebuggerIsDead:
            whyStop = EPDC.Why_done;
            break;

         case DebugSession.WS_UncaughtExceptionThrown:
         case DebugSession.WS_ExceptionThrown:
            whyStop = EPDC.Why_PgmExcept;
            break;

         case DebugSession.WS_ThreadDeath:
            whyStop = EPDC.Why_Other;
            break;
      }

      DU = threadManager.getThreadDU(_debugSession.stopThreadName());
      if (Gdb.traceLogger.DBG) 
          Gdb.traceLogger.dbg(1,"Active thread DU="+DU);
      _rep = new ERepCommandLogExecute(DU, whyStop);

      // If we were notified of an exception, we simply step into the 
      // exception handler (catch block) and let the user decide what to
      // do next (Run or Step)
      if (_debugSession.whyStop() == DebugSession.WS_ExceptionThrown ||
          _debugSession.whyStop() == DebugSession.WS_UncaughtExceptionThrown)
      {
         //debugSession.cmdStep(debugSession.stopThreadName(), false);
         ((ERepCommandLogExecute) _rep).setExceptionMsg(_debugSession.exceptionName() ); 
      }

      // update classes and threads if program did not quit
      if (whyStop == EPDC.Why_done) 
      {
         // The debuggee is done.  Make sure we don't send any change packets
         // to the front end since we are sending a why stop of DONE.
         _debugSession.clearManagers();

         // Create a new reply since we may have forced a quit.  (!!! Should
         // change epdc ERepCommandLogExecute to let you set whystop yourself)
         _rep = new ERepCommandLogExecute(DU, whyStop);

	 switch (_debugSession.whyStop())
         {
//	 case DebugSession.WS_UncaughtExceptionThrown:
//	   _rep.setMessage(_debugSession.getResourceString("UNCAUGHT_EXCEPTION_MSG") 
//                            + "'" + _debugSession.exceptionName() + "'");
//	   break;

	 case DebugSession.WS_Unknown:
	 case DebugSession.WS_RemoteDebuggerIsDead:
	   _rep.setMessage(_debugSession.getResourceString("UNKNOWN_PROGRAM_TERMINATION_MSG"));
	   break;

	 case DebugSession.WS_PgmDetach:
	   _rep.setMessage(_debugSession.getResourceString("PROGRAM_DETACHED_MSG"));
	   break;

         default:
	   _rep.setMessage(_debugSession.getResourceString("PROGRAM_DONE_MSG"));
	   break;
         }
      }
      else
      {
         // moduleManager.updateModules();
         variableMonitorManager.updateMonitors();
      }
      return false;
   }

   // Class fields
   private EReqCommandLogExecute _req;
}
