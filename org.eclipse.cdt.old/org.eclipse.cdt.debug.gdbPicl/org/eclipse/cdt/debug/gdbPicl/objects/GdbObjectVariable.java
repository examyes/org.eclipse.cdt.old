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
	    
	    value.trim();
    	String parseStr = value.substring(1,value.length()-1);
    	    
		createTree(parseStr);    	    

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
		
			if (parseStr.startsWith("{") && parseStr.endsWith("}"))
			{
		    	parseStr = s.substring(1,s.length()-1);   	    
			}	    	
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
		return _fields.size();
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
		{a=1, b=2, c=3}
		{<type_name> = <something>, a=1, b=2, c=3}
		{<type_name> = {a=1, b=2, c=3}, d=4, e=5}
		{a = {b=1, c=2, d=3}, x=y, k=z}
		{x=y, k=z, a = {...}}
		{a = {1, 2, 3, 4}, b=4, d=5}
		
	*/
	private void createTree(String parseStr)
	{
		while(parseStr != "")
	    {
	    	int equal = parseStr.indexOf(" = ");
	    	int comma = parseStr.indexOf(",");
	    	
	    	if (equal != -1 && comma != -1 && !parseStr.startsWith("{{"))
	    	{
		    	String fieldName = parseStr.substring (0, equal);	
		    	
		    	if (fieldName.startsWith("{"))
		    	{    		
		    		fieldName = fieldName.substring(1);
		    	}
		    	    	
		    	String fieldValue = parseStr.substring(equal + 3, comma);
		    	
		    	if (!fieldValue.startsWith("{"))
		    	{
		    		// scalar value    	
		    		
		    		String fieldType;
		    		
		    		if (!fieldName.startsWith("<") && !fieldName.endsWith(">"))
		    		{
			    		String fullFieldName = _prefix + "." + fieldName;
						fieldType = GdbVariableMonitor.getExpressionType((GdbDebugSession)_debugSession, fullFieldName);		    								
		    		}
		    		else			    		
		    		{
		    			// if name is enclosed by <>, it's the actual type
		    			fieldType = fieldName.substring(1, fieldName.length()-1);
		    		}
					GdbScalarVariable newField;
			    	parseStr = parseStr.substring(comma + 2);
					newField  = new GdbScalarVariable(_debugSession, fieldName, fieldType, fieldValue, _nodeID);
					_fields.add(newField);
		    	}
		    	else
		    	{
		    		// a sub tree, need to create another GdbObjectVariable
		    		// object value
		    		GdbObjectVariable newField;
					int endClass = findMatchingEnd(parseStr);
		    		
		    		if (endClass != -1)
		    		{
		    			String fieldType;
		    			String prefix = _prefix;
			    		if (!fieldName.startsWith("<") && !fieldName.endsWith(">"))
			    		{
				    		String fullFieldName = _prefix + "." + fieldName;
				    		prefix = fullFieldName;
							fieldType = GdbVariableMonitor.getExpressionType((GdbDebugSession)_debugSession, fullFieldName);		    								
			    		}
			    		else			    		
			    		{
			    			fieldType = fieldName.substring(1, fieldName.length()-1);
			    		}
		
			    		fieldValue = parseStr.substring(equal+3, endClass+1);
			    		newField = new GdbObjectVariable(_debugSession, fieldName, fieldType, fieldValue, prefix, _nodeID);
			    		_fields.add(newField);
		    		
		    			if (endClass+2 < parseStr.length())
		    			{
				    		parseStr = parseStr.substring(endClass + 2);
		    			}
		    			else
		    			{
		    				break;
		    			}
		    		}
		    		else
		    		{	    			
		    			// error
		    			break;
		    		}			    		
		    	}					
	    	}
	    	else if (equal != -1 && comma == -1)
	    	{
	    		// last field
	    		int endBrace = parseStr.indexOf("}");
	    		if (endBrace != -1)
	    		{
	    			GdbScalarVariable newField;
	    			String fieldName = parseStr.substring(0, equal);
	    			String fieldType;
	    			
	    			if (fieldName.startsWith("{"))
			    	{
			    		fieldName = fieldName.substring(1);
			    	}
			    	
		    		if (!fieldName.startsWith("<") && !fieldName.endsWith(">"))
		    		{
			    		String fullFieldName = _prefix + "." + fieldName;
						fieldType = GdbVariableMonitor.getExpressionType((GdbDebugSession)_debugSession, fullFieldName);		    		
		    		}
		    		else			    		
		    		{
		    			fieldType = fieldName.substring(1, fieldName.length()-1);
		    		}
	    			
	    			String fieldValue = parseStr.substring(equal + 3, endBrace);
	    			
					newField  = new GdbScalarVariable(_debugSession, fieldName, fieldType, fieldValue, _nodeID);
				
					_fields.add(newField);	    			
	    			
	    			break;
	    		}	    		
	    		// something wrong...missing end "}"
	    		else
	    		{
	    			GdbScalarVariable newField;
	    			String fieldName = parseStr.substring(0, equal);
	    			String fieldValue = parseStr.substring(equal + 3);
   					String fieldType;
	    			
	    			if (fieldName.startsWith("{"))
			    	{
			    		fieldName = fieldName.substring(1);
			    	}
			    	
		    		if (!fieldName.startsWith("<") && !fieldName.endsWith(">"))
		    		{
			    		String fullFieldName = _prefix + "." + fieldName;
						fieldType = GdbVariableMonitor.getExpressionType((GdbDebugSession)_debugSession, fullFieldName);		    		
		    		}
		    		else			    		
		    		{
		    			fieldType = fieldName.substring(1, fieldName.length()-1);
		    		}
	    			
					newField  = new GdbScalarVariable(_debugSession, fieldName, fieldType, fieldValue, _nodeID);
				
					_fields.add(newField);	    	    			

	    			break;
	    		}
	    	}
	    	else if (equal == -1 || parseStr.startsWith("{{"))
	    	{
	    		// this is an array 
	    		//1, 2, 3, 4, 5
	    		String fieldName = _name+"[]";	
	    		String fieldValue;
	    		String fieldType;
	    		if (comma != -1)		    			    	    	
			    	fieldValue = parseStr.substring(0, comma);
			    else
			    	fieldValue = parseStr;
			    	
			    if (parseStr.indexOf("{") == -1 && parseStr.indexOf("}") == -1)
			    {	
			    	// scalar
			    	fieldType = _type;
			    	GdbScalarVariable newField  = new GdbScalarVariable(_debugSession, fieldName, fieldType, fieldValue, _nodeID);				
					_fields.add(newField);	
					
					if (comma != -1)
					{  
						parseStr = parseStr.substring(comma+2);  			
					}
					else
					{
						parseStr = "";
					}
			    }					
			    else
			    {
			    	// complex
			    	int endClass = findMatchingEnd(parseStr);
		    		fieldType = _type;
		    		
		    		if (endClass != -1)
		    		{
			    		fieldValue = parseStr.substring(0, endClass+1);
		    		}
		    		else
		    		{
		    			fieldValue = parseStr;
		    		}
		    		
		    		GdbObjectVariable newField  = new GdbObjectVariable(_debugSession, fieldName, fieldType, fieldValue, _prefix, _nodeID);
		    		_fields.add(newField);
		    		
		    		if (endClass == -1)
		    			break;
		    		
	    			if (endClass+2 < parseStr.length())
	    			{
			    		parseStr = parseStr.substring(endClass + 2);
	    			}
	    			else
	    			{
	    				break;
	    			}
			    }					
	    	}
	    	else
	    	{
	    		// all done... parseStr is empty
	    		break;
	    	}
	    	
	    }
	}
		

    private int _rep;
    private String _gdbData;		// store data from gdb for comparison later
    private String _prefix;
    
    protected String _className;
    protected boolean _expand;
    protected int _startChild;
    protected int _endChild;
    protected Vector _fields;
}

