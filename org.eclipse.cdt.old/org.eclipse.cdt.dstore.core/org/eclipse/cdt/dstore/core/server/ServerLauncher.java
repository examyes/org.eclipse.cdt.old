package com.ibm.dstore.core.server;

/*
 * Copyright (c) 2001 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import com.ibm.dstore.core.server.*;

import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;

public class ServerLauncher extends Thread
{
  public class ConnectionListener extends Thread
  {
    private Socket         _socket;
    private PrintWriter    _writer;
    private BufferedReader _reader;
    private Process        _serverProcess;
    private String         _port;
    private boolean        _done;
    private BufferedReader _outReader;
    private BufferedReader _errReader;

    public ConnectionListener(Socket socket)
        {
          _socket = socket;
          try
          {
            _writer  = new PrintWriter(_socket.getOutputStream());
            _reader  = new BufferedReader(new InputStreamReader(_socket.getInputStream()));          
          }
          catch (java.io.IOException e)
          {
            System.out.println("ServerLauncher:" + e);
          }
        }

    public void finalize() throws Throwable
        {
          if (_serverProcess != null)
          {
            _serverProcess.destroy();            
          }
          super.finalize();
        }

    public void run()
        {
          _done = true;
          if (listen())
	      {
		  if (_serverProcess != null)
		      {
			  _done = false;
			  
			  try
			      {
				  String line = null;
				  
				  while ((_outReader != null) && ((line = _outReader.readLine()) != null))
				      {
					  if (line.equals(ServerReturnCodes.RC_FINISHED))
					      {
						  break;
					      }
					  else
					      {
						  System.out.println(line);
					      }
				      }
				  
				  if (_outReader != null)
				      {
					  _outReader.close();
				      }
				  if (_errReader != null)
				      {
					  _errReader.close();
				      }
				  
				  _serverProcess.waitFor();
			      }
			  catch(Exception e)
			      {              
				  System.out.println("ServerLauncher:" + e);
			      } 
		      }
		  
		  System.out.println("finished on port " + _port);
		  _outReader = null;
		  _errReader = null;
		  _serverProcess = null;
		  _done = true;
	      }
	  else
	      {
		  _done = true;
	      }
	}

    public boolean isDone()
        {
          return _done;
        }

    public String getServerPort()
        {
          return _port;
        }

    public boolean listen()
        {
	    boolean connected = false;
	    
	    String user = null;
	    String password = null;
	    
	    _port = null;
	    try
		{
		    user = _reader.readLine();
		    password = _reader.readLine();
		    _port = _reader.readLine();
		}
	    catch (IOException e)
		{

		}

	    if (isPortAvailable(_port))
		{
		    // start new server
		    try
			{	
			    String launchStatus = null;
			    String ticket = new String("" + System.currentTimeMillis());

			    String theOS = System.getProperty("os.name");			    
			    if (theOS.toLowerCase().startsWith("linux"))
				{
				    String authStr = _path + File.separator + "com.ibm.dstore.core" + 
					File.separator + "auth.pl ";
				    String authString = authStr + 
					user + " " + password + " " + _path + " " + 
					_port + " " + ticket;
				    String[] authArray = {"sh", "-c", authString};
				    
				    // test password
				    _serverProcess = Runtime.getRuntime().exec(authArray); 
				    _outReader  = new BufferedReader(new InputStreamReader(_serverProcess.getInputStream()));
				    _errReader  = new BufferedReader(new InputStreamReader(_serverProcess.getErrorStream()));
				    
				    launchStatus = _outReader.readLine();				    
				}
			    else
				{
				    // launch new server
				    String[] cmdArray = {"java", 
							 "-DA_PLUGIN_PATH=" + _path,
							 "com.ibm.dstore.core.server.Server", 
							 _port,
							 ticket
				    };
				
				    _serverProcess = Runtime.getRuntime().exec(cmdArray); 
				    _outReader  = new BufferedReader(new InputStreamReader(_serverProcess.getInputStream()));
				    _errReader  = new BufferedReader(new InputStreamReader(_serverProcess.getErrorStream()));
				    
				    launchStatus = "success";
				}
			    
			    if ((launchStatus == null) || !launchStatus.equals("success"))
				{
				    _writer.println("Authentification Failed");
				}
			    else
				{				    
				    String status = _errReader.readLine();
				    _port   = _errReader.readLine();

				    if ((status != null) && status.equals(ServerReturnCodes.RC_SUCCESS))
					{
					    _outReader.readLine();
					    _writer.println("connected");
					    _writer.println(_port);
					    _writer.println(ticket);

					    System.out.println("launched new server on " + _port);
					    connected = true;
					}
				    else
					{
					    if (status == null)
						{
						    status = new String("unknown problem connecting to server");
						}

					    _writer.println(status);
					    
					    _serverProcess.destroy();
					    _serverProcess = null;
					    _outReader.close();
					    _outReader = null;
						    
					    _errReader.close();
					    _errReader = null;
					}
				}
			}
		    catch (IOException e)
			{
			    _writer.println("server failure: " + e);
			}
		}
	    
	  _writer.flush();        
	  
	  // close socket
	  try
	      {
		  _socket.close();
	      }
	  catch (IOException e)
	      {
		  System.out.println("ServerLauncher:" + e);
	      }

	  return connected;
        }      
  }

  private ServerSocket          _serverSocket;
  private String                _path;
  private ArrayList             _connections;

  public static void main(String args[])
      {
	  ServerLauncher theServer = new ServerLauncher();
	  theServer.start();
      }
    
    public ServerLauncher()
    {
	String pluginPath = System.getProperty("A_PLUGIN_PATH");
	if (pluginPath == null)
	    {
		System.out.println("A_PLUGIN_PATH is not defined");
		System.exit(-1);
	    }

	_path = pluginPath.trim();
	
        _connections = new ArrayList();
        init("1234");
      }
  
  public void init(String portStr)
      {
      int port = Integer.parseInt(portStr);

      // create server socket from port
      try
	  {
	      _serverSocket = new ServerSocket(port);
	      System.out.println("Server running on: " + InetAddress.getLocalHost().getHostName());
	  }
      catch (UnknownHostException e)
	  {
	      System.err.println("Networking problem, can't resolve local host");
	      e.printStackTrace();
	      System.exit(-1);
	  }
      catch (IOException e)
	  {
	      System.err.println("Failure to create ServerSocket");
	      e.printStackTrace();
	      System.exit(-1);
	  }
      }
    
    public ConnectionListener getListenerForPort(String port)
    {
        for (int i = 0; i < _connections.size(); i++)
	    {
          ConnectionListener listener = (ConnectionListener)_connections.get(i);
	  if (listener.getServerPort().equals(port))
	      {
		  return listener;
	      }
	    }
	
        return null;
    }
        
  public boolean isPortAvailable(String port)
      {
        for (int i = 0; i < _connections.size(); i++)
        {
          ConnectionListener listener = (ConnectionListener)_connections.get(i);
	  if (listener.isDone())
	  {
	    // remove listener  
          }
          else if (listener.getServerPort().equals(port))
          {
            return false;
          }
        } 

        return true;
      }

  public void run()
    {
      while (true)
      {
        try
        {
          Socket newSocket = _serverSocket.accept();
	  
          ConnectionListener listener = new ConnectionListener(newSocket);          
          listener.start();
          _connections.add(listener);
        }
        catch (IOException ioe)
        {
          System.err.println("Server: error initializing socket: " + ioe);
          System.exit(-1);
        }        
      }
    }
}
