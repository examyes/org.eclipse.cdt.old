package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ScalarMonitoredExpressionTreeNode.java, java-model, eclipse-dev, 20011128
// Version 1.4.1.2 (last modified 11/28/01 16:11:46)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;

public class ScalarMonitoredExpressionTreeNode extends MonitoredExpressionTreeNode
{

   ScalarMonitoredExpressionTreeNode(EStdTreeNode node,
                                     MonitoredExpression expr)
   {
      super(node, expr);

      _value = ((EStdScalarItem)(node.getTreeNodeData())).getValue();

      // Set the default representation index of this node
      // Set the array of representation indices of this node
      super.setDefRepAndRepsArray(
                    ((EStdScalarItem)(node.getTreeNodeData())).getArrayOfReps(),
                    ((EStdScalarItem)(node.getTreeNodeData())).getDefRep() );
   }

   /**
    * Return the contents of the variable as a string
    */
   public String getValue()
   {
      return _value;
   }

   private String _value;
}
