package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/EntryBreakpoint.java, java-model, eclipse-dev, 20011128
// Version 1.20.1.2 (last modified 11/28/01 16:11:17)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;
import java.io.*;

/**
 * This class represents a breakpoint that has been set on the entry point
 * of a function.
 */

public class EntryBreakpoint extends LocationBreakpoint
{
  EntryBreakpoint(DebuggeeProcess owningProcess, ERepGetNextBkp epdcBkp)
  {
    super(owningProcess, epdcBkp);

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Creating EntryBreakpoint : ID=" + epdcBkp.getID());
  }

  /**
   * Modify a current function breakpoint. This method can be used to modify
   * an existing entry breakpoint (that is not deferred). By using this
   * method the model will guarantee that the breakpoint id will remain the
   * same after the breakpoint information (such as changing function name or
   * changing the conditional expression) is changed.
   * If this method is used to defer an existing active function the request
   * will be canceled.
   * The information that can be changed using this method are:
   * 1) the name of the module, part or source of a breakpoint. Any of these
   *    file name changes may result in changing the current set of possible
   *    functions that exist in a given file.
   * 2) the name of funtion, the name has to be within the list of function
   *    of a file
   * 3) Any of the optional parameters (from, to, every, conditional
   *    expression) except changing the breakpoint to be deferred.
   * @param function A function object that contains the information about
   * the breakpoint we are about to change.
   * @param stopEvery the number of times the breakpoint should stop
   * the execution when the breakpoint is hit
   * @param from the breakpoint should stop execution if the number of
   * times the breakpoint is hit is greater that 'from'.
   * @param to the breakpoint should stop execution if the number of
   * times the breakpoint is hit is less than 'to'.
   * @param expr the conditional expression for this breakpoint
   * @param threadID the id of the thread where the breakpoint will be
   * evaluated. The thread id of zero represents every possible thread.
   * @param sendReceiveControlFlags the flag indicating the state in which the
   * request is to be sent(synchronized, asynchronized).
   * @return 'true' if the request to modify the breakpoint was sent
   * successfully, and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   * @see Breakpoint
   * @see LocationBreakpoint
   * @see Function
   */
  public boolean modify(Function function,
                        int stopEvery, int from, int to,
                        String expr,
                        int threadID,
                        int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return modify(function,
                  stopEvery, from, to,
                  expr,
                  threadID,
                  sendReceiveControlFlags,
                  null);
  }

