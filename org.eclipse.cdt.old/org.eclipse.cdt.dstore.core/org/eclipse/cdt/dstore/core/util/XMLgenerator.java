package com.ibm.dstore.core.util;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.model.*;

import java.io.*;
import java.util.*;

public class XMLgenerator
{
    private int          _state;
    
    private StringBuffer _document;
    
    private int          _indent;
    private Stack        _tagStack;
    private PrintStream  _writer;
    private int          _bufferSize;
    private boolean      _generateBuffer;
    private boolean      _updateAll;
    private boolean      _ignoreDeleted;
  
    public static final int EMPTY = 0;
    public static final int OPEN  = 1;
    public static final int CLOSE = 2;
    public static final int BODY  = 3;
    
    
    public XMLgenerator()
    {
        _state = EMPTY;
       	_bufferSize = 100000;
	
        _document = new StringBuffer(_bufferSize);
	
        _indent = 0;
		_generateBuffer = true;
		_ignoreDeleted = false;
        _tagStack = new Stack();
    }
    
    public void setIgnoreDeleted(boolean flag)
    {
    	_ignoreDeleted = flag;
    }
    
    public void setWriter(PrintStream writer)
    {
        _writer = writer;
    }

    public void setBufferSize(int size)
    {
	_bufferSize = size;
    }
    
    public void setGenerateBuffer(boolean flag)
    {
	_generateBuffer = flag;    
    }

    private void append(String buffer)
    {
        _document.append(buffer);
    }
    
    private void append(StringBuffer buffer)
    {
	_document.append(buffer);	
    }
    
    private void nextLine()
    {
        if (_writer != null)
        {
	  _document.append("\n");
  
	  int length = _document.length();
	  if (length > _bufferSize)
	    {	      
		flush();
	    }
	}	
      }

    public void flush()
    {
	if (_document.length() > 0)
	    {
		try
		    {
			_writer.println(_document);	      
			_writer.flush();
			_document.setLength(0);
		    }
		catch (Exception e)
		    {
			System.out.println(e);
			_writer = null;
		    }
	    }
    }
    
    
    private void indent()
    {
        for (int i = 0; i < _indent; i++)
	    {
		append(" ");
	    }
    }
    
    
    private void startTag(String name)
    {
        if (_state == OPEN)
	    {
		append(">");
		_indent++;
	    }
        if (_state == CLOSE)
	    {
		_indent--;
	    }
        if (_state == BODY)
	    {
		nextLine();
	    }
        indent();
        if (_document == null)
	    {
		append("<");
		append(name);
	    }
        else
	    {
		append("<");
		append(name);
	    }
        _tagStack.push(name);
        _state = OPEN;
    }
    
    private void endTag(String name)
    {
        String top = (String)_tagStack.pop();
        if (_state == CLOSE)
	    {
	    }
        else if (_state == OPEN)
	    {
		if (top == name)
		    {
			append("/>");
			if (_tagStack.empty())
			    {
				_state = CLOSE;
			    }
			else
			    {
				_state = BODY;
			    }
		    }
	    }
        else if (_state == BODY)
	    {
		if (top == name)
		    {
			nextLine();
			_indent--;
			indent();
			append("</");
			append(name);
			append(">");
			if (_tagStack.empty())
			    {
				_state = CLOSE;
			    }
		    }
	    }
    }
    
    private void addAttribute(String name, String value)
    {
        if (_state != OPEN)
	    {
	    }
	
	StringBuffer niceValue = null;
	if (value != null)
	    {
		value = value.trim();
		niceValue = prepareStringForXML(value);
		
		append(" ");
		append(name);
		append("=\"");
		append(niceValue);
		append("\"");
	    }
	else
	    {
		append(" ");
		append(name);
		append("=\"\"");
	    }

      }
    
    private void addFile(File file, int size)
    {
	if (_state == OPEN)
	      {
		  append(">");
		  
		  _indent++;
		  _state = BODY;
	      }
	  if (_state == BODY)
	      {
		  try
		      {
			  flush();

			  FileInputStream inFile = new FileInputStream(file);
			  			  
			  // read all bytes into buffer
			  byte[] buffer  = new byte[size]; 
			  int written = 0;
			  while (written < size)
			      {			  
				  int available = inFile.available();
				  int read = inFile.read(buffer, written, available);				  
				  written += read;
			      }
			  
			  // send everything across
			  _writer.write(buffer, 0, size);
			  _writer.flush();
			  
			  inFile.close();
		      }
		  catch (IOException e)
		      {
			  System.out.println(e);
		      }
	      }
	  else if (_state == EMPTY)
	      {
	      }
	  else if (_state == CLOSE)
	      {
	      }	  
      }

    private void addFile(byte[] bytes, int size)
    {
	if (_state == OPEN)
	    {
		append(">");
		
		_indent++;
		_state = BODY;
	    }
	if (_state == BODY)
	    {
		flush();
		
		// send everything across
		_writer.write(bytes, 0, size);
		_writer.flush();
	    }
	else if (_state == EMPTY)
	    {
	    }
	else if (_state == CLOSE)
	    {
	    }	  
    }
    
