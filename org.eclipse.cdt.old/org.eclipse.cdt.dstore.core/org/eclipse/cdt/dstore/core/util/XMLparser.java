package com.ibm.dstore.core.util;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.util.*;
import com.ibm.dstore.core.model.*;

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.*;

public class XMLparser
{
    private DataStore         _dataStore;
    private DataElement       _rootDataElement;
    private Stack             _tagStack;
    private Stack             _objStack;
    
    private boolean           _isFile;
    private String            _tagType;

    private byte[]            _byteBuffer;
    private boolean           _panic = false;
    
    public XMLparser(DataStore dataStore)
    {
        _dataStore = dataStore;
        _tagStack = new Stack();
        _objStack = new Stack();

	_byteBuffer = new byte[10000];
    }
    
    public void readFile(BufferedInputStream reader, int size, String path)
    {
	byte[] buffer = new byte[size];
	boolean done = false;

	int written = 0;
	try
	    {
		// hack to deal with platform inconsistencies
		int first = reader.read();
		if (first != 10)
		    {
			written = 1;
			buffer[0] = (byte)first;
		    }
	    }
	catch (IOException e)
	    {
	    }
	
	while (written < size)
	    {			
		try
		    {
			int available = reader.available();
			int read = reader.read(buffer, written, size - written);				  
			written += read;
		    }
		catch (IOException e)
		    {
			System.out.println("XMLparser:read:" + e);
			_panic = true;
		    }
	    }

	if (_tagType.equals("File.Append"))
	    {
		_dataStore.appendToFile(path, buffer);
	    }
	else
	    {
		_dataStore.saveFile(path, buffer);
	    }
    }

    public String readLine(BufferedInputStream reader)
    {
	boolean done = false;
	int offset = 0;

	while (!done)
	    {
		try
		    {
			int in = reader.read();
			byte aByte = (byte)in;			
			if ((in <= 0) ||
			    (aByte == '\n') || (aByte == '\0') || (aByte == '\r'))
			    {
				done = true;
			    }
			else
			    {
				_byteBuffer[offset] = aByte;
				offset++;
			    }
		    }
		catch (IOException e)
		    {
			done = true; 
			System.out.println("XMLparser:readLine:" + e);
			_panic = true;
			return null;
		    }
	    }

	if (offset > 0)
	    {
		String result = new String(_byteBuffer, 0, offset);
		return result; 
	    }
	else
	    {
		return null;
	    }
    }

  public DataElement parseDocument(BufferedInputStream reader) throws IOException
      {
	      {
		  _tagStack.clear();
		  _objStack.clear();
		  		  
		  _rootDataElement = null;
		  _isFile = false;
		  _tagType = "DataElement";
		  
		  DataElement parent = null;
		  _rootDataElement = null;
		  String matchTag = null;
		  
		  long t1 = 0;
		  long t2 = 0;

		  boolean done = false;
		  while (!done)
		      {
			  String xmlTag = readLine(reader);
			  
			  if (xmlTag != null)
			      {
				  String trimmedTag = xmlTag.trim();
				  if (!_tagStack.empty())
				      {
					  matchTag = (String)_tagStack.peek();
				      }	     
				  if (trimmedTag.equals("<Buffer>"))
				      {
					  _tagType = "Buffer";
					  _tagStack.push("</Buffer>");
				      }
				  else if (trimmedTag.equals("</Buffer>"))
				      {
					  _tagType = "DataElement";
					  _tagStack.pop();
				      }
				  else if (_tagType.equals("Buffer"))
				      {              
					  String buffer = convertStringFromXML(xmlTag);
					  parent.appendToBuffer(buffer);
				      }
				  else if ((matchTag != null) && trimmedTag.equals(matchTag))
				      {
					  _tagStack.pop();
					  if (_tagStack.empty())
					      {
						  done = true;
					      }
					  else if (_tagStack.size() == 1)
					      {
						  parent = _rootDataElement;
					      }
					  else
					      {
						  parent = (DataElement)_objStack.pop();
					      }
				      }
				  else if (xmlTag.length() > 0)
				      {
					  xmlTag = xmlTag.trim();
					  try
					      {
						  if (parent != null)
						      {
							  _objStack.push(parent);
						      }

						  DataElement result = parseTag(xmlTag, parent);
						  if (parent == null && _rootDataElement == null)
						      {
							  _rootDataElement = result;
							  _rootDataElement.setParent(null);
						      }

						  parent = result;

						  if (_isFile && (result != null))
						      {
							  int size = result.depth();
							  String path = result.getSource();
							  if (path != null)
							      {
								  readFile(reader, size, path);
							      }
							  _isFile = false;
						      }
						  
						  String endTag = new String("</" + _tagType + ">");
						  _tagStack.push(endTag);
					     
					      }
					  catch (Exception e)
					      {
						  e.printStackTrace();
						  System.out.println(e);
						  return _rootDataElement;
					      }
				      }
			      }

			  if (_panic)
			      return null;
		      }
	      }

        return _rootDataElement;
      }

