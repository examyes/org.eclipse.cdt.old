package com.ibm.debug.gdbPicl.objects;

import com.ibm.debug.epdc.EStdTreeNode;
import com.ibm.debug.epdc.EStdArrayItem;
import com.ibm.debug.gdbPicl.DebugSession;
import java.util.*;

public class GdbArrayVariable extends GdbVariable {

	/**
	 * Constructor for GdbArrayClass
	 */
	public GdbArrayVariable(
		DebugSession debugSession,
		String name,
		String type,
		String value,
		int nodeID) {
		super(debugSession, name, type, nodeID);
		
		_elements = new Vector();
		
		value.trim();
		_gdbData = value;	
				
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
	}

	/**
	 * @see GdbVariable#getScalarValue()
	 */
	public String getScalarValue() {
		return null;
	}

	/**
	 * @see GdbVariable#getTreeNode()
	 */
	public EStdTreeNode getTreeNode() {
		
	  EStdArrayItem myarray = new EStdArrayItem(_elements.size(), 0, _name, _type, _type, new short[1], (short)1);
	  EStdTreeNode rootNode = new EStdTreeNode(_nodeID, myarray);
	  
	  for (int i=0; i<_elements.size(); i++)
	  {
	  	GdbVariable element = (GdbVariable)_elements.elementAt(i);

		EStdTreeNode childNode = element.getTreeNode();
	  	
	  	rootNode.addChildNode(childNode, element.numNodes());
	  }
   	  
   	  return rootNode;
	}

	/**
	 * @see GdbVariable#numNodes()
	 */
	public int numNodes() {
		return 0;
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
	
	
	/*
		For creating tree structure of arrays
		Possible input:
			{1}
			{{a=3}}
			{1, 2, 3, 4, 5}
			{{1, 2}, {3, 4}}
			{{a=1, b=2, c=3}, {a=1, b=2, c=3} ...}}
			{1 <repeat # times>, 1, 2, 3, 4, 5, 6, ...} for large array
	*/
	private void createTree(String parseStr)
	{
		String token;
		
		// trim start and end {}
		if (parseStr.startsWith("{"))
		{
			parseStr=parseStr.substring(1);
		}
		
		if (parseStr.endsWith("}"))
		{
			parseStr=parseStr.substring(0, parseStr.length()-1);
		}
				
		// get the first token, delimited by comma
		int comma = parseStr.indexOf(",");

		if (comma == -1)
		{
			// take the string and pretend we only have one element
			String fieldName = _name+"[0]";
			String fieldValue = parseStr;
			String fieldType = _type;
			GdbScalarVariable element = new GdbScalarVariable(_debugSession, fieldName, fieldType, fieldValue, _nodeID);
			_elements.add(element);
			return;
		}
		
		String testStr = parseStr.substring(0, comma);
		
		// if the token contains brace
		if (testStr.startsWith("{"))
		{
			// it could be an array of objects or multi-dimensional array
			// if the token contains "="
			if (testStr.indexOf("=") != -1)
			{
					// this is an array of objects
					createObjectArray(parseStr);
			}
			else
			{				
				// else
				// this is a multi-dimensional array
				createMutiDimensional(parseStr);
			}
		}
		else
		{	
			// otherwise, this is an array of scalar values
			createScalarArray(parseStr);
		}			

	}	
	
	private void createScalarArray(String parseStr)
	{
		int lastComma = 0;
		String fieldValue;
		String fieldName;
		String fieldType = _type;
		int comma = parseStr.indexOf(",");
		int counter = 0;
		
		// tokenize by "," and get the values
		while (comma != -1)
		{
			// construct name
			fieldName = _name + "[" + counter + "]";
			
			// get value
			fieldValue = parseStr.substring(0, comma);
			parseStr = parseStr.substring(comma+2);

			comma = parseStr.indexOf(",");			

			// create element 			
	    	GdbScalarVariable newElement  = new GdbScalarVariable(_debugSession, fieldName, fieldType, fieldValue, _nodeID);
			_elements.add(newElement);
			
			counter++;
			
			// if comma == -1, last element
			if (comma == -1)
			{
				fieldValue = parseStr;
				fieldName = _name + "[" + counter + "]";
		    	GdbScalarVariable lastElement  = new GdbScalarVariable(_debugSession, fieldName, fieldType, fieldValue, _nodeID);
		    	_elements.add(lastElement);
			}
		}
	}
	
	/*
		Parse the string provided, delimiting by "},"
		Create multi-dimensional arrays
		
		{{1, 2}, {3, 4}}
	*/
	private void createMutiDimensional(String parseStr)
	{
		int endElement=0;
		int counter=0;
		String fieldValue;
		String fieldName;		
		String fieldType = _type;
		
		while (parseStr != "" && parseStr != null)
		{
			fieldName = _name + "[" + counter + "]";
			
			// delimite by "{}"
			endElement = findMatchingEnd(parseStr);
			
			if (endElement == -1)
			{
				// something wrong with data, return as a plain scalar
				fieldValue = parseStr;
				GdbScalarVariable element = new GdbScalarVariable(_debugSession, fieldName, fieldType, fieldValue, _nodeID);
				_elements.add(element);
				break;
			}
			
			if (endElement+2 < parseStr.length())
			{						
				fieldValue = parseStr.substring(0, endElement+1);
				GdbArrayVariable element = new GdbArrayVariable(_debugSession, fieldName, fieldType, fieldValue, _nodeID);	
				_elements.add(element);
				
				parseStr = parseStr.substring(endElement+3);
				counter++;				
			}
			else
			{
				// last element
				fieldValue = parseStr;
				GdbArrayVariable element = new GdbArrayVariable(_debugSession, fieldName, fieldType, fieldValue, _nodeID);	
				_elements.add(element);
				parseStr = "";
				break;				
			}
		}
	}
	
	/*
		Construct an array of objects
		{{a=1, b=2, c=3}, {a=1, b=2, c=3} ...}}
	*/
	private void createObjectArray(String parseStr)	
	{
		int endElement=0;
		int counter=0;
		String fieldValue;
		String fieldName;		
		String fieldType = _type;
		
		while (parseStr != "" && parseStr != null)
		{
			fieldName = _name + "[" + counter + "]";
			
			// delimite by "{}"
			endElement = findMatchingEnd(parseStr);
			
			if (endElement == -1)
			{
				// something wrong with data, return as a plain scalar
				fieldValue = parseStr;
				GdbScalarVariable element = new GdbScalarVariable(_debugSession, fieldName, fieldType, fieldValue, _nodeID);
				_elements.add(element);
				break;
			}
			
			if (endElement+2 < parseStr.length())
			{						
				fieldValue = parseStr.substring(0, endElement+1);
				GdbObjectVariable element = new GdbObjectVariable(_debugSession, fieldName, fieldType, fieldValue, fieldName, _nodeID);	
				_elements.add(element);
				
				parseStr = parseStr.substring(endElement+3);
				counter++;				
			}
			else
			{
				// last element
				fieldValue = parseStr;
				GdbObjectVariable element = new GdbObjectVariable(_debugSession, fieldName, fieldType, fieldValue, fieldName, _nodeID);	
				_elements.add(element);
				parseStr = "";
				break;				
			}
		}

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
	
	private Vector _elements;
	private String _gdbData;

}

