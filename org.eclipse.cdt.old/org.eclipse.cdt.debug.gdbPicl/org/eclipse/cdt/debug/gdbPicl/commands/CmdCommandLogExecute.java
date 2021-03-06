/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.BreakpointManager;
import org.eclipse.cdt.debug.gdbPicl.DebugSession;
import org.eclipse.cdt.debug.gdbPicl.Gdb;
import org.eclipse.cdt.debug.gdbPicl.GdbDebugSession;
import org.eclipse.cdt.debug.gdbPicl.LocalVariablesMonitorManager;
import org.eclipse.cdt.debug.gdbPicl.ModuleManager;
import org.eclipse.cdt.debug.gdbPicl.ThreadManager;
import org.eclipse.cdt.debug.gdbPicl.VariableMonitorManager;
import org.eclipse.cdt.debug.gdbPicl.objects.ThreadComponent;

import com.ibm.debug.epdc.EPDC;
import com.ibm.debug.epdc.EPDC_EngineSession;
import com.ibm.debug.epdc.ERepCommandLog;
import com.ibm.debug.epdc.EReqCommandLog;

/**
 * Process CommandLogExecute command
 */
public class CmdCommandLogExecute extends Command {
	public CmdCommandLogExecute(DebugSession debugSession, EReqCommandLog req) {
		super(debugSession);
		_req = req;
	}

	/**
	 * execute program as specified
	 */

	public boolean execute(EPDC_EngineSession EPDCSession) {

		//////////////////////////////////////////////////////////////////////////////////////
		String commandString = _req.getString();
		if (commandString == null)
			commandString = " ";
		if (Gdb.traceLogger.EVT)
			Gdb.traceLogger.evt(
				1,
				"===>>> CmdCommandLogExecute _commandString=" + commandString);
		int whyStopXX = EPDC.Why_none;
		//int dU = 1234;

		ThreadManager threadManager = _debugSession.getThreadManager();
		int dU = threadManager.getThreadDU(_debugSession.stopThreadName());

		if (dU > 0) {
			_rep = new ERepCommandLog();
			//      //_rep.setMessage( null );  //"GDB..GDB..GDB..GDB" );
			//    ((ERepCommandLogExecute)_rep).setExceptionMsg( null ); //"GDB..EXCEPTION..GDB" );
			//    ((ERepCommandLogExecute)_rep).addResponseLine("Response Line #1" );
			//    ((ERepCommandLogExecute)_rep).addResponseLine("Response Line #2" );
			//            debugSession.setLastUserCmd(DebugSession.CommandLogExecute,0);
			((GdbDebugSession) _debugSession).cmdCommandLogExecute(
				commandString,
				(ERepCommandLog) _rep);
		}

		if (true)
			return false;
		//////////////////////////////////////////////////////////////////////////////////////

		// Note: Front end will send 0 if EPDC.Exec_Run was selected
		int DU = 0; //BCS _req.getDU();

		//      ThreadManager                threadManager                = _debugSession.getThreadManager();
		ModuleManager classManager = _debugSession.getModuleManager();
		BreakpointManager breakpointManager = _debugSession.getBreakpointManager();
		VariableMonitorManager variableMonitorManager =
			_debugSession.getVariableMonitorManager();
		LocalVariablesMonitorManager localVariablesMonitorManager =
			_debugSession.getLocalVariablesMonitorManager();

		if (DU > 0) {
			ThreadComponent tc = threadManager.getThreadComponent(DU);
			if (tc != null && tc.isBlocked()) {
				_rep = new ERepCommandLog();
				_rep.setReturnCode(EPDC.ExecRc_ThreadBlocked);
				_rep.setMessage(_debugSession.getResourceString("CANT_STEP_BLOCKED_THREAD"));
				return false;
			}
		}

		//BCS      EStdView view = _req.getViewInfo();
		//BCS      int partID = view.getPPID();
		//BCS      int lineNum = view.getLineNum();

		_debugSession.setLastUserCmd(DebugSession.CommandLogExecute, 0);
		_debugSession.cmdCommandLogExecute(
			threadManager.getThreadName(DU),
			(ERepCommandLog) _rep);

		int whyStop = 0;

		switch (_debugSession.whyStop()) {
			case DebugSession.WS_BkptHit :
			case DebugSession.WS_UserBkptHit :
			case DebugSession.WS_HaltRequest :
				whyStop = EPDC.Why_break;
				break;

			case DebugSession.WS_Unknown :
			case DebugSession.WS_PgmQuit :
			case DebugSession.WS_PgmDetach :
			case DebugSession.WS_RemoteDebuggerIsDead :
				whyStop = EPDC.Why_done;
				break;

			case DebugSession.WS_UncaughtExceptionThrown :
			case DebugSession.WS_ExceptionThrown :
				whyStop = EPDC.Why_PgmExcept;
				break;

			case DebugSession.WS_ThreadDeath :
				whyStop = EPDC.Why_Other;
				break;
		}

		DU = threadManager.getThreadDU(_debugSession.stopThreadName());
		if (Gdb.traceLogger.DBG)
			Gdb.traceLogger.dbg(1, "Active thread DU=" + DU);
		_rep = new ERepCommandLog();

		// If we were notified of an exception, we simply step into the 
		// exception handler (catch block) and let the user decide what to
		// do next (Run or Step)
		if (_debugSession.whyStop() == DebugSession.WS_ExceptionThrown
			|| _debugSession.whyStop() == DebugSession.WS_UncaughtExceptionThrown) {
			//debugSession.cmdStep(debugSession.stopThreadName(), false);
			 ((ERepCommandLog) _rep).setMessage(_debugSession.exceptionName());
		}

		// update classes and threads if program did not quit
		if (whyStop == EPDC.Why_done) {
			// The debuggee is done.  Make sure we don't send any change packets
			// to the front end since we are sending a why stop of DONE.
			_debugSession.clearManagers();

			// Create a new reply since we may have forced a quit.  (!!! Should
			// change epdc ERepCommandLogExecute to let you set whystop yourself)
			_rep = new ERepCommandLog();

			switch (_debugSession.whyStop()) {
				//	 case DebugSession.WS_UncaughtExceptionThrown:
				//	   _rep.setMessage(_debugSession.getResourceString("UNCAUGHT_EXCEPTION_MSG") 
				//                            + "'" + _debugSession.exceptionName() + "'");
				//	   break;

				case DebugSession.WS_Unknown :
				case DebugSession.WS_RemoteDebuggerIsDead :
					_rep.setMessage(
						_debugSession.getResourceString("UNKNOWN_PROGRAM_TERMINATION_MSG"));
					break;

				case DebugSession.WS_PgmDetach :
					_rep.setMessage(_debugSession.getResourceString("PROGRAM_DETACHED_MSG"));
					break;

				default :
					_rep.setMessage(_debugSession.getResourceString("PROGRAM_DONE_MSG"));
					break;
			}
		} else {
			// moduleManager.updateModules();
			variableMonitorManager.updateMonitors();
		}
		return false;
	}

	// Class fields
	private EReqCommandLog _req;
}