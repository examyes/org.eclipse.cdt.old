package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/MonitoredExpressionTreeNode.java, java-model, eclipse-dev, 20011128
// Version 1.11.1.2 (last modified 11/28/01 16:11:46)
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
import java.util.Vector;

public abstract class MonitoredExpressionTreeNode
{
   MonitoredExpressionTreeNode(EStdTreeNode node,
                               MonitoredExpression expr)
   {
      _name = (node.getTreeNodeData()).getName();
      _type = (node.getTreeNodeData()).getType();
      _childNum = node.getNumChild();
      _nodeID = node.getID();
      _monitoredExpr = expr;
   }

   /**
    * Send a request to change the value of a monitored expression. The user
    * can modify the value of a member of structure using this method.
    * However, the user can only modify the value of an expression that is
    * being currently monitored.
    * @param value The string representing the value the user has typed in,
    * sendReceiveControlFlags, the flag indicating the state in which the
    * request is to be sent(synchronized, unsynchronized).
    * @return 'true' if the request to modify the value of a monitored
    * expression was sent successfully, and 'false' otherwise.
    * @exception java.io.IOException if there is a communication problem
    * with the debug engine, this exception occurs.
    */

   public boolean modifyValue(String value, int sendReceiveControlFlags)
   throws java.io.IOException
   {
     DebugEngine debugEngine = _monitoredExpr.getOwningProcess().debugEngine();

     if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_ExpressionValueModify,
                                            sendReceiveControlFlags))
         return false;

     short id = _monitoredExpr.getMonitoredExpressionAssignedID();

     EReqExpressionValueModify request = new EReqExpressionValueModify(id,
                                                                       _nodeID,
                                                                       value);

     if (!debugEngine.processEPDCRequest(request, sendReceiveControlFlags))
         return false;
     else
         return true;
   }

   /**
    * Send a request to change the representation of a monitored value.
    * @param representation the object whose representation is to change
    * @param sendReceiveControlFlags this flag indicates the state in
    * which the request is to be sent.
    * @return 'true' if the request to change the representation format of
    * a given value is sent successfully, and 'false' otherwise.
    * @exception java.io.IOException if there is a communication problem
    * with the debug engine, this exception occurs.
    */
   public boolean changeRepresentation(Representation rep,
                                       int sendReceiveControlFlags)
   throws java.io.IOException
   {
     DebugEngine debugEngine = _monitoredExpr.getOwningProcess().debugEngine();

     if (!debugEngine.prepareForEPDCRequest(EPDC.Remote_ExpressionRepTypeSet,
                                            sendReceiveControlFlags))
         return false;

     int repIndex = -1;

     if (rep == null ||
         _representations == null ||
         (repIndex = _representations.indexOf(rep)) == -1)
     {
         debugEngine.cancelEPDCRequest(EPDC.Remote_ExpressionRepTypeSet);
         return false;
     }

     short id = _monitoredExpr.getMonitoredExpressionAssignedID();

     // The representation index within the engine starts from index 1,
     // and that is why one has to be added to repIndex
     EReqExpressionRepTypeSet request = new EReqExpressionRepTypeSet(
                                                           id,
                                                           _nodeID,
                                                           (short)(repIndex+1));

     if (!debugEngine.processEPDCRequest(request, sendReceiveControlFlags))
         return false;
     else
         return true;
   }

   /**
    * Get the name of the expression tree node
    */
   public String getName()
   {
     return _name;
   }

   /**
    * Get the type of the expression tree node
    */
   public String getType()
   {
     return _type;
   }

   /**
    * Return the id of the expression tree node
    */
   protected int nodeID()
   {
     return _nodeID;
   }

   protected int getChildNumber()
   {
      return _childNum;
   }

   /**
    * Get the current representation for this expression
    */
   public Representation getCurrentRepresentation()
   {
     return _defaultRep;
   }

   /**
    * Set the array of representations for this expression and set the
    * default representation for this expression
    */
   protected void setDefRepAndRepsArray(short[] reps, short defIndex)
   {
     DebugEngine engine = _monitoredExpr.getOwningProcess().debugEngine();

     // In a deferred mon expr, reps[0] is -1 :

     if (reps == null || reps.length == 0 || reps[0] < 1)
        return;

     _representations = new Vector(reps.length);

     for (int i = 0; i < reps.length; i++)
         _representations.addElement(engine.getRepresentation(reps[i]-1));

     // Since default representation index starts from one and reps
     // indices array start from 0 subtract one to get to the correct
     // representation.

     _defaultRep = (Representation)_representations.elementAt(defIndex-1);
   }


   /**
    * Return the arrary of all possible representation types
    * for this expression
    */
   public Representation[] getArrayOfRepresentations()
   {
     if (_representations == null)
        return null;

     Representation[] reps = new Representation[_representations.size()];
     _representations.copyInto(reps);
     return reps;
   }

   public void print(PrintWriter printWriter)
   {
     if (Model.includePrintMethods)
     {
       if (this instanceof ScalarMonitoredExpressionTreeNode)
       {
           printWriter.print("name: " + ((ScalarMonitoredExpressionTreeNode)this).getName());
           printWriter.print("type: " + ((ScalarMonitoredExpressionTreeNode)this).getType());
           printWriter.print(" value: " + ((ScalarMonitoredExpressionTreeNode)this).getValue());
       }
       else
       if (this instanceof StructMonitoredExpressionTreeNode)
       {
           printWriter.print("name: " + ((StructMonitoredExpressionTreeNode)this).getName());
           printWriter.print("type: " + ((StructMonitoredExpressionTreeNode)this).getType());
       }
       else
       if (this instanceof ClassMonitoredExpressionTreeNode)
       {
           printWriter.print("name: " + ((ClassMonitoredExpressionTreeNode)this).getName());
           printWriter.print("type: " + ((ClassMonitoredExpressionTreeNode)this).getType());
       }
       else
       if (this instanceof ArrayMonitoredExpressionTreeNode)
       {
           printWriter.print("name: " + ((ArrayMonitoredExpressionTreeNode)this).getName());
           printWriter.print("type: " + ((ArrayMonitoredExpressionTreeNode)this).getType());
       }
       if (this instanceof PointerMonitoredExpressionTreeNode)
       {
           printWriter.print("name: " + ((PointerMonitoredExpressionTreeNode)this).getName());
           printWriter.print("type: " + ((PointerMonitoredExpressionTreeNode)this).getType());
           printWriter.print(" value: " + ((PointerMonitoredExpressionTreeNode)this).getValue());
       }
     }
   }

   private String _name;
   private String _type;
   private int _childNum;
   private int _nodeID;
   private MonitoredExpression _monitoredExpr;
   private Vector _representations;
   private Representation _defaultRep;
}
