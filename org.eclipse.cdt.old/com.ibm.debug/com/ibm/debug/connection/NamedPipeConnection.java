package com.ibm.debug.connection;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/connection/NamedPipeConnection.java, java-connection, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:29:39)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

/**
 * NamedPipeConnection provides the communication with named pipe
 */
public final class NamedPipeConnection extends ConnectionWithJNI
{
  public NamedPipeConnection(String pipeName, int connectionMode)
       throws IOException
  {
    super(NAMEDPIPE, "", pipeName, connectionMode);
  }

  public NamedPipeConnection(String pipeName, int connectionMode, int connectionAttempts, int sleepInterval)
       throws IOException
  {
    super(NAMEDPIPE, "", pipeName, connectionMode, connectionAttempts, sleepInterval);
  }

  public int communicationType() { return NAMEDPIPE; }
  public String toString() { return "Named Pipe"; }
}
