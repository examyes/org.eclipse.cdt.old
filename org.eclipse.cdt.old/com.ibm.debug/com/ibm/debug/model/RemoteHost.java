package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/RemoteHost.java, java-model, eclipse-dev, 20011128
// Version 1.11.1.2 (last modified 11/28/01 16:11:12)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.connection.ConnectionInfo;
import java.net.InetAddress;

/** Use this class when the debug engine and SUI are running on
 *  different machines.
 */

public class RemoteHost extends Host
{
  RemoteHost(Object address)
  {
    super(address);
  }

  public boolean loadEngine(EngineInfo engineInfo,
                            ProductInfo productInfo,
                            ConnectionInfo connectionInfo,
                            EngineArgs engineArgs)
  {
    return false;
  }
}
