package com.ibm.dstore.core.util;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.model.*;
import com.ibm.dstore.core.util.*;

import java.net.*; 
import java.io.*;
import java.lang.*;
import java.util.*;

public class Sender implements ISender
{
  private Socket            _socket;
  private PrintStream       _out;
  private XMLgenerator      _xmlGenerator;

  public Sender(Socket socket)
    {
      _socket = socket;
      _xmlGenerator = new XMLgenerator();

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
        _out = new PrintStream(_socket.getOutputStream());
	_xmlGenerator.setWriter(_out);	  
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
	synchronized(_out)
        {
	      _out.println(document);
              _out.flush();
        }
      }

  public void sendFile(DataElement objectRoot, File file, int depth)
      {
	synchronized(_out)
        {
          _xmlGenerator.empty();	  
          _xmlGenerator.generate(objectRoot, depth, file);
          _xmlGenerator.flush();
        }
      }

  public void sendFile(DataElement objectRoot, byte[] bytes, int size)
      {
	synchronized(_out)
        {
          _xmlGenerator.empty();	  
          _xmlGenerator.generate(objectRoot, bytes, size);
          _xmlGenerator.flush();
        }
      }

  public void sendAppendFile(DataElement objectRoot, byte[] bytes, int size)
      {
	synchronized(_out)
        {
          _xmlGenerator.empty();	  
          _xmlGenerator.generate(objectRoot, bytes, size, true);
          _xmlGenerator.flush();
        }
      }

  public void sendDocument(DataElement objectRoot, int depth)
      {
	  synchronized(_out)
	      {
		  _xmlGenerator.empty();	  
		  _xmlGenerator.generate(objectRoot, depth);
		  _xmlGenerator.flush();
	      }

      }


}






