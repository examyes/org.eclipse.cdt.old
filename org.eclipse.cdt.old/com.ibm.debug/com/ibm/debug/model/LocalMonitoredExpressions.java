package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/LocalMonitoredExpressions.java, java-model, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:12:40)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;
import java.util.Vector;

/**
 * Class representing monitored local expressions in the model
 */
public class LocalMonitoredExpressions extends DebugModelObject
{
  LocalMonitoredExpressions(DebuggeeThread thread)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Creating LocalMonitoredExpressions");

    _thread = thread;
  }

  /**
    * Send a request to remove local expressions monitor
    * This method will always be handled synchronously because before any
    * other request comes in we need to know the thread the local monitor
    * is associated with to handle freeing of the monitor.
    * @return 'true' if the request to delete the local expressions monitor
    * was sent successfully, and 'false' otherwise.
    * @exception java.io.IOException if there is a communication problem
    * with the debug engine, this exception occurs.
    */
  public boolean remove()
  throws java.io.IOException
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "In LocalMonitoredExpressions.remove()");

    DebugEngine debugEngine = _thread.owningProcess().debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_LocalVariableFree,
                                          debugEngine.sendReceiveSynchronously))
        return false;

    int threadID = _thread.debugEngineAssignedID();

    EReqLocalVariableFree request = new EReqLocalVariableFree(threadID, 0);

    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(2, "Sending EPDC request Remote_LocalVariableFree");

    if (!debugEngine.processEPDCRequest(request,
                                        debugEngine.sendReceiveSynchronously))
        return false;
    else
        return true;
  }

  /**
   * Add a monitored expression to the vector of monitored expressions in
   * the local monitor. The index in the vector is the id the backend has
   * assigned to the monitored expression.
   */
  void addLocalMonitoredExpression(MonitoredExpression expr)
  {
    int index = expr.getMonitoredExpressionAssignedID();

    setVectorElementToObject(expr, _localMonitoredExpressions, index);

    DebugEngine debugEngine = expr.getOwningProcess().debugEngine();

    EPDC_Request mostRecentRequest = debugEngine.getMostRecentRequest();

    int requestCode = mostRecentRequest.requestCode();

    // If this expr is being added due to a Remote_LocalVariable request
    // don't fire the "added" event:

    if (requestCode == EPDC.Remote_LocalVariable &&
        ((EReqLocalVariable)mostRecentRequest).getDU() == _thread.debugEngineAssignedID())
       return;

    debugEngine.getEventManager().addEvent(new MonitoredExpressionAddedEvent(
                                               this, expr, requestCode),
                                           _eventListeners);
  }

  /**
   * Remove the monitored expression from the local monitor
   */

  synchronized void removeExpression(int id)
  {
    if (Model.TRACE.DBG && Model.traceInfo())
        Model.TRACE.dbg(4, "LocalMonitoredExpression" + ".removeExpression()");

    DebugEngine debugEngine = _thread.owningProcess().debugEngine();
    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    MonitoredExpression expr = getLocalMonitoredExpression(id);

    expr.prepareToDie();
    expr.setHasBeenDeleted();

    try
    {
      _localMonitoredExpressions.setElementAt(null, id);
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
    }
  }

 /**
  * Notify listeners that local expressions monitor has ended.
  */

  void prepareToDie()
  {
    if (Model.TRACE.DBG && Model.traceInfo())
      Model.TRACE.dbg(3, "In LocalMonitoredExpressions.prepareToDie()");

    DebugEngine debugEngine = _thread.owningProcess().debugEngine();

    int requestCode = debugEngine.getMostRecentReply().getReplyCode();

    debugEngine.getEventManager().addEvent(new
                                           LocalMonitoredExpressionsEndedEvent(
                                                this, this, requestCode),
                                           _eventListeners);
  }

  /**
   * Return the entire list of local monitored expressions
   */

  public MonitoredExpression[] getLocalMonitoredExpressionsArray()
  {
    if (_localMonitoredExpressions == null)
        return null;

    int size = _localMonitoredExpressions.size();
    if (size == 0)
        return null;

    MonitoredExpression[] exprs = new MonitoredExpression[size];

    _localMonitoredExpressions.copyInto(exprs);

    return exprs;
  }

  /**
   * Return the monitored expression in the local monitor using its id
   */
  MonitoredExpression getLocalMonitoredExpression(int id)
  {
    try
    {
       return (MonitoredExpression)(_localMonitoredExpressions.elementAt(id));
    }
    catch (ArrayIndexOutOfBoundsException excp)
    {
      return null;
    }
  }

  /**
   * Event listener for adding a local monitor
   */
  public void addEventListener(LocalMonitoredExpressionsEventListener eventListener)
  {
    _eventListeners.addElement(eventListener);
  }

  /**
   * Event listener for removing a local monitor
   */
  public void removeEventListener(LocalMonitoredExpressionsEventListener eventListener)
  {
    int index = _eventListeners.indexOf(eventListener);

    if (index != -1)
    {
        try
        {
          _eventListeners.setElementAt(null, index);
        }
        catch(ArrayIndexOutOfBoundsException excp)
        {
        }
    }
  }

  /**
   * Return the thread associated with the local monitor
   */
  public DebuggeeThread getThread()
  {
    return _thread;
  }

  private DebuggeeThread _thread;
  private Vector _eventListeners = new Vector();
  private Vector _localMonitoredExpressions= new Vector();

}
