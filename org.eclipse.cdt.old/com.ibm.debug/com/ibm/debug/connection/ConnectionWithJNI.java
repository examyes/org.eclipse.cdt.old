package com.ibm.debug.connection;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/connection/ConnectionWithJNI.java, java-connection, eclipse-dev, 20011128
// Version 1.12.1.2 (last modified 11/28/01 16:29:35)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.util.*;
import java.lang.*;
import java.io.*;
import java.util.*;

/**
 * ConnectionWithJNI provides the abstract for making connections whose
 * communication protocol are implemented with C/C++ language.
 */
public abstract class ConnectionWithJNI extends Connection
{
  private static final int BUFFER_SIZE = 60000; // this is PICL_BUFSIZE
  private static final int OK = 0;
  private static final int FAILED = -1;

  private int connection;
  private int connectionMode;   // server or client

  private int connectionAttempts = 20;
  private int sleepInterval = 500;

  private byte[] inputBuffer = null;
  private ByteArrayInputStream inputByteStream = null;
  private ByteArrayOutputStream outputByteStream = null;

  public native int open(int protocol, String host, String address,
                         int connectionMode);
  public native void close(int connection);
  public native int send(int connection, byte[] buffer, int bufferSize);
  public native int receive(int connection, byte[] buffer, int bufferSize);

  // method for server only
  public native int connect(int connection); // block until client is connected
  public native int disconnect(int connection);

  static {
    ResourceBundle productProperties = ResourceBundle.getBundle("com.ibm.debug.util.Product");
    String prefix = productProperties.getString("filename.prefix");
    String version = productProperties.getString("filename.version");
    String libraryName = prefix + "dtji" + version;

    if (Connection.TRACE.DBG)
      Connection.TRACE.dbg(1, "loading " + libraryName);

    System.loadLibrary(libraryName);
  }

  public ConnectionWithJNI(int protocol, int connectionMode)
       throws IOException
  {
    if (Connection.TRACE.DBG)
      Connection.TRACE.dbg(1, "open(" + toString() + ")");

    openConnection(protocol, "", "", connectionMode);
    initIO();
  }

  public ConnectionWithJNI(int protocol, String host, int connectionMode)
       throws IOException
  {
    if (Connection.TRACE.DBG)
      Connection.TRACE.dbg(1, "open(" + toString() + ", " + host + ")");

    openConnection(protocol, host, "", connectionMode);
    initIO();
  }

  public ConnectionWithJNI(int protocol, String host, int connectionMode, int connectionAttempts, int sleepInterval)
       throws IOException
  {
    if (Connection.TRACE.DBG)
      Connection.TRACE.dbg(1, "open(" + toString() + ", " + host + "," + connectionAttempts + "," + sleepInterval + ")");

    this.connectionAttempts = connectionAttempts;
    this.sleepInterval = sleepInterval;
    openConnection(protocol, host, "", connectionMode);
    initIO();
  }

  public ConnectionWithJNI(int protocol, String host, String address, int connectionMode)
       throws IOException
  {
    if (Connection.TRACE.DBG)
      Connection.TRACE.dbg(1, "open(" + toString() + ", " + host + ", " +
                           address + ")");

    openConnection(protocol, host, address, connectionMode);
    initIO();
  }

  public ConnectionWithJNI(int protocol, String host, String address, int connectionMode, int connectionAttempts, int sleepInterval)
       throws IOException
  {
    if (Connection.TRACE.DBG)
      Connection.TRACE.dbg(1, "open(" + toString() + ", " + host + ", " + address + "," + connectionAttempts + "," + sleepInterval + ")");

    this.connectionAttempts = connectionAttempts;
    this.sleepInterval = sleepInterval;
    openConnection(protocol, host, address, connectionMode);
    initIO();
  }

  private void openConnection(int protocol, String host, String address,
                              int connectionMode)
    throws IOException
  {
    this.connectionMode = connectionMode;
    int attempts = connectionAttempts;

    while (attempts > 0) {
      connection = open(protocol, host, address, connectionMode);

      if (connection != FAILED)
        return;

      --attempts;

      try {
        Thread.sleep(sleepInterval);
      }
      catch (InterruptedException e) {
      }
    }

    throw new IOException("open failed");
  }

  private void initIO()
  {
    inputBuffer = new byte[BUFFER_SIZE];
    outputByteStream = new ByteArrayOutputStream(BUFFER_SIZE);
    setOutputStream(outputByteStream);
  }

  public void connectToClient() throws IOException
  {
    if (Assertion.ON)
      Assertion.check(connectionMode == AS_SERVER);

    if (connect(connection) != OK)
      throw new IOException("connect failed");
  }

  public void disconnectFromClient() throws IOException
  {
    if (Assertion.ON)
      Assertion.check(connectionMode == AS_SERVER);

    if (disconnect(connection) != OK)
      throw new IOException("disconnect failed");
  }

  public void close() throws IOException
  {
    outputByteStream = null;
    close(connection);
  }

  /**
   * Send the data after writing of the output stream is completed
   */
  public void endWrite() throws IOException
  {
    if (Assertion.ON)
      Assertion.check(connection != FAILED);

    if (Connection.TRACE.DBG)
      Connection.TRACE.dbg(3, "JNI: calling native send()");

    int size = outputByteStream.size();
    int byteSent = send(connection, outputByteStream.toByteArray(), size);

    if (byteSent != size)
      throw new IOException("Send failed");

    outputByteStream.reset();
  }

  /**
   * Receive the data before the reading of the input stream
   */
  public int beginRead() throws IOException
  {
    if (Assertion.ON)
      Assertion.check(connection != FAILED);

    if (Connection.TRACE.DBG)
      Connection.TRACE.dbg(3, "JNI: calling native receive()");

    int byteReceived = receive(connection, inputBuffer, BUFFER_SIZE);

    if (byteReceived <= 0)
      throw new IOException("Receive failed");

    setInputStream(new ByteArrayInputStream(inputBuffer, 0, byteReceived));
    return 0;
  }

  protected void finalize() {
    close(connection);
  }

  public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