    private void addData(StringBuffer data)
    {
        if (_state == OPEN)
	    {
		append(">");
		
		_indent++;
		_state = BODY;
	    }
        if (_state == BODY)
	    {
		if (_generateBuffer && (data.length() > 0))
		    {
			StringBuffer text = prepareStringForXML(data);
			if (text != null && text.length() > 0)
			    {
				nextLine();
				indent();
				append("<Buffer>");  
				nextLine();
				indent();
				append(text.toString());
				nextLine();
				indent();
				append("</Buffer>");  
			    }
		    }
		else
		    {
			append("");
		    }
	    }
        else if (_state == EMPTY)
	    {
	    }
        else if (_state == CLOSE)
	    {
	    }
      }
    
    public StringBuffer document()
    {
	return _document;
    }
    
    public void empty()
    {
        _indent = 0;
        _document.delete(0, _document.length());
    }
    
    public static StringBuffer prepareStringForXML(StringBuffer input) 
    {
	StringBuffer output = new StringBuffer();
	
	for (int idx=0; idx < input.length(); idx++)
	    {
		char currChar = input.charAt(idx);
		switch (currChar)
		    {	    
		    case '&':
			output.append("&amp;");
			break;	    
		    case '"':
			output.append("&quot;");
			break;	    
		    case '\'':
			output.append("&apos;");
			break;	    
		    case '<':
			output.append("&lt;");
			break;	    
		    case '>':
			output.append("&gt;");
			break;	    
		    case ';':
			output.append("&#59;");
			break;	    
		    default:
			output.append(currChar);
			break;	    
		    }
	    }
	
	return output;
    }
    
    public static StringBuffer prepareStringForXML(String input) 
    {
	StringBuffer output = new StringBuffer();
	
	for (int idx=0; idx < input.length(); idx++)
	    {
		char currChar = input.charAt(idx);
		switch (currChar)
		    {	    
		    case '&':
			output.append("&amp;");
			break;	    
		    case '"':
			output.append("&quot;");
			break;	    
		    case '\'':
			output.append("&apos;");
			break;	    
		    case '<':
			output.append("&lt;");
			break;	    
		    case '>':
			output.append("&gt;");
			break;	    
		    case ';':
			output.append("&#59;");
			break;	    
		    default:
			output.append(currChar);
			break;	    
		    }
	    }
	
	return output;
    }
    
    public synchronized void generate(DataElement object, byte[] bytes, int size)
    {
	generate(object, bytes, size, false);
    }

    public synchronized void generate(DataElement object, byte[] bytes, int size, boolean isAppend)
    {
        String tagType = "File";
	if (isAppend)
	    {
		tagType += ".Append";
	    }
	
	if (object != null)
	    {
		startTag(tagType);
		addAttribute(DE.P_TYPE, object.getAttribute(DE.A_TYPE));
		addAttribute(DE.P_ID, object.getAttribute(DE.A_ID));
		addAttribute(DE.P_NAME, object.getAttribute(DE.A_NAME));
		addAttribute(DE.P_VALUE, object.getAttribute(DE.A_VALUE));
		addAttribute(DE.P_SOURCE, object.getAttribute(DE.A_SOURCE));

		if (object.isReference())
		    {
			addAttribute(DE.P_ISREF, "true");
		    }
		else
		    {
			addAttribute(DE.P_ISREF, "false");
		    }
		
		addAttribute(DE.P_DEPTH, "" + size);
		addFile(bytes, size);
            }

	// end generation
	endTag(tagType);
    }

    public void generate(DataElement object, int depth)
    {
        generate(object, depth, null);
    }
    

    public void generate(DataElement object, int depth, File file)
    {
	if ((object != null) && 
		((file != null) || (depth >= 0)) )		
	    {
		String tagType = "DataElement";
		if (file != null)
		    {
			tagType = "File";
		    }

		if (object.isUpdated() && !_generateBuffer && (file == null))
		    {
		    }    
		else
		    {	      			
			  if (object.isDeleted() && _ignoreDeleted)			
			  {
			  }	
			  else
			  {			  
				startTag(tagType);
				addAttribute(DE.P_TYPE, object.getAttribute(DE.A_TYPE));
				addAttribute(DE.P_ID, object.getAttribute(DE.A_ID));
				addAttribute(DE.P_NAME, object.getAttribute(DE.A_NAME));
				addAttribute(DE.P_VALUE, object.getAttribute(DE.A_VALUE));
				addAttribute(DE.P_SOURCE, object.getAttribute(DE.A_SOURCE));

				if (object.isReference())
			    {
					addAttribute(DE.P_ISREF, "true");
			    }
				else
			    {
					addAttribute(DE.P_ISREF, "false");
			    }
			
				if (file != null)
				{
			 	   long length = file.length();
			  	  addAttribute(DE.P_DEPTH, "" + length);
			  	  addFile(file, (int)length);
				}
				else
			    {
					addAttribute(DE.P_DEPTH, "" + object.depth());
					addData(object.getBuffer());
					object.setUpdated(true);
				
					if (!object.isReference() && depth >= 0)
				    {
						for (int i = 0; i < object.getNestedSize(); i++)
					    {
							generate(object.get(i), depth - 1, file);
					    }
				    }
			    }	      
			  }
			  
			// end generation
			endTag(tagType);

		    }
	    }
    }

}
