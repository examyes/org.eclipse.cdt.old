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
	    
	    boolean done = false;
	    
    	String parseStr = value.substring(1,value.length()-1);
    	    
	    while(parseStr != "")
	    {
	    	int equal = parseStr.indexOf(" = ");
	    	int comma = parseStr.indexOf(",");
	    	
	    	if (equal != -1 && comma != -1)
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
			    		String fullFieldName = prefix + "." + fieldName;
						fieldType = GdbVariableMonitor.getExpressionType((GdbDebugSession)debugSession, fullFieldName);		    								
		    		}
		    		else			    		
		    		{
		    			// if name is enclosed by <>, it's the actual type
		    			fieldType = fieldName.substring(1, fieldName.length()-1);
		    		}
					GdbScalarVariable newField;
			    	parseStr = parseStr.substring(comma + 2);
					newField  = new GdbScalarVariable(debugSession, fieldName, fieldType, fieldValue, nodeID);
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
		    		
			    		if (!fieldName.startsWith("<") && !fieldName.endsWith(">"))
			    		{
				    		String fullFieldName = prefix + "." + fieldName;
				    		prefix = fullFieldName;
							fieldType = GdbVariableMonitor.getExpressionType((GdbDebugSession)debugSession, fullFieldName);		    								
			    		}
			    		else			    		
			    		{
			    			fieldType = fieldName.substring(1, fieldName.length()-1);
			    		}
		
			    		fieldValue = parseStr.substring(equal+3, endClass+1);
			    		newField = new GdbObjectVariable(debugSession, fieldName, fieldType, fieldValue, prefix, nodeID);
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
			    		String fullFieldName = prefix + "." + fieldName;
						fieldType = GdbVariableMonitor.getExpressionType((GdbDebugSession)debugSession, fullFieldName);		    		
		    		}
		    		else			    		
		    		{
		    			fieldType = fieldName.substring(1, fieldName.length()-1);
		    		}
	    			
	    			String fieldValue = parseStr.substring(equal + 3, endBrace);
	    			
					newField  = new GdbScalarVariable(debugSession, fieldName, fieldType, fieldValue, nodeID);
				
					_fields.add(newField);	    			
	    			
	    			break;
	    		}	    		
	    		// something wrong...missing end "}"
	    		else
	    		{
	    			GdbScalarVariable newField;
	    			String fieldName = parseStr.substring(0, equal);
	    			String fieldValue = parseStr.substring(equal + 3);
	    			
					newField  = new GdbScalarVariable(debugSession, fieldName, "int", fieldValue, nodeID);
				
					_fields.add(newField);	    	    			

	    			break;
	    		}
	    	}
	    	else
	    	{
	    		// all done... parseStr is empty
	    		break;
	    	}
	    	
	    }
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
	}

	/**
	 * @see GdbVariable#getScalarValue()
	 */
	public String getScalarValue() {
		return null;
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

    private int _rep;
    protected String _className;

    protected boolean _expand;
    protected int _startChild;
    protected int _endChild;

    protected Vector _fields;
}

