package org.eclipse.cdt.debug.gdbPicl;

/*
 * Copyright (c) 1997, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/** Establish a connection between SUI and the debug engine using a socket.
 */

public class SocketConnection extends Connection
{
  private Socket socket;
  private ServerSocket serverSocket;
  private int connectAttempts = 100;
  private int sleepInterval = 500;
  private int mode = AS_CLIENT;

  public SocketConnection(String host, String portAsString, int mode)
  throws IOException
  {
      this.mode = mode;
      init(host, portAsString);
  }
  public SocketConnection(String host, String portAsString, int mode, int connectAttempts, int sleepInterval)
  throws IOException
  {
      this.connectAttempts = connectAttempts;
      this.sleepInterval = sleepInterval;
      this.mode = mode;
      init(host, portAsString);
  }
  public SocketConnection(InetAddress internetAddress, int portNumber)
  throws IOException
  {
      mode = AS_CLIENT;
      initClient(internetAddress, portNumber);
  }
  public SocketConnection(InetAddress internetAddress, int portNumber, int connectAttempts, int sleepInterval)
  throws IOException
  {
      this.connectAttempts = connectAttempts;
      this.sleepInterval = sleepInterval;
      mode = AS_CLIENT;
      initClient(internetAddress, portNumber);
  }
  public SocketConnection(Socket socket)
  throws IOException
  {
      mode = AS_CLIENT;
      this.socket = socket;
      super.setOutputStream(socket.getOutputStream());
      super.setInputStream(socket.getInputStream());
  }

  /**
   * Makes a clone of itself and then clears the current
   * socket.  If there is no socket it returns null.
   * Used to break off the current socket connection for
   * use elsewhere while this connection can continue to
   * listen for new connections.
   * @returns A clone of this socket connection or null if
   *          there is currently no socket.
   */
  public SocketConnection cloneConnectionAndClear()
  throws IOException
  {
      SocketConnection clone = null;

      if(socket != null)
      {
          clone = new SocketConnection(socket);
          socket = null;
          super.setOutputStream(null);
          super.setInputStream(null);
      }

      return clone;
  }

  /** Packet is prefixed by its length.
   */
  public int beginRead()
  throws IOException
  {
      return (new DataInputStream(_inputStream)).readInt();
  }

  /**
   * Write out the packet size
   */
  public void beginWrite(int packetSize)
  throws IOException
  {
     new DataOutputStream(_outputStreamBuffer).writeInt(packetSize);
  }

  public void close() throws IOException
  {
      super.close();

      if (mode == AS_CLIENT)
      {
         if (socket != null)
            socket.close();
      }
      else
      {
         if (serverSocket != null)
            serverSocket.close();
      }
  }
  public int communicationType() { return TCPIP; }
  public void connectToClient()
  throws IOException
  {
      socket = serverSocket.accept();
      super.setOutputStream(socket.getOutputStream());
      super.setInputStream(socket.getInputStream());
  }
/**
 * Terminate the socket connection.
 * Creation date: (9/14/2000 4:00:34 PM)
 */
public void disconnectFromClient() throws IOException
{
    if (socket != null)
        socket.close();
}
  private void init(String host, String portAsString)
  throws IOException
  {
      int port;
      try
      {
        port = Integer.parseInt(portAsString);
      }
      catch (NumberFormatException e)
      {
        if (Connection.TRACE.ERR)
            Connection.TRACE.err(1, "Socket port not a number: " + portAsString);

        throw new IOException("Socket port not a number:" + portAsString);
      }

      if (mode == AS_CLIENT)
          initClient(InetAddress.getByName(host), port);
      else
          initServer(port);
  }
  private void initClient(InetAddress internetAddress, int portNumber)
  throws IOException
  {
      boolean connected = false;
      int attempts = 0;

      while (!connected)
      {
        try
        {
          socket = new Socket(internetAddress, portNumber);

          connected = true;

          super.setOutputStream(socket.getOutputStream());
          super.setInputStream(socket.getInputStream());
          socket.setTcpNoDelay(true); // Send data immediately (defect 16268)

        }
        catch (ConnectException excp) // Could not connect
        {
          if (++attempts == connectAttempts)
             throw excp;
          else
             try
             {
               Thread.sleep(sleepInterval, 0);
             }
             catch (InterruptedException excp2)
             {
             }
        }
      }

  }
  private void initServer(int portNumber) throws IOException
  {
      boolean connected = false;
      int attempts = 0;

      while (!connected)
      {
        try
        {
          serverSocket = new ServerSocket(portNumber);
          connected = true;
        }
        catch (IOException excp)
        {
          if (++attempts == connectAttempts)
             throw excp;
          else
             try
             {
               Thread.sleep(sleepInterval, 0);
             }
             catch (InterruptedException excp2)
             {
             }
        }
      }

  }
}
