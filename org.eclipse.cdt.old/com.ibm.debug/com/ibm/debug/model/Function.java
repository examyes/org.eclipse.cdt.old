package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/Function.java, java-model, eclipse-dev, 20011128
// Version 1.17.1.2 (last modified 11/28/01 16:11:53)
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
 * Function objects are contained within ViewFile objects.
 * <p>A ViewFile object will contain a list of Function objects,
 * which can be retrieved via the getFunctions() method.
 * @see ViewFile
 */


public class Function extends DebugModelObject
{
   Function(ViewFile owningFile, ERepEntryGetNext epdcEntry)
   {
     if (Model.TRACE.EVT && Model.traceInfo())
       Model.TRACE.evt(3, "Creating Function : Name=" + epdcEntry.getEntryName());

      _owningFile = owningFile;
      _epdcEntry = epdcEntry;
   }


   /**
    * Get the ViewFile which contains this function.
    */

   public ViewFile getFile()
   {
      return _owningFile;
   }

   /**
    * Get the entry id of this function.
    */

   int getId()
   {
      return _epdcEntry.getEntryID();
   }

   /**
    * Get the name of this function.
    */

   public String getName()
   {
      return _epdcEntry.getEntryName();
   }

   /**
    * Get the demangled name of this function.
    */

   public String getDemangledName()
   {
      return _epdcEntry.getDemangledName();
   }

   /**
    * Get the return type of this function.
    */

   public String getReturnType()
   {
      return _epdcEntry.getEntryReturnType();
   }


   /**
    * Get the location of this function
    */

   public Location getLocation()
   throws java.io.IOException
   {
      EStdView epdcLocation = _epdcEntry.getEStdView();

      // This should be impossible, but...

      if (epdcLocation == null)
         return null;

      int lineNumber = epdcLocation.getLineNum();

      // When a function is retrieved from the engine, it is possible for the
      // line number in the location of the function to be 0. We will make one
      // attempt to get the line number from the engine using the Remote_EntryWhere
      // request:

      if (lineNumber == 0 && !_fullyResolved)
      {
         _fullyResolved = true;

         DebugEngine debugEngine = _owningFile.view().part().module().process().debugEngine();

         // TODO: There's a small chance that the new location returned will
         // not be in the same view as the one we originally had
         // for this function. We should make sure we get the right one.

         EStdView resolvedEPDCLocation = debugEngine.resolveFunction(_epdcEntry);

         if (resolvedEPDCLocation != null &&
             (lineNumber = resolvedEPDCLocation.getLineNum()) != 0)
            epdcLocation.setLineNum(lineNumber);
      }

      return new Location(_owningFile, lineNumber);
   }

   public void print(PrintWriter printWriter)
   {
      printWriter.println("Function ID: " + getId());
      printWriter.println("Function Name: " + getName());
      printWriter.println("DemangledName: " + getDemangledName());
      printWriter.println("Return Type: " + getReturnType());
      printWriter.println("Function Location:");
      try
      {
        getLocation().print(printWriter);
      }
      catch (java.io.IOException excp)
      {
      }
      printWriter.println();
   }

