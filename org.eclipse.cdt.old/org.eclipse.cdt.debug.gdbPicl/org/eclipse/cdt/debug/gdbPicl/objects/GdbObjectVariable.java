/*
 * Copyright (c) 1995, 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package com.ibm.debug.gdbPicl.objects;

import com.ibm.debug.epdc.*;
import com.ibm.debug.gdbPicl.DebugSession;
import com.ibm.debug.gdbPicl.GdbDebugSession;
import java.util.*;

public class GdbObjectVariable extends GdbVariable {

	/**
	 * Constructor for GdbObjectVariable
	 */
	public GdbObjectVariable(
		DebugSession debugSession,
		String name,
		String type,
		String value,
		String prefix,
		int nodeID) {
		super(debugSession, name, type, nodeID);
		
	    _rep = 0;
	    _className = type;
	    _expand = false;
	    _fields = new Vector();
       	_gdbData = value;
       	_prefix = prefix;
       	_numNodes = 1;
	    
	    value.trim();
    	    
		createTree(value);    	    

	}
	
	/**
	 * @see Variable#setValue(int, String)
	 */
	public void setValue(int nodeID, String expr) throws Exception {
	}

	/**
	 * @see Variable#setVariable(Object)
	 */
	public boolean setVariable(Object value) {
		return false;
	}

	/**
	 * @see GdbVariable#setScalarValue(String)
	 */
	public void setScalarValue(String s) {
		
		if (!s.equals(_gdbData))
		{
			_fields.removeAllElements();
		
			String parseStr = s;		
			
			createTree(parseStr);
			_changed = true;
		}
	}

	/**
	 * @see GdbVariable#getScalarValue()
	 */
	public String getScalarValue() {
		return _gdbData;
	}

	/**
	 * @see GdbVariable#getTreeNode()
	 * 
	 * Construct the tree for this object
	 * 
	 */
	public EStdTreeNode getTreeNode() {
		
	  EStdClassItem myclass = new EStdClassItem(EPDC.StdClassNode, (short)1, _fields.size(), getName(), getClassName());
	  EStdTreeNode rootNode = new EStdTreeNode(_nodeID, myclass);
	  
	  for (int i=0; i<_fields.size(); i++)
	  {
	  	GdbVariable field = (GdbVariable)_fields.elementAt(i);

		EStdTreeNode childNode = field.getTreeNode();
	  	
	  	rootNode.addChildNode(childNode, field.numNodes());
	  }
   	  
   	  return rootNode;

	}

	/**
	 * @see GdbVariable#numNodes()
	 */
	public int numNodes() {
/*		
		int numNodes = 0;
		
		for (int i=0; i<_fields.size(); i++)		
		{
			numNodes += ((GdbVariable)_fields.elementAt(i)).numNodes();
		}
				
		return numNodes;
*/
		return _numNodes;		
	}

	/**
	 * @see GdbVariable#collapseSubTree(int, int, int)
	 */
	public void collapseSubTree(int rootNodeID, int startChild, int endChild) {
	}

	/**
	 * @see GdbVariable#expandSubTree(int, int, int)
	 */
	public void expandSubTree(int rootNodeID, int startChild, int endChild) {
	}

	/**
	 * @see GdbVariable#setRepresentation(int, int)
	 */
	public void setRepresentation(int nodeID, int rep) {
	}
	
	/**
	 * Gets the className
	 * @return Returns a String
	 */
	public String getClassName() {
		return _className;
	}
	/**
	 * Sets the className
	 * @param className The className to set
	 */
	public void setClassName(String className) {
		_className = className;
	}
	
	private int findMatchingEnd(String parseStr)
	{
		String str = parseStr;
		int startLocation=-1;
		int endLocation=-1;

		int start=0;
		int end=0;
			
		
		for (int i=0; i<str.length(); i++)
		{
			if (str.charAt(i) == '{')
			{			
				start++;
				startLocation = i;						
			}
			
			if (str.charAt(i) == '}')
			{
				end++;
				endLocation = i;
			}
			
			if (start==end && start!=0 && end !=0)
				break;
		}				
		
		return endLocation;
	}
	
	/*
		Create Tree Structure for object data
		Possible Input:
		{a = 1, b = 2, c = 3}
		{<type_name> = <something>, a = 1, b = 2, c = 3}
		{<type_name> = {a = 1, b = 2, c = 3}, d = 4, e = 5}
		{a = {b = 1, c = 2, d =3 }, x = y, k = z}
		{x = y, k = z, a = {...}}
		{a = {1, 2, 3, 4}, b = 4, d = 5}
		{a = {{x = 1, y = 2, z = 3}, {...}}, b = 3}
		
	*/
	private void createTree(String parseStr)
	{
		String fieldName;
		String fieldType;
		String fieldValue;
		String prefix = _prefix;
		String fullFieldName;
		boolean lastone = false;
		
		// remove {} from beginning and end
		if (parseStr.startsWith("{"))
			parseStr = parseStr.substring(1);
			
		if (parseStr.endsWith("}"))
			parseStr = parseStr.substring(0, parseStr.length()-1);			
		
		
		while (parseStr != "" && parseStr != null)
		{	
			// delimite by comma to get the first token
			int comma = parseStr.indexOf(",");
			
			// could be the last element!!
			if (comma == -1)
			{
				// return scalar??	
				lastone = true;
			}
			
			// get field name (up to equal sign)
			// there must be an equal sign here... otherwise, it won't get here
			int equal = parseStr.indexOf("=");
			fieldName = parseStr.substring(0, equal);
			
			// get type name for this field		
			fieldName = fieldName.trim();	    		
	   		if (!fieldName.startsWith("<") && !fieldName.endsWith(">"))
	   		{
				fullFieldName = _prefix + "." + fieldName;
	    		prefix = fullFieldName;
				fieldType = GdbVariableMonitor.getExpressionType((GdbDebugSession)_debugSession, fullFieldName);		    								
	   		}
	   		else			    		
	   		{
	   			// if name is enclosed by <>, it's the actual type
	   			fieldType = fieldName.substring(1, fieldName.length()-1);
	   		}
			
			// get value (from first equal sign to comma)
			if (!lastone && equal+2 < comma)
				fieldValue = parseStr.substring(equal+2, comma);
			else
			{
				fieldValue = parseStr.substring(equal+2);				
			}							
			
			// in case this is a string and a comma is part of the data
			if (fieldValue.startsWith("\""))
			{
				comma = parseStr.indexOf("\",");
				if (comma != -1)
				{
					comma++;	// skip the quote
					fieldValue = parseStr.substring(equal+2, comma);
				}
			}
			
			// if the value starts with {
			if (fieldValue.startsWith("{"))
			{
				// it could be an array or object
				// if there is no equal siqn or starts with {{
				if (fieldValue.indexOf("=") == -1 || fieldValue.startsWith("{{"))
				{
					// this is an array
					int endClass = findMatchingEnd(parseStr);
					
					if (endClass != -1)
					{
			    		fieldValue = parseStr.substring(equal+2, endClass+1);
			    		GdbArrayVariable newField = new GdbArrayVariable(_debugSession, fieldName, fieldType, fieldValue, _nodeID + _numNodes);
			    		_fields.add(newField);
			    		_numNodes += newField.numNodes();
			    		
			    		if (endClass+3 > parseStr.length())
			    		{
			    			// done!
			    			break;
			    		}
			    				    		
			    		parseStr = parseStr.substring(endClass+3);
					} 
					else
					{
						// braces not matching
						// create scalar and break
						GdbScalarVariable newField;
						newField  = new GdbScalarVariable(_debugSession, fieldName, fieldType, fieldValue, _nodeID + _numNodes);						
						_fields.add(newField);
						_numNodes++;
						break;
					}
				}
				else
				{				
					// if there is an equal sign between the first equal sign and comma					
					// this is an object
					int endClass = findMatchingEnd(parseStr);
					if (endClass != -1)
					{
			    		fieldValue = parseStr.substring(equal+2, endClass+1);
			    		GdbObjectVariable newField = new GdbObjectVariable(_debugSession, fieldName, fieldType, fieldValue, prefix, _nodeID + _numNodes);
			    		_fields.add(newField);
			    		_numNodes += newField.numNodes();
			    		
			    		if (endClass+3 > parseStr.length())
			    		{
			    			// done!
			    			break;
			    		}
			    		
			    		parseStr = parseStr.substring(endClass+3);
					} 
					else
					{
						// braces not matching
						// create scalar and break
						GdbScalarVariable newField;
						newField  = new GdbScalarVariable(_debugSession, fieldName, fieldType, fieldValue, _nodeID + _numNodes);
						_fields.add(newField);
						_numNodes++;
						break;
					}   		
				}				
			}
			else			
			{
				// the value does not start with {
				// this is a scalar		
				GdbScalarVariable newField;
				newField  = new GdbScalarVariable(_debugSession, fieldName, fieldType, fieldValue, _nodeID + _numNodes);
				_fields.add(newField);
				_numNodes++;
				
				if (lastone)
					break;
				
	    		if (comma+2 > parseStr.length())
	    		{
	    			// done!
	    			break;
	    		}
				
		    	parseStr = parseStr.substring(comma + 2);
			}
		} // while
	}
		
    private int _rep;
    private String _gdbData;		// store data from gdb for comparison later
    private String _prefix;
    private int _numNodes;
    
    protected String _className;
    protected boolean _expand;
    protected int _startChild;
    protected int _endChild;
    protected Vector _fields;
}

