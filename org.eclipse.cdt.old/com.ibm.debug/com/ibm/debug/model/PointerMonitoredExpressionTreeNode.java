package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/PointerMonitoredExpressionTreeNode.java, java-model, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:12:33)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;

/**
 * This class represents the monitored expressions of pointer type
 */
public class PointerMonitoredExpressionTreeNode extends
                                                MonitoredExpressionTreeNode
{

   PointerMonitoredExpressionTreeNode(EStdTreeNode node,
                                      MonitoredExpression expr)
   {
      super(node, expr);

      _value = ((EStdPointerItem)(node.getTreeNodeData())).getValue();
      _monitoredExpr = expr;

      // Set the default representation index of this node
      // Set the array of representation indices of this node
      super.setDefRepAndRepsArray(
                   ((EStdPointerItem)(node.getTreeNodeData())).getArrayOfReps(),
                   ((EStdPointerItem)(node.getTreeNodeData())).getDefRep() );
   }

  /**
   * Send a request to de-reference a pointer expression. This request
   * will be sent when the user attempst to see the structure the pointer
   * expression is pointing to. As a result of this request, a new monitored
   * expression will be created to represent the dereferenced pointer and the
   * original pointer expression will remain intact.
   * Only monitored expressions of pointer type can be de-referenced.
   * @param sendReceiveControlFlags, the flag indicating the state in which the
   * request is to be sent(synchronized, unsynchronized).
   * @return 'true' if the request to de-reference the monitored
   * expression was sent successfully, and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */
  public boolean dereference(int sendReceiveControlFlags)
  throws java.io.IOException
  {
    DebugEngine debugEngine = _monitoredExpr.getOwningProcess().debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_PointerDeref,
                                           sendReceiveControlFlags))
        return false;

    short id = _monitoredExpr.getMonitoredExpressionAssignedID();

    EReqPointerDeref request = new EReqPointerDeref(id, super.nodeID());

    if (!debugEngine.processEPDCRequest(request, sendReceiveControlFlags))
        return false;
    else
        return true;
  }

   /**
    * Return the string that represents the value of the pointer
    */
   public String getValue()
   {
      return _value;
   }

   private String _value;
   private MonitoredExpression _monitoredExpr;
}
