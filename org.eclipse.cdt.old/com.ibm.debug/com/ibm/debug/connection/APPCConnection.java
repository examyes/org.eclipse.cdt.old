package com.ibm.debug.connection;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/connection/APPCConnection.java, java-connection, eclipse-dev, 20011128
// Version 1.4.1.2 (last modified 11/28/01 16:29:38)
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
 * APPCConnection provides the communication with APPC protocol
 */
public final class APPCConnection extends ConnectionWithJNI
{
  public APPCConnection(String host, int connectionMode)
       throws IOException
  {
    super(APPC, host, connectionMode);
  }

  public int communicationType() { return APPC; }
  public String toString() { return "APPC"; }
}
