package com.ibm.debug.connection;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/connection/Connection.java, java-connection, eclipse-dev, 20011128
// Version 1.24.1.2 (last modified 11/28/01 16:29:32)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.util.*;
import java.io.*;
import java.net.*;

public abstract class Connection
{
  // remote communication types
  protected static final int TCPIP = 1;
  protected static final int NAMEDPIPE = 2;
  protected static final int APPC = 3;
  protected static final int JAVASOCKET = 4;

  // connection types matched with the C++ connection classes
  static public int AS_CLIENT = 0;
  static public int AS_SERVER = 1;

  static public TraceLogger TRACE = new TraceLogger("CONNECTION");

  static public Connection createRemoteConnection(int type,
                                                  String host,
                                                  String tsap,
                                                  int mode)
  {
    Connection connection = null;

    try {
      switch(type) {
      case TCPIP:
        connection = new TCPIPConnection(host, tsap, mode);
        break;
      case NAMEDPIPE:
        connection = new NamedPipeConnection(tsap, mode);
        break;
      case APPC:
        connection = new APPCConnection(host, mode);
        break;
      case JAVASOCKET:
        connection = new SocketConnection(host, tsap, mode);
        break;
      default:
        if (Assertion.ON)
          Assertion.check(false, "Unknown connection type");
        break;
      }
    }
    catch (IOException e) {
    }

    return connection;
  }

  /**
   * factory method for creating remote connection object in client mode
   */
  static public Connection createRemoteClientConnection(int type,
                                                        String host,
                                                        String tsap)
  {
    return createRemoteConnection(type, host, tsap, AS_CLIENT);
  }

  /**
   * factory method for creating remote connection object in server mode
   */
  static public Connection createRemoteServerConnection(int type,
                                                        String tsap)
  {
    return createRemoteConnection(type, "", tsap, AS_SERVER);
  }

  // Subclasses should call this ctor if it's okay for the connection
  // object to dump data to a file as it flows through the connection (e.g.
  // for an EPDC dump).

  protected Connection()
  {
    this(true);
  }

  // Subclasses should call this ctor if it's NOT okay for the connection
  // object to dump data to a file as it flows through the connection. For
  // example, when using a PipedConnection, we don't want both ends of the
  // pipe to dump the data, only one, so one of the PipedConnection objects
  // will suppress the dumping of data by calling this ctor with 'false'.

  protected Connection(boolean dumpingAllowed)
  {
    _dumpingAllowed = dumpingAllowed;
  }

  /**
   * Will start dumping the EPDC packets that are flowing through this
   * Connection object if this object was constructed to allow such
   * dumping.
   */

  public void startDumping()
  {
    if (DUMP_STREAMS && _dumpingAllowed)
    {
       // Try to create the file into which data will be dumped:

       String dumpFileName = System.getProperty("EPDCDUMP");

       if (dumpFileName != null)
	  try
	  {
	    dumpFileName += ++_dumpFileNumber + ".hex";
	    _dumpStream = new FileOutputStream(dumpFileName);
	  }
	  catch (java.io.IOException excp)
	  {
	  }
       else
       {
	 String viewEPDC = System.getProperty("EPDCVIEW");

	 if (viewEPDC != null)
	    try
	    {
              if (_serverSocket == null) // Do this only once:
              {
                 _wait = viewEPDC.equals("wait");

                 if (!_wait)
                    try // If EPDCVIEW property is a number, use it as the port:
                    {
                      _localPort = Integer.parseInt(viewEPDC);
                      _wait = true;
                    }
                    catch(NumberFormatException excp)
                    {
                    }

	         _serverSocket = new ServerSocket(_localPort);

	         _localPort = _serverSocket.getLocalPort();
              }

              if (!_wait)
              {
	         System.out.println("Launching EPDC Viewer");
                 Runtime.getRuntime().exec("java com.ibm.debug.epdc.viewer.EPDCViewer -port=" + _localPort);
              }

	      System.out.println("Waiting for connection from EPDC Viewer at port " + _localPort);

	      Socket socket = _serverSocket.accept();

	      System.out.println("EPDC Viewer connected!");

	      _dumpStream = socket.getOutputStream();
	    }
	    catch (java.io.IOException excp)
	    {
	      System.out.println("Could not launch or wait for EPDC Viewer!");
	    }
       }

       if (_dumpStream != null)
       {
          setOutputStream(_connectionDumperOutputStream = new ConnectionDumperOutputStream(_outputStream, _dumpStream));
          setInputStream(_connectionDumperInputStream = new ConnectionDumperInputStream(_inputStream, _dumpStream));
       }
    }
  }

  public int communicationType() { return 0; }

  /**
   * Get the output stream used
   * @deprecated
   */
  final public OutputStream getOutputStream()
  {
    return _outputStream;
  }

  /**
   * Get the buffered output stream used
   */
  final public ByteArrayOutputStream getOutputStreamBuffer()
  {
    return _outputStreamBuffer;
  }

  final public InputStream getInputStream()
  {
    return _inputStream;
  }

  /** Set the output stream that clients should use when sending data
   *  via this connection. This method should generally only be called from a
   *  subclass.
   */

