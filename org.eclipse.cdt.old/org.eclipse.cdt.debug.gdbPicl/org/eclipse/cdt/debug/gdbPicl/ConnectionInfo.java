package org.eclipse.cdt.debug.gdbPicl;

/*
 * Copyright (c) 1998, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
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
