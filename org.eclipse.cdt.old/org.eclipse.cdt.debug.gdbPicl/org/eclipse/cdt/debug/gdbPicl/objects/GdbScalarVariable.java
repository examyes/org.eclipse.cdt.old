/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl.objects;
import  org.eclipse.cdt.debug.gdbPicl.*;

import com.ibm.debug.epdc.*;

/**
 * This stores information for a monitored integer
 */
class GdbScalarVariable extends GdbVariable
{	
   /**
    * Create a new monitored scalar variable.
    * @param name the variable name
    * @param intVar the remote integer object
    * @param nodeID the tree node ID
    */
   GdbScalarVariable(DebugSession debugSession, String name, String type, String value, int nodeID)
   {   	
     super(debugSession, name, type, nodeID);
      
      int typ = Gdb.TYPEINDEX_DEFAULT;
      
      _rep     = _debugEngine.getSession()._repInfo.defaultRepresentation(typ);
      //_intVar  = intVar;
      _value   = value;
      
	  _name = _name.replace((char) 26, ',');
	  _value = _value.replace((char) 26, ',');      
   }


   /**
    * Convert this integer to a hexidecimal string
    */
   private String getHexString()
   {
      String zeros = "0x00000000";
      int intValue = Integer.parseInt(_value);
      String hex = Integer.toHexString(intValue).toUpperCase();

      // return zero padded hex string
      return zeros.substring(0, zeros.length() - hex.length()) + hex;
   }

   /**
    * Update the variable with a new remote int object.
    * @return true if variable is correctly updated, false if the RemoteValue is of the wrong type
    */
/*
   boolean setRemoteVar(RemoteValue remoteVar)
   {
      // Make sure _changed is set correctly when this method exits with true.
      if (remoteVar instanceof RemoteInt)
      {
         _intVar = (RemoteInt) remoteVar;
         int prevValue = _value;
         _value = _intVar.get();
         _changed =  (prevValue != _value);
         return true;
      }
      return false;
   }
*/

   /**
    * Returns the EStdTreeNode item for this data type.
    */
   public EStdTreeNode getTreeNode()
   {
   	  if (this._type.indexOf("*") == -1)
   	  {
      	return new EStdTreeNode(_nodeID, getScalarItem());
   	  }
   	  else
   	  {
	    return new EStdTreeNode(_nodeID, getPointerItem());
   	  }
   }

   /**
    * Get the current representation
    */
   int getRepresentation()
   {
      return _rep+1;
   }

   /**
    * Set the representation
    */
   public void setRepresentation(int nodeID, int rep)
   {
      _rep = rep-1;
   }

   /**
    * Expand a subtree of this variable
    */
   public void expandSubTree(int rootNodeID, int startChild, int endChild) {}


   /**
    * Collapse a subtree of this variable
    */
   public void collapseSubTree(int rootNodeID, int startChild, int endChild) {}

   /**
    * Set the node ID
    */
   void setNodeID(int newNodeID)
   {
      super.setNodeID(newNodeID);
      _nodeID = newNodeID;
   }

   /**
    * Return the number of nodes required by this variable to construct its tree
    */
   public int numNodes()
   {
      return 1;
   }
  
   /**
    * Get the current variable value
    */
   public String getScalarValue()
   {
      return _value;
   }

   /**
    * Get the current variable value
    */
   public void setScalarValue(String s)
   {
   		if (!s.equals(_value))
   		{
	      _value = s;
	      _changed = true;
   		}
   }
   
   /**
    * Update the variable with a new variable object.
    * @return true if variable is correctly updated, false if the
    * Value is of the wrong type
    */
   public boolean setVariable(Object value)
   {
	   if (Gdb.traceLogger.EVT) 
			Gdb.traceLogger.evt(3,"??????????????? GdbScalarVariable.setVariable value="+value.toString() );
       _value = value.toString();
       return true;
   }

   /* Modify the value of the addressed subnode.*/
   public void setValue(int nodeID, String expr)
   {   setScalarValue(expr);    }

   /**
    * Get the EStdScalarItem for this data type
    */
   EStdScalarItem getScalarItem()
   {

      short[] scalarReps = 
         _debugEngine.getSession()._repInfo.repsForType(Gdb.TYPEINDEX_DEFAULT);

      String scalarValue = _value; //null;
      
      // retrieve value with specified representation
      if (_rep+1 != Gdb.REPINDEX_DEFAULT)
      {
      	String name = _fullName;
      	
      	if (name.equals(""))
      		name = _name;
		String cmd = "print" + _repArgument[_rep] + name;
		boolean ok = ((GdbDebugSession)_debugSession).executeGdbCommand(cmd);

		if (ok)
		{
			String[] lines = ((GdbDebugSession)_debugSession).getTextResponseLines();
			
			if (lines.length > 1)
			{
				scalarValue = lines[1];
			}
		}
      }
      

      return new EStdScalarItem(_name, _type+PAD, scalarValue, scalarReps, (short)(_rep+1));
   }
   
   
      /**
    * Get the EStdScalarItem for this data type
    */
   EStdPointerItem getPointerItem()
   {

      short[] scalarReps = 
         _debugEngine.getSession()._repInfo.repsForType(Gdb.TYPEINDEX_DEFAULT);

      String scalarValue = _value; //null;
      
      // retrieve value with specified representation
      if (_rep+1 != Gdb.REPINDEX_DEFAULT)
      {
      	String name = _fullName;
      	
      	if (name.equals(""))
      		name = _name;
		String cmd = "print" + _repArgument[_rep] + name;
		boolean ok = ((GdbDebugSession)_debugSession).executeGdbCommand(cmd);

		if (ok)
		{
			String[] lines = ((GdbDebugSession)_debugSession).getTextResponseLines();
			
			if (lines.length > 1)
			{
				scalarValue = lines[1];
			}
		}
      }
      

      return new EStdPointerItem((short)1, _name, _type+PAD, _type+PAD, scalarValue, (short)(_rep+1),scalarReps);
   }
   
   public GdbVariable getNode(int nodeID)
   {
   		if (nodeID == _nodeID)
	   		return (GdbVariable)this; 		
		else
			return null;	   		
   }   
   
   // data fields
   protected int      _rep;       // the current representation
   protected String   _value;
   protected String   PAD = "";
   // data members
 
 	// gdb printing argument:  default, decimal, hex, octal, binary  
   private static final String _repArgument[] = {" ", " /d ", " /x ", " /o ", " /t "};
   
}
