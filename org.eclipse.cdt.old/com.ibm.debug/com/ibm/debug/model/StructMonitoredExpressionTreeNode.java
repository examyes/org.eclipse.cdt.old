package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/StructMonitoredExpressionTreeNode.java, java-model, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:12:07)
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
 * This class represents the monitored expressions of C structure type
 */
public class StructMonitoredExpressionTreeNode extends AggregateMonitoredExpressionTreeNode
{
   StructMonitoredExpressionTreeNode(EStdTreeNode node,
                                     MonitoredExpression expr)
   {
      super(node, expr);
      int count = ((EStdStructItem)(node.getTreeNodeData())).getItemCount();
      super.setNumberOfChildren(count);
   }

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";
}
