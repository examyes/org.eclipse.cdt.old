/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;
//import org.eclipse.cdt.debug.gdbPicl.expr.ExprValue;

/**
 * This class represents a variable that is being monitored.  It stores the information required
 * to form an EStdTreeNode class.
 */
public abstract class Variable
{
   public Variable(DebugSession debugSession, String name, String type, int nodeID)
   {
      _debugSession = debugSession;
      _debugEngine = _debugSession.getDebugEngine();
      _name        = name;
      _type        = type;
      _nodeID      = nodeID;
      _inScope     = true;
      _changed     = true;
   }

   /**
    * Get this variable's name.
    */
   public String getName()
   {
      return _name;
   }

   /**
    * Get this variable's type.
    */
   public String getType()
   {
      return _type;
   }


   /**
    * Get this variable's node id.
    */
   public int getNodeID()
   {
      return _nodeID;
   }

   /**
    * Set the node id.
    */
   void setNodeID(int newNodeID)
   {
      _nodeID = newNodeID;
   }

   /**
    * Set whether this variable is currently avaialable to be monitored.
    */
   void setScope(boolean inScope)
   {
      if (_inScope != inScope)
      {
         _treeStructChanged = true;
      }
      _inScope = inScope;
   }

   /**
    * Get whether this variable is currently in scope
    */
   boolean inScope()
   {
      return _inScope;
   }

   public boolean hasChanged()
   {
      return _changed;
   }

   /**
    * Set whether the tree structure has changed for this variable
    */
   void setTreeStructChanged(boolean treeStructChanged)
   {
      _treeStructChanged = treeStructChanged;
   }

   /**
    * Return whether the tree structure has changed for this variable 
    * since the last time isTreeStructChanged() was called
    */
   public boolean isTreeStructChanged()
   {
      boolean treeStructChanged = _treeStructChanged;
      _treeStructChanged = false;
      return treeStructChanged;
   }

   /**
    * Set the representation for the given node.
    */
   public abstract void setRepresentation(int nodeID, int rep);

   /**
    * Expand a subtree of this variable
    */
   public abstract void expandSubTree(int rootNodeID, int startChild, int endChild);

   /**
    * Collapse a subtree of this variable
    */
   public abstract void collapseSubTree(int rootNodeID, int startChild, int endChild);

   /**
    * Return the number of nodes required by this variable to construct its tree
    */
   public abstract int numNodes();

   /**
    * Get the EStdTreeNode for this variable
    */
   public abstract EStdTreeNode getTreeNode();

   /**
    * Update the variable with a new variable object.
    * @return true if variable is correctly updated, false if the
    * Value is of the wrong type
    */
   public abstract boolean setVariable(Object value);

   /*
    * Modify the value of the addressed subnode.
    */
   public abstract void setValue(int nodeID, String expr) throws Exception;

   // data fields
   protected DebugSession _debugSession;
   protected DebugEngine  _debugEngine;
   protected boolean      _inScope;
   protected String       _name;
   protected String       _type;
   protected int          _nodeID;
   protected boolean      _changed;

   protected boolean      _treeStructChanged;
}
