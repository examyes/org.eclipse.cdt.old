package com.ibm.dstore.core.util;

/*
 * Copyright (C) 2000, 2001 International Business Machines Corporation and others. All Rights Reserved.  
 */

import com.ibm.dstore.core.util.*;
import com.ibm.dstore.core.model.*;

import java.lang.*;
import java.net.*;
import java.io.*;

public abstract class Receiver extends Thread
{
  private Socket              _socket;

  protected DataStore         _dataStore;

  private XMLparser           _xmlParser;
  private BufferedInputStream _in;

  protected boolean           _canExit;

  public Receiver(Socket socket, DataStore dataStore)
      {
        _socket = socket;
        _dataStore = dataStore;
        _canExit = false;
        _xmlParser = new XMLparser(dataStore);          

        try
        {
          _in  = new BufferedInputStream(socket.getInputStream());
        }
        catch (UnknownHostException uhe)
        {
	    System.out.println("Receiver:" + uhe);
        }
        catch (IOException ioe)
        {
	    System.out.println("Receiver:" + ioe);
        }
      }

  public synchronized void finish()
      {
        _canExit = true;
      }

  public boolean canExit()
      {
        return _canExit;
      }

  public void run()
      {
        try
        {
          while (!_canExit)
          {
            handleInput();
          }
        }
        catch (Exception e)
        {
	    System.out.println("Receiver:run()" + e);
	    _canExit = true;
	    e.printStackTrace();
	    handleError(e);
        }
      }

  public synchronized void handleInput()
      {
        try
        {
	    DataElement rootObject = _xmlParser.parseDocument(_in);
	    
	    if (rootObject != null)
		{
		    String type = rootObject.getType();
		    if (!type.equals("FILE"))
			{
			    handleDocument(rootObject);
			}
		}
	    else
		{
		   
		    // something really bad happened
		    _canExit = true;
		    handleError(null);
		}
        }        
        catch (IOException ioe)
	    {
		System.out.println("Receiver:handleInput()" + ioe);
		ioe.printStackTrace();
		_canExit = true;
		handleError(ioe);
	    }
	catch (Exception e)
	    {
		System.out.println("Receiver: " + e);
		e.printStackTrace();
		handleError(e);
	    }
      }


    
  public Socket socket()
      {
        return _socket;
      }

  public void handleFile(DataElement fileObject)
      {
        // file name gives us the file name from remote system
        String remotePath = fileObject.getSource();
        StringBuffer contents = fileObject.getBuffer();
        _dataStore.saveFile(remotePath, contents);

      }

  // this is implemented by extended classes
  public abstract void handleDocument(DataElement documentObject);
  
  public abstract void handleError(Exception e);
}























