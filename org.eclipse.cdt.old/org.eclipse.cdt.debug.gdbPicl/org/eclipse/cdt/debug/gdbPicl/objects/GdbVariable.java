/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

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
      _fullName    = "";
   }

   /**
    * Create a variable object given the variable name, node ID
    */
   public static GdbVariable createVariable(DebugSession debugSession, String varName, String type, String value, int nodeID) 
   {
   	
   		if (!_allowTreeStructure)
   		{
	         return new GdbScalarVariable(debugSession, varName, type, value, nodeID);
   		}
   		
   		if (value.startsWith("{"))
   		{	 
   			int comma = value.indexOf(",");
   			String testStr;
   			if (comma != -1)
   			{
   				testStr = value.substring(0, comma);
   			}
   			else
   			{
   				return new GdbScalarVariable(debugSession, varName, type, value, nodeID);
   			}
   			
   			int equal = testStr.indexOf("=");
   			if (equal != -1)
   			{
   				
   				int lastBrace = testStr.lastIndexOf("{");
   				
   				if (testStr.charAt(lastBrace+1) == '\"' && equal>lastBrace)
 				{
 					// array
 					// even it has an equal sign.... it starts with quote, so it's a string
 					return new GdbArrayVariable(debugSession, varName, type, value, varName, nodeID);
 				}
   				
   				if (!testStr.startsWith("{{"))
 				{
	   				// object
					return new GdbObjectVariable(debugSession, varName, type, value, varName, nodeID);
 				}
 				else
 				{
 					// there is an equal sign
 					// starts with {{
 					// could be an array of object or an object that starts with an union
 					// if this is an array of object, then we will see the same field name again
 					// try to extract the field name
 					
 					int brace = testStr.lastIndexOf("{");
 					String fieldName = testStr.substring(brace+1, equal);
 					fieldName = fieldName.trim();
 					
 					if (value.indexOf(fieldName, comma) != -1)
 					{
 						// this is an array
 						return new GdbArrayVariable(debugSession, varName, type, value, varName, nodeID);
 					}				
 					else
 					{
 						// this is an object that starts with an union
 						return new GdbObjectVariable(debugSession, varName, type, value, varName, nodeID);
 					}
 				}					
   			}
   			else
   			{
   				// it's really an array
				return new GdbArrayVariable(debugSession, varName, type, value, varName, nodeID);
   			}	         
   		}
   		else
   		{
	         return new GdbScalarVariable(debugSession, varName, type, value, nodeID);
   		}
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
	 * Gets the fullName
	 * @return Returns a String
	 */
	public String getFullName() {
		
		if (_fullName == "")
			return _name;
		
		return _fullName;
	}
	/**
	 * Sets the fullName
	 * @param fullName The fullName to set
	 */
	public void setFullName(String fullName) {
		_fullName = fullName;
	}

   /**
    * Get the EStdTreeNode for this variable
    */
   public abstract EStdTreeNode getTreeNode();

   public abstract String getScalarValue();
   public abstract void setScalarValue(String s);
   public abstract GdbVariable getNode(int nodeID);

   private static boolean _allowTreeStructure=true;
   protected String _fullName;


}
