//////////////////////////////////////////////////////////////////////////////////////////////////////////
//Copyright (C)  1995, 2001 International Business Machines Corporation and others. All Rights Reserved.
//////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;

/**
 * This class represents a variable that is being monitored.  It stores the information required
 * to form an EStdTreeNode class.
 */
public abstract class GdbVariable  extends Variable
{
   public GdbVariable(DebugSession debugSession, String name, String type, int nodeID)
   {
      super(debugSession, name, type, nodeID);
      _debugSession = debugSession;
      _name        = name;
      _type        = type;
      _nodeID      = nodeID;
      _inScope     = true;
      _changed     = true;
   }

   /**
    * Create a variable object given the variable name, node ID
    */
   public static GdbVariable createVariable(DebugSession debugSession, String varName, String type, String value, int nodeID) 
   {
         return new GdbScalarVariable(debugSession, varName, type, value, nodeID);
   }

   /**
    * Get this variable's name.
    */
   public String getName()
   {
      return _name;
   }

   /**
    * Get this variable's name.
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
   public void setScope(boolean inScope)
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
    * Get whether this variable will return an expanded EPDC tree
    */
/*
   boolean isExpanded()
   {
      return _expand;
   }
*/

   /**
    * Set whether the tree structure has changed for this variable
    */
   void setTreeStructChanged(boolean treeStructChanged)
   {
      _treeStructChanged = treeStructChanged;
   }

   /**
    * Return whether the tree structure has changed for this variable since the last time isTreeStructChanged()
    * was called
    */
   public boolean isTreeStructChanged()
   {
      boolean treeStructChanged = _treeStructChanged;
      _treeStructChanged = false;
      return treeStructChanged;
   }

   /**
    * Return the number of nodes required by this variable to construct its tree
    */
   public abstract int numNodes();

   /**
    * Get the EStdTreeNode for this variable
    */
   public abstract EStdTreeNode getTreeNode();

   public abstract String getScalarValue();
   public abstract void setScalarValue(String s);

}
