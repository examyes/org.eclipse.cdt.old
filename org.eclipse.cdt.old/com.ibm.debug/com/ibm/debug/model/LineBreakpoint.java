package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/LineBreakpoint.java, java-model, eclipse-dev, 20011128
// Version 1.31.1.2 (last modified 11/28/01 16:11:19)
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
 * This class represents a breakpoint that has been set on a specific line
 * in a file.
 */

public class LineBreakpoint extends LocationBreakpoint
{
  LineBreakpoint(DebuggeeProcess owningProcess, ERepGetNextBkp epdcBkp)
  {
    super(owningProcess, epdcBkp);

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(3, "Creating LineBreakpoint : ID=" + epdcBkp.getID());
  }

  /**
   * Modify the current line breakpoint. This method can be used for modifying
   * an existing breakpoint (that is not deferred) on a line. By using this
   * method the model will guarantee that the breakpoint id will remain the
   * same after the breakpoint information (such as changing the line or
   * changing the conditional expression) is changed.
   * If this method is used to defer an existing active line the request
   * will be canceled.
   * @param location the location (file and line number) of the breakpoint
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
   */
  public boolean modify(Location location,
                        int stopEvery, int from, int to,
                        String expr,
                        int threadID,
                        int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return modify(location,
                  stopEvery, from, to,
                  expr,
                  threadID,
                  sendReceiveControlFlags,
                  null);
  }

