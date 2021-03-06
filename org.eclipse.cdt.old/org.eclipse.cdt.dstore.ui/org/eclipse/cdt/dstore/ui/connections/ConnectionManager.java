package org.eclipse.cdt.dstore.ui.connections;

/*
 * Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */
 
import org.eclipse.cdt.dstore.ui.*;
import org.eclipse.cdt.dstore.ui.ConvertUtility;
import org.eclipse.cdt.dstore.ui.actions.RemoteOperation;
import org.eclipse.cdt.dstore.ui.dialogs.*;
import org.eclipse.cdt.dstore.ui.connections.*;

import org.eclipse.cdt.dstore.core.client.*;
import org.eclipse.cdt.dstore.core.model.*;
import org.eclipse.cdt.dstore.extra.internal.extra.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.resource.*;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;

import java.util.*;
import java.io.*;

public class ConnectionManager
{

  
  private ArrayList              _connections;
  private DataElement            _input;
  private DomainNotifier         _notifier;
  private String                 _connectionsFile;

  public ConnectionManager(DataElement input, DomainNotifier notifier, String connectionsFile)
      {
        _input = input;
        _notifier = notifier;
        _connections = new ArrayList();
        _connectionsFile = _input.getDataStore().getAttribute(DataStoreAttributes.A_PLUGIN_PATH) + connectionsFile;
      }
      
  public DataElement getInput()
  {
  	return _input;	
  }

  public ArrayList getConnections()
      {
        return _connections;
      }

  public Connection findConnectionFor(DataStore dataStore)
  {
    for (int i = 0; i < _connections.size(); i++)
      {
	Connection connection = (Connection)_connections.get(i);
	if (connection.contains(dataStore))
	  {
	    return connection;	    
	  }	
      } 

    return null;   
  }

  public Connection findConnectionFor(DataElement host)
  {
    String name = host.getName();
    for (int i = 0; i < _connections.size(); i++)
    {
      Connection connection = (Connection)_connections.get(i);
      if (connection._name.equals(name))
      {
        return connection;
      }
    }
    
    return null;   
  }

  public void removeConnection(Connection connection)
      {
	if (connection != null)
         _connections.remove(connection);
      }
  
  public ConnectionStatus connect(DataElement host)
      {
        Connection connection = findConnectionFor(host);
        if (connection != null)
        {
          return connection.connect(_notifier);
        }
        else
        {
          return new ConnectionStatus(false);
        }
      }

  public void delete(DataElement host)
      {
        String name = host.getName();
        for (int i = 0; i < _connections.size(); i++)
        {
          Connection connection = (Connection)_connections.get(i);
          if (connection._name.equals(name))
          {
            connection.delete();
            _connections.remove(connection);
            return;
          }
        }        
      }

  public void disconnect(DataElement host)
      {
        String name = host.getName();
        for (int i = 0; i < _connections.size(); i++)
        {
          Connection connection = (Connection)_connections.get(i);
          if (connection._name.equals(name))
          {
            connection.disconnect();
          }
        }        
      }

  public void disconnectAll()
      {
        writeConnections();

        for (int i = 0; i < _connections.size(); i++)
        {
          Connection connection = (Connection)_connections.get(i);
          connection.disconnect();
        }        
      }

    public String getConnectionFile()
    {
		return _connectionsFile;	
    }   

  public void writeConnections()
      {
	  String connectionFile = getConnectionFile();
        File file = new File(connectionFile);
	if (!file.exists())
	  {
	    File dir = new File(file.getParent());
	    dir.mkdirs();
	  }
        
        try
        {
          FileOutputStream fileStream = new FileOutputStream(file);
          StringBuffer buffer = new StringBuffer();
          
          for (int i = 0; i < _connections.size(); i++)
          {
            Connection con = (Connection)_connections.get(i);
            String line = con.toString();          
            
            buffer.append(line + "\r\n");
          }
          
          fileStream.write(buffer.toString().getBytes());            
          fileStream.close();
        }
        catch (IOException e)
        {
          System.out.println(e);
        }        
      }
  
  public void readConnections()
      {        
        String connectionFile = getConnectionFile();
        
        File file = new File(connectionFile);

        try
        {
	  FileInputStream inFile = null;	  
	  try
	    {	      
	      inFile = new FileInputStream(file);    
	    }
	  catch (FileNotFoundException e)
	    {
	      return;	      
	    }

          BufferedReader in = new BufferedReader(new InputStreamReader(inFile));
          
          String connection = null;
          while ((connection = in.readLine()) != null)
          {
            ArrayList args = new ArrayList();
            StringTokenizer tokenizer = new StringTokenizer(connection);

            String name = (String)tokenizer.nextElement();
            while (tokenizer.hasMoreElements())
            {
              String token = (String)tokenizer.nextElement();
              args.add(token);
            }
            
            Connection con = new Connection(name, args, _input);
            _connections.add(con);
          }        
        }
        catch (IOException e)
        {
          System.out.println(e);
        }        
      }
}
