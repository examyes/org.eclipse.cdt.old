package com.ibm.debug.connection;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1995, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/connection/TCPIPDebugDaemon.java, java-connection, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:29:49)
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

public class TCPIPDebugDaemon extends DebugDaemon
{
  private String port;

  public TCPIPDebugDaemon(String port)
  {
    super();
    this.port = port;
  }
  public Connection createConnection() throws IOException
  {
    return new SocketConnection("", port, Connection.AS_SERVER, 2, 1000);
  }
  public ConnectionInfo newEngineConnectionInfo(String engineHost,
                                                String enginePort)
  {
    return new SocketConnectionInfo(engineHost, enginePort);
  }
}
