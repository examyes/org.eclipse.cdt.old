/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// com/ibm/debug/gdb/objects/GdbScalarVariable.java, gdb, java-dev
// Version 1.1 (last modified 5/24/01 13:45:28)
/////////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl.objects;
import  com.ibm.debug.gdbPicl.*;

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
      int typ = Gdb.TYPEINDEX_INTEGER;
           if(type.equals("float"))   typ = Gdb.TYPEINDEX_FLOAT;
      else if(type.equals("double"))  typ = Gdb.TYPEINDEX_DOUBLE;
      else if(type.equals("char"))    typ = Gdb.TYPEINDEX_CHARACTER;
      else if(type.equals("int"))     typ = Gdb.TYPEINDEX_INTEGER;
      else if(type.equals("short"))   typ = Gdb.TYPEINDEX_INTEGER;
      else if(type.equals("long"))    typ = Gdb.TYPEINDEX_INTEGER;
      else if(type.startsWith("float *")) typ = Gdb.TYPEINDEX_INTEGER;
      else if(type.startsWith("double *"))typ = Gdb.TYPEINDEX_INTEGER;
      else if(type.startsWith("char *"))  typ = Gdb.TYPEINDEX_INTEGER;
      else if(type.startsWith("int "))   typ = Gdb.TYPEINDEX_INTEGER;
      else if(type.startsWith("long "))  typ = Gdb.TYPEINDEX_INTEGER;
      else if(type.startsWith("short ")) typ = Gdb.TYPEINDEX_INTEGER;
      else
      {
         if (Gdb.traceLogger.ERR) 
             Gdb.traceLogger.err(2,"GdbScalarVariable ctor unknown type="+type+" defaultRepresentation=int");
      }
      _rep     = _debugEngine.getSession()._repInfo.defaultRepresentation(typ);
      //_intVar  = intVar;
      _value   = value;
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
      return new EStdTreeNode(_nodeID, getScalarItem());
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
      _value = s;
      _changed = true;
   }

   /**
    * Update the variable with a new variable object.
    * @return true if variable is correctly updated, false if the
    * Value is of the wrong type
    */
   public boolean setVariable(Object value)
   {
System.out.println("??????????????? GdbScalarVariable.setVariable value="+value.toString() );
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

//      short[] scalarReps = 
//         _debugEngine.getSession()._repInfo.repsForType(Gdb.TYPEINDEX_INTEGER);
      short[] scalarReps = new short[0];
      if(_debugEngine==null)
      {
System.out.println("### GdbScalarVariable.getScalarItem _debugEngine==NULL" );
          return null;
      }
      EPDC_EngineSession epdcSession = _debugEngine.getSession();
      if(epdcSession==null)
      {
System.out.println("### GdbScalarVariable.getScalarItem epdcSession==NULL" );
      }else
      {
         ERepTypesNumGet typesNum = epdcSession._repInfo;
         if(typesNum==null)
         {
System.out.println("### GdbScalarVariable.getScalarItem typesNum==NULL" );
         }else
         {
            scalarReps = typesNum.repsForType(Gdb.TYPEINDEX_INTEGER);
         }
      }
      String scalarValue = _value; //null;
/*
      // if not in scope return an EStdScalarItem object saying so
      if (!inScope())
         return new EStdScalarItem(_name, "int",
            _debugEngine.getResourceString("NOT_ALLOCATED_MSG"),
               scalarReps, (short)(_rep+1));
      if (scalarReps[_rep] == Gdb.REPINDEX_DECIMAL)
         scalarValue = Integer.toString(_value);
      else if (scalarReps[_rep] == Gdb.REPINDEX_HEXADECIMAL)
         scalarValue = getHexString();
      else
         scalarValue = _debugEngine.getResourceString("REPNAME_UNKNOWN_TEXT");
*/

      return new EStdScalarItem(_name, _type+PAD, scalarValue, scalarReps, (short)(_rep+1));
   }
   // data fields
   protected int      _rep;       // the current representation
   protected String   _value;
   protected String   PAD = "  ";
   // data members
}
