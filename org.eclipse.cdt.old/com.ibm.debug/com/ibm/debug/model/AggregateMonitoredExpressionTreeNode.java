package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/AggregateMonitoredExpressionTreeNode.java, java-model, eclipse-dev, 20011128
// Version 1.10.1.2 (last modified 11/28/01 16:12:30)
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
 * This is an abstract super class of the following tree structures:
 * 1) StructMonitoredExpressionTreeNode
 * 2) ClassMonitoredExpressionTreeNode
 * 3) ArrayMonitoredExpressionTreeNode
 */
public abstract class AggregateMonitoredExpressionTreeNode extends
                                                    MonitoredExpressionTreeNode
{
  AggregateMonitoredExpressionTreeNode(EStdTreeNode node,
                                       MonitoredExpression expr)
  {
    super(node, expr);
    _owningMonitoredExpr = expr;

    // When the root node is created no children has been seen yet
    if (node.children() != null)
    {
        int numberOfChildren = node.children().size();
        _children = new MonitoredExpressionTreeNode[numberOfChildren];

        // create the array of children
        for (int i = 0; i < numberOfChildren; i++)
        {
             EStdTreeNode child = (EStdTreeNode)(node.children().elementAt(i));
             short type = child.getTreeNodeData().getGenericNodeType();

             switch (type)
             {
               case EPDC.StdScalarNode:
                    _children[i] = new ScalarMonitoredExpressionTreeNode(child,
                                                                         expr);
                    break;

               case EPDC.StdStructNode:
                    _children[i] = new StructMonitoredExpressionTreeNode(child,
                                                                         expr);
                    break;

               case EPDC.StdClassNode:
                    _children[i] = new ClassMonitoredExpressionTreeNode(child,
                                                                        expr);
                    break;
               case EPDC.StdArrayNode:
                    _children[i] = new ArrayMonitoredExpressionTreeNode(child,
                                                                        expr);
                    break;
               case EPDC.StdPointerNode:
                    _children[i] = new PointerMonitoredExpressionTreeNode(child,
                                                                          expr);
                    break;
             }
        }
    }
  }

  /**
   * Send a request to expand a node in a structure, a class, or an array item.
   * This will result in the children of that node to be monitored as well as
   * the node itself. Only one level of expansion can take place at a time.
   * A node in the structure (or a class or an array) that is not the root node
   * can be expanded only through the root node. This means that by having
   * the root node, the children of the root and any subsequent children of
   * the children can be identified and the expand request can be applied to
   * them.
   * This request will not be sent if the first and the last node specified
   * are outside the range of the nodes of the tree (less than one and
   * greater than the number of nodes in the tree).
   * @parm startChild starting node of a subtree to retrieve, must start
   * from 1.
   * @parm endChild the last node of the subtree to retrieve, must not be
   * greater than the number of nodes in the tree
   * @param sendReceiveControlFlags, the flag indicating the state in which the
   * request is to be sent(synchronized, asynchronized).
   * @return 'true' if the request to expand the member of a monitored
   * expression was sent successfully, and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */
  public boolean expand(int startChild, int endChild,
                        int sendReceiveControlFlags)
  throws java.io.IOException
  {
    DebugEngine debugEngine = _owningMonitoredExpr.getOwningProcess().debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_ExpressionSubTree,
                                           sendReceiveControlFlags))
        return false;

    // Extra check to make sure a request with an invalid startChild
    // or endChild is not sent.
    int size = getNumberOfChildren();
    if (startChild < 1 || endChild > size)
    {
        debugEngine.cancelEPDCRequest(EPDC.Remote_ExpressionSubTree);
        return false;
    }

    short id = _owningMonitoredExpr.getMonitoredExpressionAssignedID();

    EReqExpressionSubTree request = new EReqExpressionSubTree(
                                                       id,
                                                       super.nodeID(),
                                                       startChild,
                                                       endChild);

    if (!debugEngine.processEPDCRequest(request, sendReceiveControlFlags))
        return false;
    else
        return true;
  }

  /**
   * Send a request to collapse a node in a structure, a class, or an array
   * item. This will result in the children of the node to be hiddren from
   * the monitor view. Only one level of collapse can take place at a time.
   * A node in the structure (or a class or an array) that is not the root node
   * can be collapsed only through the root node(the monitored expression).
   * This means that by having the root node, the children of the root and
   * any subsequent children of the children can be identified and the collapse
   * request can be applied to them.
   * This request will not be sent if the first and the last node specified
   * are outside the range of the nodes of the tree (less than one and
   * greater than the number of nodes in the tree).
   * @parm startChild starting node of a subtree to remove, cannot be less
   * than 1.
   * @parm endChild the last node of the subtree to remove, cannot be
   * greater than the number of nodes in the tree
   * @param sendReceiveControlFlags, the flag indicating the state in which
   * the request is to be sent(synchronized, asynchronized).
   * @return 'true' if the request to collapse the children of a monitored
   * expression was sent successfully, and 'false' otherwise.
   * @exception java.io.IOException if there is a communication problem
   * with the debug engine, this exception occurs.
   */
  public boolean collapse(int startChild, int endChild,
                          int sendReceiveControlFlags)
  throws java.io.IOException
  {
    DebugEngine debugEngine = _owningMonitoredExpr.getOwningProcess().debugEngine();

    if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_ExpressionSubTreeDelete,
                                           sendReceiveControlFlags))
        return false;

    // Extra check to make sure a request with an invalid startChild
    // or endChild is not sent.
    int size = getNumberOfChildren();
    if (startChild < 1 || endChild > size)
    {
        debugEngine.cancelEPDCRequest(EPDC.Remote_ExpressionSubTreeDelete);
        return false;
    }

    short id = _owningMonitoredExpr.getMonitoredExpressionAssignedID();

    EReqExpressionSubTreeDelete request = new EReqExpressionSubTreeDelete(
                                                  id,
                                                  super.nodeID(),
                                                  startChild,
                                                  endChild);

    if (!debugEngine.processEPDCRequest(request, sendReceiveControlFlags))
        return false;
    else
        return true;
  }

  /**
   * The super class will keep track of the number of node members
   */
  protected void setNumberOfChildren(int count)
  {
    _numberOfChildren = count;
  }

  /**
   * Return the number of items in the structure/array/class
   */
  public int getNumberOfChildren()
  {
    return _numberOfChildren;
  }

  /**
   * Number of children returned
   */
  public int getNumberOfChildrenReturned()
  {
    if (_children != null)
      return _children.length;
    else
      return 0;
  }

  /**
   * Returns the ordinal number of the first child.
   * This can be used when collapsing a monitor where a subset of the children are displayed
   * If there are no children then returns 0
   */
  public int getStartChildNumber()
  {
     if (getNumberOfChildrenReturned() > 0)
        return _children[0].getChildNumber();
     else
        return 0;

  }

  /**
   * Returns the ordinal number of the last child.
   * This can be used when collapsing a monitor where a subset of the children are displayed
   * If there are no children then returns 0
   */
  public int getEndChildNumber()
  {
     if (getNumberOfChildrenReturned() > 0)
        return _children[getNumberOfChildrenReturned()-1].getChildNumber();
     else
        return 0;
  }

  /**
   * Return the array of children of the node
   */
  public MonitoredExpressionTreeNode[] getChildren()
  {
    if (_children == null)
        return null;

    if (_children.length == 0)
        return null;

    return _children;
  }

  private MonitoredExpression _owningMonitoredExpr;
  private int _numberOfChildren;
  private MonitoredExpressionTreeNode[] _children;

}