  /**
   * Set a entry breakpoint at this function.
   * For setting function breakpoints from a Function object, the user does
   * not need to specify the names of module, part or file names. All that
   * the engine requires is the id of the function object to be passed to
   * set the breakpoint. There are two methods in the class for setting
   * function breakpoints. The current one is used when the user specifies
   * the optional parameters. The next method, is used when the ui provides
   * a predefined set of information for the optional parameters and the
   * user does not change them (therefore, the user does not need to specify
   * any of them).
   * @param isEnabled The attributes of a breakpoint which can be set
   * within a Function object are 'enable' or 'disable'. Therefore, if the
   * parameter is set to true the breakpoint attribute with be 'enable'
   * and 'disable' otherwise.
   * @param stopEvery the number of times the breakpoint should stop
   * the execution when the breakpoint is hit. The default value for this
   * parameter is 1.
   * @param from the breakpoint should stop execution if the number of
   * times the breakpoint is hit is greater that 'from'. The default value
   * for this parameter is 1.
   * @param to the breakpoint should stop execution if the number of
   * times the breakpoint is hit is less than 'to'. The default value for
   * this parameter is 0 (infinity).
   * @param expr the conditional expression for this breakpoint. The
   * conditional expression is not validated until the breakpoint is hit.
   * Therefore, an invalid expression cannot be detected at the time the
   * request is sent.
   * @param threadID The id of the DebuggeeThread object in which the
   * breakpoint is to be evaluated. If the value of zero is passed as
   * the thread id, the evaluation will be for every possible thread
   * available.
   * @param sendReceiveControlFlags A set of flags which specify the mode
   * in which this request is to be performed. There is a set of constants
   * in DebugEngine which define the possible values for this argument.
   * For example, a value of DebugEngine.sendReceiveDefault means that the
   * request is to be done asynchronously, while a value of
   * DebugEngine.sendReceiveSynchronously
   * means that the request is to be performed synchronously.
   * <p>When done asynchronously,
   * this method will return immediately after sending the request to the
   * debug engine without waiting for a response from
   * the debug engine. The response to the request will be
   * received on a separate thread and client code will be notified of the
   * the response via the event listener mechanism.
   * @return 'true' if the set breakpoint request was successfully sent to
   * the debug engine, 'false' otherwise. Note that a return value of 'true'
   * does not imply that the debug engine was able to set the breakpoint but
   * rather simply that the request was successfully sent to the debug engine.
   * Whether or not the breakpoint was actually set by the debug engine will
   * be indicated via the event listener mechanism.
   *  @exception java.io.IOException If there is a problem communicating
   *  with the debug engine.
   */
  public boolean setBreakpoint(boolean isEnabled,
                               int stopEvery, int from, int to,
                               String expr,
                               int threadID,
                               int sendReceiveControlFlags)
  throws java.io.IOException
  {
    return setBreakpoint(isEnabled,
                         stopEvery, from, to,
                         expr,
                         threadID,
                         sendReceiveControlFlags,
                         null);
  }

  public boolean setBreakpoint(boolean isEnabled,
                               int stopEvery, int from, int to,
                               String expr,
                               int threadID,
                               int sendReceiveControlFlags,
                               Object property)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(2, "Function[" + getName() + "].setBreakpoint()");

    DebugEngine debugEngine = _owningFile.view().part().module().process().debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_BreakpointLocation,
                                           sendReceiveControlFlags))
       return false;

    if (Model.checkFCTBit)
    {
        if (!debugEngine.getCapabilities().getBreakpointCapabilities().functionBreakpointsSupported())
        {
            debugEngine.cancelEPDCRequest(EPDC.Remote_BreakpointLocation);
            return false;
        }
    }

    short attribute = (isEnabled) ? EPDC.BkpEnable : (short)0;

    EEveryClause clause = null;

    // If no values have been specified for the every clause create one
    // with a set of defaults.
    if (!(stopEvery == 0 && from == 0 && to == 0))
        clause = new EEveryClause(stopEvery, to, from);

    EStdExpression2 conditionalExpr = null;

    // Is there a conditional expression?
    if (expr != null)
    {
        EStdView epdcContext = getLocation().getEStdView();
        conditionalExpr = new EStdExpression2(epdcContext, expr, threadID, 0);
    }

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request Remote_BreakpointLocation");

    // All strings(except the conditional expression) are left as null
    // because only the entry id is needed.
    if (!debugEngine.processEPDCRequest(new EReqBreakpointEntry
                                            (attribute,
                                              clause,
                                              null,           // entry name
                                              null,           // module name
                                              null,           // part name
                                              null,           // file name
                                              conditionalExpr,
                                              threadID,
                                              getId()),
                                        sendReceiveControlFlags,
                                        property))
       return false;
    else
       return true;
  }

  /**
   * Set an entry breakpoint at this function
   * This is the default method for setting a function breakpoint. The
   * function breakpoint will be set as 'enable', with the stopEvery, from,
   * and to parameters set to 0, as well as conditional expression parameter
   * set to null.
   * @param sendReceiveControlFlags the flag indicating the state in which the
   * request is to be sent(synchronized, asynchronized).
   * @return 'true' if the set breakpoint request was successfully sent to
   * the debug engine, 'false' otherwise. Note that a return value of 'true'
   * does not imply that the debug engine was able to set the breakpoint but
   * rather simply that the request was successfully sent to the debug engine.
   * Whether or not the breakpoint was actually set by the debug engine will
   * be indicated via the event listener mechanism.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   * @see setBreakpoint
   */
  public boolean setBreakpoint(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(2, "Function[" + getId() + "].setBreakpoint()");

    return setBreakpoint(true, 0, 0, 0, null, 0, sendReceiveControlFlags);
  }

   //Data fields
   private ViewFile _owningFile;
   private ERepEntryGetNext _epdcEntry;
   private boolean _fullyResolved = false;
}


