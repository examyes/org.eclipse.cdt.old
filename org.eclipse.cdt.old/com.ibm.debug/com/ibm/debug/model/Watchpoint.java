package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/Watchpoint.java, java-model, eclipse-dev, 20011128
// Version 1.12.1.2 (last modified 11/28/01 16:11:18)
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

public class Watchpoint extends EventBreakpoint
{
  Watchpoint(DebuggeeProcess owningProcess, ERepGetNextBkp epdcBkp)
  {
    super(owningProcess, epdcBkp);

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Setting Storage Change Breakpoint");
  }

  public String getExpression()
  {
    return _epdcBkp.getEntryName();
  }

  /**
   * Return the computed address that is used in the change address
   * breakpoint.
   */
  public String getAddress()
  {
    return _epdcBkp.getAddress();
  }

  /**
   * Return the number of bytes to watch in the change address breakpoint
   */
  public int getByteCount()
  {
    return _epdcBkp.getByteCount();
  }

  /**
   * Send a request to modify the breakpoint that was set when a
   * particular address or an expression (that would result into an address)
   * was changed.
    * @param addrOrExpr the address or expression as typed by the user
    * @param stopEvery the number of times the breakpoint should stop
    * the execution when the breakpoint is hit
    * @param from the breakpoint should stop execution if the number of
    * times the breakpoint is hit is greater that 'from'.
    * @param to the breakpoint should stop execution if the number of
    * times the breakpoint is hit is less than 'to'.
    * @parm byteCount the number of bytes to watch, depending on the
    * language the range of the byteCount will vary. For example, for
    * C++ the maximum byte count is 4. This means that the byteCount to
    * watch for an address could be set to 1, 2, or 4 bytes.
    * @param threadID The id of the thead where the breakpoint will be
    * evaluated. The thread id of zero represents every possible thread.
    * @param sendReceievControlFlags this flag indicates the state in
    * which the request is to be sent.
    * @return 'true' if the request to change the breakpoint was sent
    * successfully, and 'false' otherwise.
    * @exception java.io.IOException if there is a communication problem
    * with the debug engine, this exception occurs.
    * @see Watchpoint
    * @see EventBreakpoint
    * @see Breakpoint
    */
  public boolean modify(String addrOrExpr,
                        int stopEvery, int from, int to,
                        int byteCount,
                        int threadID,
                        int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return modify(addrOrExpr,
                  stopEvery, from, to,
                  byteCount,
                  threadID,
                  sendReceiveControlFlags,
                  null);
  }

  public boolean modify(String addrOrExpr,
                        int stopEvery, int from, int to,
                        int byteCount,
                        int threadID,
                        int sendReceiveControlFlags,
                        Object property)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2,"EventBreakpoint.modifyWatchpoint : AddrOrExpr=" + addrOrExpr);

    DebugEngine debugEngine = getOwningProcess().debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_BreakpointEvent,
                                          sendReceiveControlFlags))
        return false;

    // If the breakpoint cannot be modified cancel the request
    if (Model.checkFCTBit)
    {
        if (!debugEngine.getCapabilities().getBreakpointCapabilities().breakpointModifySupported() || isReadOnly())
        {
            debugEngine.cancelEPDCRequest(EPDC.Remote_BreakpointEvent);
            return false;
        }
    }

    EEveryClause everyClause;

    if (stopEvery == 0 && from == 0 && to == 0)
        everyClause = null;
    else
        everyClause = new EEveryClause(stopEvery, to, from);

    if (!debugEngine.processEPDCRequest(new EReqBreakpointWatchpoint(
                                                   EPDC.BkpEnable,
                                                   everyClause,
                                                   addrOrExpr,
                                                   null, null, null,
                                                   null,
                                                   byteCount,
                                                   null,
                                                   threadID,
                                                   _epdcBkp.getID(),
                                                   _epdcBkp.getAddress()),
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
    return targetProcess.setWatchpoint((short)(EPDC.BkpDefer |
                                       (isEnabled() ? EPDC.BkpEnable : 0)),
                                       _epdcBkp.getEntryName(), // the expr
				       getEveryVal(),
				       getFromVal(),
				       getToVal(),
                                       _epdcBkp.getByteCount(),
                                       getThreadID(),
                                       _epdcBkp.getDLLName(),
                                       _epdcBkp.getSourceName(),
                                       _epdcBkp.getFileName(),
                                       _epdcBkp.getAddress(),
                                       sendReceiveControlFlags,
                                       property);
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
       printWriter.println();
       printWriter.println("BP Type: Storage Change. ");
       super.print(printWriter);
       printWriter.println("Byte Count: " + getByteCount());
       printWriter.println("Address/Expression: " + getExpression());
       printWriter.println("Computed Address: " + getAddress());
    }
  }
}