  public boolean modify(Function function,
                        int stopEvery, int from, int to,
                        String expr,
                        int threadID,
                        int sendReceiveControlFlags,
                        Object property)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "EntryBreakpoint" + ".modify (non-deferred)");

    DebugEngine debugEngine = getOwningProcess().debugEngine();

    EStdView epdcContext = function.getLocation().getEStdView();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_BreakpointLocation,
                                           sendReceiveControlFlags))
       return false;

    // If the current engine does not support providing the breakpoint
    // with a conditional expression or modifying the breakpoint information,
    // cancel the request.

    if (Model.checkFCTBit)
    {
        EngineBreakpointCapabilities breakpointCapabilities = debugEngine.getCapabilities().getBreakpointCapabilities();

        if ((!breakpointCapabilities.conditionalBreakpointsSupported() &&
             expr != null) ||
            (!breakpointCapabilities.breakpointThreadsSupported() &&
             threadID != 0) ||
            (!breakpointCapabilities.breakpointFrequencySupported() &&
             !(stopEvery == 1 && to == 0 && from == 1)) ||
            (!breakpointCapabilities.breakpointModifySupported()) ||
            (isReadOnly()))
        {
             debugEngine.cancelEPDCRequest(EPDC.Remote_BreakpointLocation);
             return false;
        }
    }

    EEveryClause clause = null;

    // If no values have been specified for the every clause create one
    // with a set of defaults.
    if (!(stopEvery == 0 && from == 0 && to == 0))
        clause = new EEveryClause(stopEvery, to, from);

    // If the breakpoint is already enabled/disabled make sure it is remembered
    // when the request to modify breakpoint is sent.
    short attribute = (_epdcBkp.isEnabled()) ? EPDC.BkpEnable : (short)0;

    EStdExpression2 conditionalExpr = null;
    if (expr != null)
    {
        conditionalExpr = new EStdExpression2(epdcContext, expr,
                                              threadID, function.getId());
    }

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request Remote_BreakpointLocation");

    if (!debugEngine.processEPDCRequest(new EReqBreakpointEntry
                                                             (attribute,
                                                              clause,
                                                              null,
                                                              null,
                                                              null,
                                                              null,
                                                              conditionalExpr,
                                                              threadID,
                                                              _epdcBkp.getID(),
                                                              function.getId()),
                                        sendReceiveControlFlags,
                                        property))
       return false;
    else
       return true;
  }

  /**
   * Send a request to modify an existing deferred entry breakpoint. This
   * method cannot be used for modifying non-deferred breakpoints. Therefore,
   * if this method is used on a non-deferred line breakpoint, the request
   * will be canceled.
   * @param entryName The name of function to set the breakpoint to
   * @param moduleName The name of the executable or dll file
   * @param partName The name of the object file
   * @param stopEvery the number of times the breakpoint should stop
   * the execution when the breakpoint is hit
   * @param from the breakpoint should stop execution if the number of
   * times the breakpoint is hit is greater that 'from'.
   * @param to the breakpoint should stop execution if the number of
   * times the breakpoint is hit is less than 'to'.
   * @param expr the conditional expression for this breakpoint
   * @param threadID the id of the thread where the breakpoint will be
   * evaluated.  The thread id of zero represents every possible thread.
   * @param sendReceiveControlFlags the flag indicating the state in which the
   * request is to be sent(synchronized, asynchronized).
   * @return 'true' if the set breakpoint request was successfully sent to
   * the debug engine, 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   * @see Breakpoint
   * @see LocationBreakpoint
   * @see Function
   * @see EntryBreakpoint
   * @see DebuggeeProcess
   */
  public boolean modify(String entryName,
                        String moduleName,
                        String partName,
                        int stopEvery, int from, int to,
                        String expr,
                        int threadID,
                        int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return modify(entryName,
                  moduleName,
                  partName,
                  stopEvery, from, to,
                  expr,
                  threadID,
                  sendReceiveControlFlags,
                  null);
  }

  public boolean modify(String entryName,
                        String moduleName,
                        String partName,
                        int stopEvery, int from, int to,
                        String expr,
                        int threadID,
                        int sendReceiveControlFlags,
                        Object property)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, "EntryBreakpoint" + ".modify (deferred)");

    DebugEngine debugEngine = getOwningProcess().debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_BreakpointLocation,
                                           sendReceiveControlFlags))
       return false;

    // If the current engine does not support providing the breakpoint
    // with a conditional expression or modifying the breakpoint information,
    // cancel the request.
    // If the breakpoint is not deferred cancel the request.

    if (Model.checkFCTBit)
    {
        EngineBreakpointCapabilities breakpointCapabilities = debugEngine.getCapabilities().getBreakpointCapabilities();

        if ((!breakpointCapabilities.deferredBreakpointsSupported()) ||
            (!breakpointCapabilities.conditionalBreakpointsSupported() &&
             expr != null) ||
            (!breakpointCapabilities.breakpointThreadsSupported() &&
             threadID != 0) ||
            (!breakpointCapabilities.breakpointFrequencySupported() &&
             !(stopEvery == 1 && to == 0 && from == 1)) ||
            (!breakpointCapabilities.breakpointModifySupported()) ||
            (isReadOnly()))
        {
             debugEngine.cancelEPDCRequest(EPDC.Remote_BreakpointLocation);
             return false;
        }
    }

    // Is The current breakpoint enabled, or disabled?
    short attribute = _epdcBkp.getAttribute();

    if (!isDeferred())
        attribute = (short)(attribute ^ EPDC.BkpDefer);

    attribute = (short)(attribute ^
                 ( (_epdcBkp.isEnabled()) ? EPDC.BkpEnable : (short)0) );

    // If no values have been specified for the every clause create one
    // with a set of defaults.
    EEveryClause clause = null;

    if (!(stopEvery == 0 && from == 0 && to == 0))
        clause = new EEveryClause(stopEvery, to, from);

    // build the conditional expression if specified
    EStdExpression2 conditionalExpr = null;

    if (expr != null)
        conditionalExpr = new EStdExpression2(null, expr, threadID, 0);

    if (Model.TRACE.EVT && Model.traceInfo())
        Model.TRACE.evt(2, "Sending EPDC request Remote_BreakpointLocation");

    if (!debugEngine.processEPDCRequest(new EReqBreakpointEntry
                                                             (attribute,
                                                              clause,
                                                              entryName,
                                                              moduleName,
                                                              partName,
                                                              null,
                                                              conditionalExpr,
                                                              threadID,
                                                              _epdcBkp.getID(),
                                                              0),
                                        sendReceiveControlFlags,
                                        property))
        return false;
    else
        return true;
  }

  boolean restore(DebuggeeProcess targetProcess, int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return restore(targetProcess, sendReceiveControlFlags, null);
  }

  boolean restore(DebuggeeProcess targetProcess,
                  int sendReceiveControlFlags,
                  Object property)
  throws java.io.IOException
  {
    return targetProcess.setDeferredEntryBreakpoint(isEnabled(),
                                                    getFunctionName(),
                                                    getModuleName(),
                                                    getPartName(),
                                                    getThreadID(),
                                                    getEveryVal(),
                                                    getFromVal(),
                                                    getToVal(),
                                                    getExpression(),
                                                    sendReceiveControlFlags,
                                                    property);
  }

  public void print(PrintWriter printWriter)
  {
    printWriter.println();
    printWriter.println("BP Type: Function. ");
    super.print(printWriter);
  }
}
