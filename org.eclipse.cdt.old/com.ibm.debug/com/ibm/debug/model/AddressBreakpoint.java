package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/AddressBreakpoint.java, java-model, eclipse-dev, 20011128
// Version 1.20.1.2 (last modified 11/28/01 16:11:21)
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

public class AddressBreakpoint extends LocationBreakpoint
{
  AddressBreakpoint(DebuggeeProcess owningProcess, ERepGetNextBkp epdcBkp)
  {
    super(owningProcess, epdcBkp);

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(3, "Creating AddressBreakpoint : ID=" + epdcBkp.getID());
  }

  /**
   * Send a request to modify a breakpoint that was set at an address or
   * an expression (that would result into an address).
   * @param addrOrExpr the address or expression as typed by the user
   * @param location The context of the breakpoint. This parameter will be
   * needed if the conditional expression contains variable names. However,
   * if the expression is a literal (such as an address), no location is
   * necessary. Therefore, for the latter case a value of null can be
   * provided.
   * @param stopEvery the number of times the breakpoint should stop
   * the execution when the breakpoint is hit
   * @param from the breakpoint should stop execution if the number of
   * times the breakpoint is hit is greater that 'from'.
   * @param to the breakpoint should stop execution if the number of
   * times the breakpoint is hit is less than 'to'.
   * @param expr the conditional expression for this breakpoint
   * @param threadID The id of the thread where the breakpoint is to be
   * evaluated. The thread id of zero represents every possible thread.
   * @param sendReceiveControlFlags this flag indicates the state in
   * which the request is to be sent.
   * @return 'true' if the request to add the breakpoint was sent
   * successfully, and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   * @see AddressBreakpoint
   * @see LocationBreakpoint
   * @see Breakpoint
   */
  public boolean modify(String addrOrExpr,
                        Location loc,
                        int stopEvery, int from, int to,
                        String expr,
                        int threadID,
                        int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return modify(addrOrExpr,
                  loc,
                  stopEvery, from, to,
                  expr,
                  threadID,
                  sendReceiveControlFlags,
                  null);
  }

  public boolean modify(String addrOrExpr,
                        Location loc,
                        int stopEvery, int from, int to,
                        String expr,
                        int threadID,
                        int sendReceiveControlFlags,
                        Object property)
  throws java.io.IOException
  {
    DebugEngine debugEngine = getOwningProcess().debugEngine();

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
            !breakpointCapabilities.breakpointModifySupported() ||
            isReadOnly())
        {
            debugEngine.cancelEPDCRequest(EPDC.Remote_BreakpointLocation);
            return false;
        }
    }

    EEveryClause everyClause = null;

    // If no values have been specified for the every clause create one
    // with a set of defaults.
    if (!(stopEvery == 0 && from == 0 && to == 0))
        everyClause = new EEveryClause(stopEvery, to, from);

    EStdView context = null;
    if (loc != null)
        context = loc.getEStdView();

    EStdExpression2 conditionalExpr = null;
    conditionalExpr = new EStdExpression2(context, expr, threadID, 0);

    if (!debugEngine.processEPDCRequest(new EReqBreakpointAddress(
                                                   EPDC.BkpEnable,
                                                   everyClause,
                                                   addrOrExpr,
                                                   null, null, null,
                                                   conditionalExpr,
                                                   threadID,
                                                   _epdcBkp.getID(),
                                                   context),
                                        sendReceiveControlFlags,
                                        property))
        return false;
     else
        return true;
  }

  /**
   * Get the address at which this bkp is set.
   */

  public String getAddress()
  {
    return _epdcBkp.getAddress();
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
    return targetProcess.setAddressBreakpoint(isEnabled(),
                                              _epdcBkp.getAddress(),
                                              null,
                                              getEveryVal(),
                                              getFromVal(),
                                              getToVal(),
                                              getExpression(),
                                              getThreadID(),
                                              sendReceiveControlFlags,
                                              property);
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
       printWriter.println();
       printWriter.println("BP Type: Address. ");
       super.print(printWriter);
    }
  }
}
