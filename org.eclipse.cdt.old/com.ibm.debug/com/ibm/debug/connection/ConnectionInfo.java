package com.ibm.debug.connection;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/connection/ConnectionInfo.java, java-connection, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:29:44)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public abstract class ConnectionInfo
{
  ConnectionInfo(String host, String conduit)
  {
    _host = host;
    _conduit = conduit;
    _connection = null;
  }

  /**
   * @param connectionMode Pass Connection.AS_CLIENT to get a client
   * Connection object or Connection.AS_SERVER to get a server
   * Connection object.
   */

  public Connection getNewConnection(int connectionMode) throws java.io.IOException
  {
    return getNewConnection(connectionMode, false);
  }

  /**
   * @param connectionMode Pass Connection.AS_CLIENT to get a client
   * @param noWait Indicates that if connection fails at first do not wait
   *               and try again.
   * Connection object or Connection.AS_SERVER to get a server
   * Connection object.
   */

  public abstract Connection getNewConnection(int connectionMode, boolean noWait)

  throws java.io.IOException;

  public void setHost(String host)
  {
    _host = host;
  }

  public void setConduit(String conduit)
  {
    _conduit = conduit;
  }

  public String getHost()
  {
    return _host;
  }

  public String getConduit()
  {
    return _conduit;
  }

  // Intended to be overridden in a subclass

  public String getDefaultHost()
  {
    return null;
  }

  // Intended to be overridden in a subclass

  public String getDefaultConduit()
  {
    return null;
  }

  public void setConnection(Connection connection)
  {
    _connection = connection;
  }

  public Connection getConnection()
  {
    return _connection;
  }

  public void setClosed(boolean closed)
  {
    _isClosed = closed;
  }

  public boolean isClosed()
  {
    return _isClosed;
  }

  protected String _host;
  protected String _conduit;
  protected Connection _connection;
  protected boolean _isClosed;
}
