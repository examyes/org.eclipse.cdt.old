package com.ibm.debug.connection;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/connection/TCPIPConnectionInfo.java, java-connection, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:29:45)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.net.InetAddress;

/**
 * NOTE: Use SocketConnectionInfo for native Java sockets.
 */

public class TCPIPConnectionInfo extends ConnectionInfo
{
  /**
   * @param ipAddress If this argument is null, the getNewConnection method
   * will use the local host by default.
   * @param port If this argument is null, the getNewConnection method
   * will use port 8000 by default.
   */

  public TCPIPConnectionInfo(String ipAddress, String port)
  {
    super(ipAddress, port);
  }

  /**
   * If the host contained in this TCPIPConnectionInfo object is null,
   * the getNewConnection method will use the local host by default.
   * If the port # contained in this TCPIPConnectionInfo object is null,
   * the getNewConnection method will use a default of 8000.
   * @param connectionMode Pass Connection.AS_CLIENT to get a client
   * Connection object or Connection.AS_SERVER to get a server
   * Connection object.
   */

  public Connection getNewConnection(int connectionMode, boolean noWait)
  throws java.io.IOException
  {
     return new TCPIPConnection((_host == null) ? getDefaultHost() : _host,
                                (_conduit == null) ? getDefaultConduit() : _conduit,
                                connectionMode);
  }


  /**
   * The default host is "localhost".  Using "localhost" instead of the actual
   * host name avoids a network request to the name server.   This allows the
   * connection to work when there is no network
   */

  public String getDefaultHost()
  {
      return "localhost";
  }

  public String getDefaultConduit()
  {
    return _defaultPort;
  }

  private static final String _defaultPort = "8000";
}
