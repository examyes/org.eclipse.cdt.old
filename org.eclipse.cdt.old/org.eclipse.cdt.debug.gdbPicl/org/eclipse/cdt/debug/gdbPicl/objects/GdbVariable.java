/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
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
    * Bug 10102
    * This function cleans up the string for template.
    * In case a variable is a template variable, the string representing
    * the variable may have the following format:  <<a,b>, <c,d>> = <<a,b>, <c,d>> values.
    * The commas within '<'  and '>' will screw up parsing of the variable inside
    * GdbObjectVariable when the tree is created.  In some extreme cases, gdbPicl will
    * not be able to distinguish a template variable and will parse it as an array.
    * 
    * To get around this problem, any comma between <> will be replaced with ^Z character.
    * ^Z will be replaced back to a comma after parsing is completed.  This function 
    * replaces the commas to ^Z.  In GdbObjectVariable and GdbScalarVariable, ^Z will 
    * be changed back to commas.  ^Z will not be changed to commas in GdbArrayVariable 
    * because an array variable is either an array of objects or an array of scalars.
    * Replacements of ^Z in GdbObjectVariable and GdbScalarVariable are sufficient 
    * to ensure that all ^Z set here will be changed back to commas.  In fact, replacing ^Z
    * in GdbArrayVariable will pose problem when gdbPicl tries to create an array 
    * of objects.
    * 
    * Need to change the algorithm to use string buffer?
    */
   private static String cleanupForTemplate(String parseStr)
	{
		String str = parseStr;
		
		if (parseStr.indexOf("<") == -1)
			return parseStr;
		
		int startLocation=-1;
		int endLocation=-1;

		int start=0;
		int end=0;
		
		boolean quote = false;
		
		String returnStr="";			
		StringBuffer buffer = new StringBuffer(parseStr.length());
		
		char [] strChar = str.toCharArray();
				
		for (int i=0; i<str.length(); i++)
		{			
			if (strChar[i] == '\"')
			{
				quote = !quote;
			}
			
			if (strChar[i] == '<' && !quote)
			{			
				start++;
				startLocation = i;
				buffer.append(strChar[i]);
			}
			
			if (strChar[i] == '>' && !quote)
			{
				end++;
				endLocation = i;
				buffer.append(strChar[i]);
			}
			
			if ((start==end && strChar[i] != '<') && (strChar[i] != '>'))
			{				
				buffer.append(strChar[i]);
			}
			else
			{
				if (strChar[i] == ',')
					buffer.append((char) 26);
				else if ((strChar[i] != '<') && (strChar[i] != '>'))
					buffer.append(strChar[i]);					
			}
		}				
		
		returnStr = buffer.toString();
		return returnStr;		
		
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
   		
   		value = cleanupForTemplate(value);
   		
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
 					if (brace+1 < equal)
 					{
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
 					else
 					{
 						testStr = testStr.substring(0, equal-1);
 						// use another method to find the field name
 						while (testStr.startsWith("{"))
 						{
 							testStr = testStr.substring(1);
 						}
 						String fieldName = testStr;
 						
 						 					
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
