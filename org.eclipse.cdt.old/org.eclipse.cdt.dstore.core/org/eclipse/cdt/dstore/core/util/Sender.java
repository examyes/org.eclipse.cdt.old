package org.eclipse.cdt.dstore.core.util;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.core.util.*;

import java.net.*; 
import java.io.*;
import java.lang.*;
import java.util.*;

public class Sender implements ISender
{
  private Socket             _socket;
  private PrintStream        _outFile;
  private BufferedWriter     _outData;
  private XMLgenerator       _xmlGenerator;
  private DataStore          _dataStore;  

  public Sender(Socket socket, DataStore dataStore)
    {
      _socket = socket;
      _dataStore = dataStore;

      _xmlGenerator = new XMLgenerator(_dataStore);
      try
 	  {
	      int bufferSize = _socket.getSendBufferSize();	  
	  
	      _socket.setSendBufferSize(bufferSize);
	      _xmlGenerator.setBufferSize(bufferSize);
	  }
      catch (SocketException e)
	  {
	   System.out.println("Sender:" + e);	  
	  }      
      try
      {
        _outFile = new PrintStream(_socket.getOutputStream());
        _outData = new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream(), "UTF-8"));
        
	    _xmlGenerator.setFileWriter(_outFile);	  
	    _xmlGenerator.setDataWriter(_outData);
	    _xmlGenerator.setGenerateBuffer(false);
      }
      catch (java.io.IOException e)
      {
	  System.out.println("Sender:" + e);
      }
    }

  public Socket socket()
      {
        return _socket;
      }

  public void sendDocument(String document)
      {
    	synchronized(_outData)
        {
        	try
        	{
		    _outData.write(document, 0, document.length());
		    _outData.flush();
        	}
        	catch (IOException e)
        	{
		    e.printStackTrace();
        	}        	
        }
      }

  public void sendFile(DataElement objectRoot, File file, int depth)
      {
     	synchronized(_outData)
        {
          _xmlGenerator.empty();	  
          _xmlGenerator.generate(objectRoot, depth, file);
          _xmlGenerator.flushData();
        }
      }

  public void sendFile(DataElement objectRoot, byte[] bytes, int size, boolean binary)
      {
    	synchronized(_outData)
        {
          _xmlGenerator.empty();	  
          _xmlGenerator.generate(objectRoot, bytes, size, false, binary);
          _xmlGenerator.flushData();
        }
      }

  public void sendAppendFile(DataElement objectRoot, byte[] bytes, int size, boolean binary)
      {
	  synchronized(_outFile)
	      {
		  _xmlGenerator.empty();	  
		  _xmlGenerator.generate(objectRoot, bytes, size, true, binary);
		  _xmlGenerator.flushData();
	      }
      }
    
    public void sendDocument(DataElement objectRoot, int depth)
    {
	synchronized(_outData)
	    {
		_xmlGenerator.empty();	  
		_xmlGenerator.generate(objectRoot, depth);
		_xmlGenerator.flushData();
	    }
	
	if (objectRoot.getParent() != null)
	    objectRoot.getDataStore().deleteObject(objectRoot.getParent(), objectRoot);	      
    }
    
}






