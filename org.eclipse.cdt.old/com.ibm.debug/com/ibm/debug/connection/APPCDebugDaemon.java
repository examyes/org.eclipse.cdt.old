package com.ibm.debug.connection;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/connection/APPCDebugDaemon.java, java-connection, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:29:51)
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

/**
 * APPCDebugDaemon listens to a named pipe for the request to an SNA host
 */
public class APPCDebugDaemon extends DebugDaemon
{
  private String pipename;

  public APPCDebugDaemon(String pipename)
  {
    super();
    this.pipename = pipename ;
  }
  public Connection createConnection() throws IOException
  {
    return new NamedPipeConnection(pipename, Connection.AS_SERVER);
  }
  public ConnectionInfo newEngineConnectionInfo(String engineHost,
                                                String enginePort)
  {
    return new APPCConnectionInfo(engineHost);
  }
}