  protected synchronized DataElement parseTag(String fullTag, DataElement parent)
    {
        int currentIndex;
        fullTag = fullTag.substring(1, fullTag.length() - 1);

        // get type
        int nextSpace = fullTag.indexOf(' ');
        if (nextSpace > 0)
        {

          String[] attributes = new String[DE.A_SIZE];

          // tag type
          String tagType = fullTag.substring(0, nextSpace);
          if (tagType.equals("File"))
	      {
		  _isFile = true;
		  _tagType = tagType;
	      }
	  else if (tagType.equals("File.Append"))
	      {
		  _isFile = true;
		  _tagType = tagType;		  
	      }

          int index = 0;
          int nextQuote = 0;
          int nextnextQuote = nextSpace;
          while ((index < DE.A_SIZE) && (nextQuote >= 0))
          {
            nextQuote     = fullTag.indexOf('\"', nextnextQuote + 1);
            nextnextQuote = fullTag.indexOf('\"', nextQuote + 1);

            if ((nextQuote >= 0) && (nextnextQuote > nextQuote) && (fullTag.length() > nextnextQuote))
            {
	      String attribute = fullTag.substring(nextQuote + 1, nextnextQuote);	      

	      //	     *** attributes[index] = convertStringFromXML(attribute);  
	      attributes[index] = attribute;  
              index++;
            }
          }

	  DataElement result = null;
	  if (attributes.length == DE.A_SIZE)
	      {
		  if (_isFile)
		      {
			  result = _dataStore.createObject(parent, attributes);		
		      }
		  else
		      {	      
			  String id = attributes[DE.A_ID]; 
			  if (parent != null && _dataStore.contains(id))
			      {
				  result = _dataStore.find(id);      
				  result.setAttributes(attributes);
				  if (parent == _rootDataElement)
				      {
					  DataElement rParent = result.getParent();
					  parent = rParent;

					  _rootDataElement.addNestedData(result, false);
				      }
				  else
				      {
					  if (result.getParent() == null)
					      {
						  result.setParent(parent);
					      }
				      }
				  
				  if (parent != null)
				      {
					  parent.addNestedData(result, true);
				      }
				  else
				      {
					  System.out.println("NULL!");
				      }
			      }
			  else
			      {

				  String isRefStr = attributes[DE.A_ISREF];
				  if ((isRefStr != null) && isRefStr.equals("true"))
				      {
					  // new reference
					  String origId = attributes[DE.A_NAME];
					  if (_dataStore.contains(origId))
					      {

						  DataElement to = _dataStore.find(origId);
						  if (parent != null)
						      {
							  result = _dataStore.createReference(parent, to, 
											      attributes[DE.A_TYPE]);
							  
						      }
						  else
						      {
							  System.out.println("NULL2!");
						      }
					      }
					  else
					      {
						  // creating reference to unknown object
						  result = _dataStore.createObject(parent, attributes);
					      }
				      }
				  else
				      {
					  // new object					  
					  result = _dataStore.createObject(parent, attributes);
				      }
				  
			      }			  
		      }
	      } 

	  return result;
	}	
	
        return null;
    }


  public static String convertStringFromXML(String input) 
  {
    StringBuffer result = new StringBuffer();    
    StringTokenizer tokenizer = new StringTokenizer(input, "&;");

    while (tokenizer.hasMoreElements())
      {
	String token = (String)tokenizer.nextElement();
	if (token.equals("amp"))
	  result.append("&");
	else if (token.equals("#59"))
	  result.append(";");
	else if (token.equals("quot"))
	  result.append("\"");
	else if (token.equals("apos"))
	  result.append("\'");
	else if (token.equals("lt"))
	  result.append("<");
	else if (token.equals("gt"))
	  result.append(">"); 
	else 
	  result.append(token);	    
      }

    return result.toString();
  }


}