  public boolean modify(Location location,
                        int stopEvery, int from, int to,
                        String expr,
                        int threadID,
                        int sendReceiveControlFlags,
                        Object property)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, "LineBreakpoint" + ".modify (non-deferred)");

    DebugEngine debugEngine = getOwningProcess().debugEngine();

    // get the context of the breakpoint
    EStdView epdcContext = location.getEStdView();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_BreakpointLocation,
                                           sendReceiveControlFlags))
       return false;

    // If the current engine does not support providing the breakpoint
    // with a conditional expression or modifying the breakpoint information,
    // cancel the request.

    EngineBreakpointCapabilities breakpointCapabilities = debugEngine.getCapabilities().getBreakpointCapabilities();

    if (Model.checkFCTBit)
    {
        if ((!breakpointCapabilities.conditionalBreakpointsSupported() &&
             expr != null) ||
            (!breakpointCapabilities.breakpointThreadsSupported() &&
             threadID != 0) ||
            (!breakpointCapabilities.breakpointFrequencySupported() &&
             !(stopEvery == 1 && to == 0 && from == 1)) ||
            (!breakpointCapabilities.breakpointModifySupported()) ||
            (isReadOnly()) )
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

    // Is The current breakpoint enabled, or disabled?
    short attribute = (_epdcBkp.isEnabled()) ? EPDC.BkpEnable : (short)0;

    EStdExpression2 conditionalExpr = null;
    if (expr != null)
        conditionalExpr = new EStdExpression2(epdcContext, expr, 0, 0);

    String stmtNumber = null;

    if (breakpointCapabilities.statementBreakpointSupported())
    {
        stmtNumber = Integer.toString(location.lineNumber());
    }

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request Remote_BreakpointLocation");

    if (!debugEngine.processEPDCRequest(new EReqBreakpointLine(attribute,
                                                               clause,
                                                               null,
                                                               null,
                                                               null,
                                                               conditionalExpr,
                                                               threadID,
                                                               _epdcBkp.getID(),
                                                               stmtNumber,
                                                               epdcContext),
                                        sendReceiveControlFlags,
                                        property))
       return false;
    else
       return true;
  }

  /**
   * Send a request to modify the current deferred line breakpoint. This
   * method cannot be used for modifying non-deferred breakpoints. Therefore,
   * if this method is used on a non-deferred line breakpoint, the request
   * will be canceled.
   * @param lineNumber The line at which the breakpoint is to be modified.
   * @param moduleName The name of the executable or dll file
   * @param partName The name of the object file
   * @param fileName The name of the source file
   * @param stopEvery the number of times the breakpoint should stop
   * the execution when the breakpoint is hit
   * @param from the breakpoint should stop execution if the number of
   * times the breakpoint is hit is greater that 'from'.
   * @param to the breakpoint should stop execution if the number of
   * times the breakpoint is hit is less than 'to'.
   * @param expr the conditional expression for this breakpoint
   * @param threadID The id of the thread where the breakpoint will be
   * evaluated. The thread id of zero represents every possible thread.
   * @param sendReceiveControlFlags the flag indicating the state in which the
   * request is to be sent(synchronized, asynchronized).
   * @return 'true' if the request to modify the breakpoint was sent
   * successfully to the debug engine, 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   * @see Breakpoint
   * @see LocationBreakpoint
   * @see ViewFile
   * @see DebuggeeProcess
   */
  public boolean modify(int lineNumber,
                        String moduleName,
                        String partName,
                        String fileName,
                        int stopEvery, int from, int to,
                        String expr,
                        int threadID,
                        int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return modify(lineNumber,
                  moduleName,
                  partName,
                  fileName,
                  stopEvery, from, to,
                  expr,
                  threadID,
                  sendReceiveControlFlags,
                  null);
  }

  public boolean modify(int lineNumber,
                        String moduleName,
                        String partName,
                        String fileName,
                        int stopEvery, int from, int to,
                        String expr,
                        int threadID,
                        int sendReceiveControlFlags,
                        Object property)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, "LineBreakpoint" + ".modify (deferred)");

    DebugEngine debugEngine = getOwningProcess().debugEngine();

    // get the context of the breakpoint
    EStdView epdcContext = new EStdView((short)0, (short)0, 0, lineNumber);

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_BreakpointLocation,
                                           sendReceiveControlFlags))
       return false;

    // If the current engine does not support deferred breakpoints, or
    // conditional expressions or modifying breakpoint information,
    // cancel the request.
    // If the current breakpoint is not deferred cancel the request.

    EngineBreakpointCapabilities breakpointCapabilities = debugEngine.getCapabilities().getBreakpointCapabilities();

    if (Model.checkFCTBit)
    {
        if ((!breakpointCapabilities.deferredBreakpointsSupported()) ||
            (!breakpointCapabilities.conditionalBreakpointsSupported() &&
             expr != null) ||
            (!breakpointCapabilities.breakpointThreadsSupported() &&
             threadID != 0) ||
            (!breakpointCapabilities.breakpointFrequencySupported() &&
             !(stopEvery == 1 && to == 0 && from == 1)) ||
            (!breakpointCapabilities.breakpointModifySupported()) ||
            (isReadOnly()) )
        {
             debugEngine.cancelEPDCRequest(EPDC.Remote_BreakpointLocation);
             return false;
        }
    }

    // Is The current breakpoint enabled, or disabled?
    short attribute = _epdcBkp.getAttribute();

    attribute = (short)(attribute ^
                 ( (_epdcBkp.isEnabled()) ? EPDC.BkpEnable : (short)0) );

    if (!_epdcBkp.isDeferred())
        attribute = (short)(attribute ^ EPDC.BkpDefer);

    // If no values have been specified for the every clause create one
    // with a set of defaults.
    EEveryClause clause = null;

    if (!(stopEvery == 0 && from == 0 && to == 0))
        clause = new EEveryClause(stopEvery, to, from);

    // build the conditional expression if specified
    EStdExpression2 conditionalExpr = null;

    if (expr != null)
        conditionalExpr = new EStdExpression2(epdcContext, expr, 0, 0);

    String stmtNumber = null;

    if (breakpointCapabilities.statementBreakpointSupported())
    {
        stmtNumber = Integer.toString(lineNumber);
    }

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request Remote_BreakpointLocation");

    if (!debugEngine.processEPDCRequest(new EReqBreakpointLine(attribute,
                                                               clause,
                                                               moduleName,
                                                               partName,
                                                               fileName,
                                                               conditionalExpr,
                                                               threadID,
                                                               _epdcBkp.getID(),
                                                               stmtNumber,
                                                               epdcContext),
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
    // We'll restore the bkp using the filename that was returned to us
    // by the debug engine at the time that the bkp was set. However, EPDC
    // does not tell us which view this file belongs to. For now, we'll
    // use the 1st view we find which is "line breakpoint-able":

    EStdView[] epdcLocations = _epdcBkp.getContexts();

    DebuggeeProcess owningProcess = getOwningProcess();
    DebugEngine engine = owningProcess.debugEngine();

    ViewInformation[] views = engine.supportedViews();
    ViewInformation view = null;

    for (int i = 0; i < views.length; i++)
        if (views[i] != null && views[i].isLineBreakpointCapable())
        {
           view = views[i];
           break;
        }

    if (view == null) // No view is line breakpoint-able
       return false;

    int lineNumber = epdcLocations[view.index()-1].getLineNum();

    if (targetProcess.debugEngine().getCapabilities().getBreakpointCapabilities().statementBreakpointSupported())
    {
        String stmtNum = _epdcBkp.getStatementNumber();
        if (stmtNum == null)
            return false;

        try
        {
            lineNumber = Integer.parseInt(stmtNum);
        }
        catch (java.lang.NumberFormatException excp)
        {
          System.out.println("Number Format Exception during restore");
          return false;
        }
    }

    // Restore this bkp into the target process:

    return targetProcess.setDeferredLineBreakpoint(isEnabled(),
                                                   lineNumber,
                                                   getFunctionName(),
                                                   getModuleName(),
                                                   getPartName(),
                                                   getFileName(),
                                                   getThreadID(),
                                                   getEveryVal(),
                                                   getFromVal(),
                                                   getToVal(),
                                                   getExpression(),
                                                   sendReceiveControlFlags,
                                                   property);

  }

  /**
   * Return the line number of a deferred line breakpoint. This method is
   * maily provided for the deferred line breakpoints because the Location
   * object for the deferred LineBreakpoint objects cannot be constructed
   * (the part, module, or file may be unknown at the time the deferred
   * LineBreakpoint is set). However, if this method is used for non-deferred
   * breakpoints or deferred function breakpoints, the line number will be
   * correct. For these breakpoints the line number should be obtained through
   * the Location object.
   * @param viewInformation A ViewInformation object which identifies a
   * particular kind of view (source, disassembly, etc.). The line number
   * returned will be a line number within a view of this type. Zero will be
   * returned if the breakpoint line number cannot be mapped to a line number
   * within this kind of view.
   *
   */
  public int getDeferredBreakpointLineNumber(ViewInformation viewInformation)
  {
    EStdView epdcLocation = getEPDCLocation(viewInformation);

    if (epdcLocation == null)
        return 0;

    return epdcLocation.getLineNum();

  }

  /*
   * Return the statement number (390 ONLY). If the debug engine does not
   * support statement breakpoints, or if the prefix area is null, the
   * statement number returned will be zero.
   */
  public int getStatementNumber()
  {
    String stmtField = _epdcBkp.getStatementNumber();

    if (stmtField == null)
        return 0;

    try
    {
       return Integer.parseInt(stmtField);
    }
    catch (java.lang.NumberFormatException excp)
    {
      System.out.println("Number Format Exception");
      return 0;
    }
  }

  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
       printWriter.println();
       printWriter.println("BP Type: Line. ");
       super.print(printWriter);
    }
  }
}
