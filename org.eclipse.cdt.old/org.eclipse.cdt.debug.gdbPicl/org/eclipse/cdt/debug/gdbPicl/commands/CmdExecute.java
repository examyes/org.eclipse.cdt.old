/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.commands;
import org.eclipse.cdt.debug.gdbPicl.*;
import org.eclipse.cdt.debug.gdbPicl.objects.*;

import com.ibm.debug.epdc.*;
import java.util.*;

/**
 * Process execute command
 */
public class CmdExecute extends Command
{
   public CmdExecute(DebugSession debugSession, EReqExecute req)
   {
      super(debugSession);
      _req = req;
   }

   /**
    * Execute program as specified
    */

   public boolean execute(EPDC_EngineSession EPDCSession)
   {
      // Note: Front end will send 0 if EPDC.Exec_Run was selected
      int DU = _req.getDU();

      ThreadManager                threadManager                = _debugSession.getThreadManager();
      ModuleManager                classManager                 = _debugSession.getModuleManager();
      BreakpointManager            breakpointManager            = _debugSession.getBreakpointManager();
      VariableMonitorManager       variableMonitorManager       = _debugSession.getVariableMonitorManager();
      LocalVariablesMonitorManager localVariablesMonitorManager = _debugSession.getLocalVariablesMonitorManager();
     int gdbID = 0;

      if (DU > 0)
      {
         ThreadComponent tc = threadManager.getThreadComponent(DU);
         if (tc != null && tc.isBlocked())
         {
            _rep = new ERepExecute(0, 0);
            _rep.setReturnCode(EPDC.ExecRc_ThreadBlocked);
            _rep.setMessage(_debugSession.getResourceString("CANT_STEP_BLOCKED_THREAD"));
            return false;
         }
      }

      EStdView view = _req.getViewInfo();
      int partID = view.getPPID();
      int lineNum = view.getLineNum();
      int viewNo = view.getViewNo();
      int line;

      try
      {
         switch (_req.getHowExecute() )
         {
            case EPDC.Exec_Go:
            case EPDC.Exec_GoExceptionRun:
               _debugSession.setLastUserCmd(DebugSession.CmdRun,0);
               _debugSession.cmdRun_User();
               break;
 
            case EPDC.Exec_GoBypass:
               _debugSession.setLastUserCmd(DebugSession.CmdExamineException,0);
               _debugSession.cmdExamineException(threadManager.getThreadName(DU));
               break;

            case EPDC.Exec_GoException:
               _debugSession.setLastUserCmd(DebugSession.CmdStepException,0);
               _debugSession.cmdStepException(threadManager.getThreadName(DU));
               break;

            case EPDC.Exec_StepInto:
               _debugSession.setLastUserCmd(DebugSession.CmdStepInto, threadManager.getCallStackSize(DU));
               ((GdbDebugSession)_debugSession).cmdStep(threadManager.getThreadName(DU), false, viewNo);
               break;

            case EPDC.Exec_GoTo:
               _debugSession.setLastUserCmd(DebugSession.CmdGoTo,threadManager.getCallStackSize(DU));
               
				if (viewNo == Part.VIEW_SOURCE) {
					_debugSession.cmdGoTo(threadManager.getThreadName(DU), String.valueOf(lineNum));
				}
				else  // MIXED AND DISASSEMBLY VIEW
				{
			   		Part part = classManager.getPart(partID);
			   		View viewInfo = part.getView(viewNo);		   		
					String lineInfo = viewInfo.getViewLine(lineNum);
					String address = null;
					
					lineInfo = lineInfo.trim();
					
					// get address from view
					if (lineInfo.startsWith("0x"))
					{
						int idx = lineInfo.indexOf(" ");
						if (idx > 0)
						{
							address = lineInfo.substring(0, _viewPrefix);
						}
					}
					else
					{
						// get next executable line in mixed view
						for (int x=lineNum+1; x<viewInfo.getViewNumLines(); x++)
						{
							lineInfo = viewInfo.getViewLine(x);
							lineInfo = lineInfo.trim();
							
							if (lineInfo.startsWith("0x"))
							{
								int idx = lineInfo.indexOf(" ");
								if (idx > 0)
								{
									address = lineInfo.substring(0, _viewPrefix);
								}
								break;
							}
						}						
					}
					((GdbDebugSession)_debugSession).cmdGoToAddress(address);										
				}
				
               break;

            case EPDC.Exec_StepOver:
               _debugSession.setLastUserCmd(DebugSession.CmdStepOver, threadManager.getCallStackSize(DU));               
               
			  String filename = ((GdbDebugSession)_debugSession).getCurrentFileName();
			  
			  try
			  {
				  line =  Integer.parseInt(((GdbDebugSession)_debugSession).getCurrentLineNumber());
			  }
			  catch (java.lang.NumberFormatException e)
			  {
			  	  line = 0;
			  }
			  
			  if (line > 0)
			  {
			  	line ++;
			  }
			  boolean needClear = false;
			  boolean needReset = false;

			  if (Gdb.supportDeferredBreakpoint && ((GdbDebugSession)_debugSession).getGdbProcess().stopOnSharedLibEvents())
			  {
			  	
			  	if (viewNo == Part.VIEW_SOURCE)
			  	{
				  	// set line breakpoint
				  	gdbID = ((GdbDebugSession)_debugSession).setLineBreakpoint(filename, line);
				  	
				  	if (gdbID > 0)
				  	{
				  		needClear = true;			  		
				  	}
				  	else
				 	{
				 		// breakpoint not successfully set
				 		// should unset stop at shared lib event, otherwise, bad results
				   		// the side effect of this is that some deferred breakpoints may not be set 
				   		// manually try to enable the breakpoints if an error occurs
				   		
	   		      if (Gdb.traceLogger.EVT) 
					          Gdb.traceLogger.evt(1,"+++ Error setting bkpt at: " + filename + "," + line + " - unset solib event");
				 		((GdbDebugSession)_debugSession).getGdbProcess().setStopOnSharedLibEvents(false);
				 		needReset = true;
				 	}
			  	}
			  	else
			  	{
			  		// if in mixed or disassembly view, turn shared lib events off.
			 		((GdbDebugSession)_debugSession).getGdbProcess().setStopOnSharedLibEvents(false);
			  		needReset = true;
			  	}
			  }
               
			   ((GdbDebugSession)_debugSession).cmdStep(threadManager.getThreadName(DU), true, viewNo);
			   
			   if (Gdb.supportDeferredBreakpoint && needClear)
			   {
			   	((GdbDebugSession)_debugSession).clearBreakpoint(gdbID);
			   }
			   
			   if (Gdb.supportDeferredBreakpoint && needReset)
			   {
			   		// reset for other step over
			   		
    		      if (Gdb.traceLogger.EVT) 
				          Gdb.traceLogger.evt(1,"+++ Reset solib event");

			   		
			 		((GdbDebugSession)_debugSession).getGdbProcess().setStopOnSharedLibEvents(true);
			 		((GdbDebugSession)_debugSession).enableDeferredBreakpoints();
			   }
			   
              break;

          case EPDC.Exec_Step:
               _debugSession.setLastUserCmd(DebugSession.CmdStepDebug, threadManager.getCallStackSize(DU));
               _debugSession.cmdStepDebug(threadManager.getThreadName(DU));
               break;

          case EPDC.Exec_StepReturn:
               _debugSession.setLastUserCmd(DebugSession.CmdStepReturn, threadManager.getCallStackSize(DU));

			  String returnFile = "";
			  line = 0;
			  needClear = false;
			  needReset = false;
			  
			if (Gdb.supportDeferredBreakpoint
				&& ((GdbDebugSession) _debugSession).getGdbProcess().stopOnSharedLibEvents()) {
					
				if (viewNo == Part.VIEW_SOURCE)	
				{
					ThreadComponent tc = threadManager.getThreadComponent(DU);
					GdbStackFrame[] callStack = ((GdbThreadComponent) tc).getCallStack();
	
					returnFile = callStack[1].getFileName();
	
					try {
						line = callStack[1].getLineNumber() + 1;
					} catch (java.lang.NumberFormatException e) {
						line = 0;
					}
	
					// set breakpint
					gdbID = ((GdbDebugSession) _debugSession).setLineBreakpoint(returnFile, line);
	
					if (gdbID > 0) {
						needClear = true;
					} else {
						// breakpoint not successfully set
						// should unset stop at shared lib event, otherwise, bad results
						// the side effect of this is that some deferred breakpoints may not be set 
						// manually try to enable the breakpoints if an error occurs
						if (Gdb.traceLogger.EVT)
							Gdb.traceLogger.evt(
								1,
								"+++ Error setting bkpt at: "
									+ returnFile
									+ ","
									+ line
									+ " - unset solib event");
	
						((GdbDebugSession) _debugSession).getGdbProcess().setStopOnSharedLibEvents(false);
						needReset = true;
					}
				}
				else
				{
					((GdbDebugSession) _debugSession).getGdbProcess().setStopOnSharedLibEvents(false);
					needReset = true;					
				}
			}
               _debugSession.cmdStepReturn_User(threadManager.getThreadName(DU));
               
				if (Gdb.supportDeferredBreakpoint && needClear) {
					((GdbDebugSession) _debugSession).clearBreakpoint(gdbID);
				}
	
				if (Gdb.supportDeferredBreakpoint && needReset) {
					// reset for other step over
    		      if (Gdb.traceLogger.EVT) 
				          Gdb.traceLogger.evt(1,"+++ Reset solib event");
										
					((GdbDebugSession) _debugSession).getGdbProcess().setStopOnSharedLibEvents(true);
			 		((GdbDebugSession)_debugSession).enableDeferredBreakpoints();						
				}

               
             break;

          case EPDC.Exec_RunToCursor:
               _debugSession.setLastUserCmd(DebugSession.CmdRun, threadManager.getCallStackSize(DU));
               // if a breakpoint already exists at the run-to location,
               // then just do a normal execute otherwise, create the
               // breakpoint, run, then delete the breakpoint
               if (Gdb.traceLogger.DBG) 
                   Gdb.traceLogger.dbg(1,"Running to location: part " + Integer.toString(partID) + ", line " +
                        Integer.toString(lineNum));
                        
			   if (viewNo == Part.VIEW_SOURCE)  // SOURCE VIEW
			   {
					if (breakpointManager.isLocationBreakpoint(partID, lineNum)) {
						_debugSession.cmdRun_User();
					} else {
						// attempt to set breakpoint
						if (_debugSession.setLineBreakpoint(partID, lineNum) < 0) {
							_rep = new ERepExecute(0, 0);
							_rep.setReturnCode(EPDC.ExecRc_BadLineNum);
							_rep.setMessage(_debugSession.getResourceString("LINE_NOT_EXECUTABLE_MSG"));
							return false;
						}
						_debugSession.setTmpBkpt(partID, lineNum);
	
						_debugSession.setLastUserCmd(DebugSession.CmdRun, 0);
						_debugSession.cmdRun_User();
	
						_debugSession.clearBreakpoint(partID, lineNum);
						_debugSession.clearTmpBkpt();
					}
					break;
			   }
			   else  // MIXED VIEW OR DISASSEMBLY VIEW
			   {
			   		Part part = classManager.getPart(partID);
			   		View viewInfo = part.getView(viewNo);		   		
					String lineInfo = viewInfo.getViewLine(lineNum);
					String address = null;
					
					lineInfo = lineInfo.trim();
					
					// get address from view
					if (lineInfo.startsWith("0x"))
					{
						int idx = lineInfo.indexOf(" ");
						if (idx > 0)
						{
							address = lineInfo.substring(0, _viewPrefix);
						}
					}
					else
					{
						// get next executable line in mixed view
						for (int x=lineNum+1; x<viewInfo.getViewNumLines(); x++)
						{
							lineInfo = viewInfo.getViewLine(x);
							lineInfo = lineInfo.trim();
							
							if (lineInfo.startsWith("0x"))
							{
								int idx = lineInfo.indexOf(" ");
								if (idx > 0)
								{
									address = lineInfo.substring(0, _viewPrefix);
								}
								break;
							}
						}						
					}
					
					int id = ((GdbDebugSession)_debugSession).setAddressBreakpoint(address);
					
					if (id < 0)
					{
	                     _rep = new ERepExecute(0, 0);
	                     _rep.setReturnCode(EPDC.ExecRc_BadLineNum);
	                     _rep.setMessage(_debugSession.getResourceString("LINE_NOT_EXECUTABLE_MSG"));
	                     return false;
					}
					
					_debugSession.setLastUserCmd(DebugSession.CmdRun,0);
					_debugSession.cmdRun_User();
					((GdbDebugSession)_debugSession).clearBreakpoint(id);
					break;
			   }

            default:
               System.err.println("This execution not implemented.");
               Gdb.handleException(new Exception("Bad execution command"));
         }

         DU = threadManager.getThreadDU(_debugSession.stopThreadName());
         if (Gdb.traceLogger.DBG) 
             Gdb.traceLogger.dbg(1,"Active thread DU="+DU);
      }
      catch (DebuggeeTerminatedError error)
      {
      if (Gdb.traceLogger.EVT) 
          Gdb.traceLogger.evt(1,"CmdExecute DebuggeeTerminatedError="+error.getMessage());
         DU = 0;
      }

      int whyStop = 0;

      switch (_debugSession.whyStop()) {
         case DebugSession.WS_BkptHit:
         case DebugSession.WS_TmpBkptHit:
         case DebugSession.WS_HaltRequest:
            whyStop = EPDC.Why_none;
            break;

         case DebugSession.WS_UserBkptHit:
         case DebugSession.WS_UserBkptCondErr:   // stopped because of error
                                                 // on conditional breakpoint
            whyStop = EPDC.Why_break;
            break;

         case DebugSession.WS_Watchpoint:
            whyStop = EPDC.Why_Watchpoint;
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

      _rep = new ERepExecute(DU, whyStop);

      if (_debugSession.whyStopMsg() != null)
         _rep.setMessage(_debugSession.whyStopMsg());

      // If we were notified of an exception, we simply step into the
      // exception handler (catch block) and let the user decide what to
      // do next (Run or Step)
      if (_debugSession.whyStop() == _debugSession.WS_ExceptionThrown ||
          _debugSession.whyStop() == _debugSession.WS_UncaughtExceptionThrown)
      {
         //debugSession.cmdStep(debugSession.stopThreadName(), false);
         ((ERepExecute) _rep).setExceptionMsg(_debugSession.exceptionName() );
      }

      // update classes and threads if program did not quit
      if (whyStop == EPDC.Why_done)
      {
         // The debuggee is done.  Make sure we don't send any change packets
         // to the front end since we are sending a why stop of DONE.
         _debugSession.clearManagers();

         // Create a new reply since we may have forced a quit.  (!!! Should
         // change epdc ERepExecute to let you set whystop yourself)
         _rep = new ERepExecute(DU, whyStop);

         switch (_debugSession.whyStop())
         {
	    //case _debugSession.WS_UncaughtExceptionThrown:
	    //  _rep.setMessage(_debugSession.getResourceString("UNCAUGHT_EXCEPTION_MSG")
            //                   + "'" + _debugSession.exceptionName() + "'");
	    //  break;

	    case DebugSession.WS_Unknown:
	    case DebugSession.WS_RemoteDebuggerIsDead:
	      _rep.setMessage(_debugSession.getResourceString("UNRECOVERABLE_JVM_MSG"));
	      break;

	    case DebugSession.WS_PgmDetach:
	      _rep.setMessage(_debugSession.getResourceString("PROGRAM_DETACHED_MSG"));
	      break;

       default:
         String msg = _debugSession.exceptionName();
         if(msg!=null)
         {
            if (Gdb.traceLogger.EVT) 
                Gdb.traceLogger.evt(1,"CmdExecute.execute whyStop==EPDC.Why_done _rep.getMessage="+_rep.messageText() );
            ((ERepExecute) _rep).setExceptionMsg( msg );
            _rep.setMessage( msg );
         }
         else
         {
            _rep.setMessage(_debugSession.getResourceString("PROGRAM_DONE_MSG"));
         }
         break;
         }
      }
      else
      {
         // If the program stopped because of user breakpoints or user
         // watchpoints, list them on the reply to the Remote_Execute request.
         if (whyStop == EPDC.Why_break || whyStop == EPDC.Why_Watchpoint)
         {
           Vector breakpointsHit = _debugSession.breakpointsHit();

           if (breakpointsHit != null)
           {
             int numberOfBreakpointsHit = breakpointsHit.size();

             for (int i = 0; i < numberOfBreakpointsHit; i++)
             {
               Breakpoint bp = (Breakpoint)breakpointsHit.elementAt(i);
   
               ((ERepExecute)_rep).addBreakpoint(bp.bkpID(),
                                                 (byte)bp.bkpType());
             }
           }
         }

//GDB    classManager.updateClasses();
         variableMonitorManager.updateMonitors();
      }
        
      return false;
   }

   // Class fields
   private EReqExecute _req;
   private int _viewPrefix = 10;
}
