package com.ibm.debug.connection;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/connection/NamedPipeConnectionInfo.java, java-connection, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:29:47)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public class NamedPipeConnectionInfo extends ConnectionInfo
{
  public NamedPipeConnectionInfo(String pipeName)
  {
    super(null, pipeName);
  }

  /**
   * @param connectionMode Pass Connection.AS_CLIENT to get a client
   * Connection object or Connection.AS_SERVER to get a server
   * Connection object.
   */

  public Connection getNewConnection(int connectionMode, boolean noWait)
  throws java.io.IOException
  {
    return new NamedPipeConnection(_conduit, connectionMode);
  }
}