  protected void setOutputStream(OutputStream outputStream)
  {
    try
    {
      flush();
    }
    catch(IOException ex)
    {}
    _outputStream = outputStream;
  }

  /** Set the input stream that clients should use when reading data
   *  from this connection. This method should generally only be called from a
   *  subclass.
   */

  protected void setInputStream(InputStream inputStream)
  {
    _inputStream = inputStream;
  }

  public final int beginPacketRead()
  throws IOException
  {
    // Let the subclass do any reading of the stream that it has to do
    // but temporarily suspend the dumping of data so that it doesn't end up in the
    // dump file:

    if (DUMP_STREAMS && _connectionDumperInputStream != null)
       _connectionDumperInputStream.suspendDumping();

    int result = beginRead();

    if (DUMP_STREAMS && _connectionDumperInputStream != null)
       _connectionDumperInputStream.resumeDumping();

    return result;
  }

  /**
   * Called before reading of the input stream to allow the appropriate set up
   * or reading of any packet prefix.
   */
  public int beginRead()
  throws IOException
  {
    return 0;
  }

  public final void endPacketRead(int packetType)
  throws IOException
  {
    // Let the subclass do any reading of the stream that it has to do
    // but temporarily suspend the dumping of data so that it doesn't end up in the
    // dump file:

    if (DUMP_STREAMS && _connectionDumperInputStream != null)
       _connectionDumperInputStream.suspendDumping();

    endRead();

    if (DUMP_STREAMS && _connectionDumperInputStream != null)
    {
       _connectionDumperInputStream.resumeDumping();
       _connectionDumperInputStream.dumpBuffer(packetType);
    }
  }

  /**
   * Called after the reading of the input stream to allow the appropriate
   * clean up.
   */
  public void endRead()
  throws IOException
  {
  }

  public final void beginPacketWrite(int packetSize)
  throws IOException
  {
    // Let the subclass do any writing of the stream that it has to do
    // but temporarily suspend the dumping of data so that it doesn't end up in the
    // dump file:

    if (DUMP_STREAMS && _connectionDumperOutputStream != null)
    {
       flush();         // force the data into the output stream so that
                        // the EPDC dumper can also write it out
       _connectionDumperOutputStream.suspendDumping();
    }

    beginWrite(packetSize);

    if (DUMP_STREAMS && _connectionDumperOutputStream != null)
    {
       flush();         // force the data into the output stream so the data
                        // is written BEFORE the EPDC dumper is restarted
       _connectionDumperOutputStream.resumeDumping();
    }
  }

  /**
   * Called before writing of the output stream to allow the appropriate set up
   * or writing of any packet prefix
   */
  public void beginWrite(int packetSize)
  throws IOException
  {
  }

  public final void endPacketWrite(int packetType)
  throws IOException
  {
    // Let the subclass do any writing of the stream that it has to do
    // but temporarily suspend the dumping of data so that it doesn't end up in the
    // dump file:

    if (DUMP_STREAMS && _connectionDumperOutputStream != null)
    {
       flush();         // force the data into the output stream so that
                        // the EPDC dumper can also write it out
       _connectionDumperOutputStream.suspendDumping();
    }

    endWrite();

    if (DUMP_STREAMS && _connectionDumperOutputStream != null)
    {
       flush();         // force the data into the output stream so the data
                        // is written BEFORE the EPDC dumper is restarted
       _connectionDumperOutputStream.resumeDumping();
       _connectionDumperOutputStream.dumpBuffer(packetType);
    }
  }

  /**
   * Called after writing of the output stream to allow the appropriate
   * clean up.
   */
  public void endWrite()
  throws IOException
  {
    flush();
  }

  public void close()
  throws IOException
  {
    flush();
    if (_connectionDumperOutputStream != null)
       _connectionDumperOutputStream.close();

    if (_connectionDumperInputStream != null)
       _connectionDumperInputStream.close();
  }

  /**
   * Server only, wait for a client connection
   */
  public void connectToClient()
  throws IOException
  {
  }

  /**
   * Server only, disconnect from the client
   */
  public void disconnectFromClient()
  throws IOException
  {
  }

   /**
    *  Flush the output stream buffer to the actual output stream.
    *  Data in the output stream buffer is not sent until the stream is flushed.
    */
   public void flush()
   throws IOException
   {
     // make sure we have an output stream and data to write
     if (_outputStream != null && _outputStreamBuffer.size() > 0)
       _outputStream.write(_outputStreamBuffer.toByteArray());
     _outputStreamBuffer.reset();
   }

  private   OutputStream _outputStream;
  protected InputStream _inputStream;
  protected ByteArrayOutputStream _outputStreamBuffer = new ByteArrayOutputStream();

  private ConnectionDumperOutputStream _connectionDumperOutputStream;
  private ConnectionDumperInputStream _connectionDumperInputStream;
  private OutputStream _dumpStream;
  private static int _dumpFileNumber;
  private boolean _dumpingAllowed = false;

  // Set this to 'false' to disable stream dumping altogether i.e. to
  // optimize-away the code that does the dumping:

  private static final boolean DUMP_STREAMS = true;

  private static int _localPort; // Port EVIEW should connect to.

  private static boolean _wait = false; // Wait for EVIEW to connect.

  private static ServerSocket _serverSocket;
}
