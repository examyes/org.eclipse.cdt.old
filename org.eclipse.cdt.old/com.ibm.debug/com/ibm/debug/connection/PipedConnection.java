package com.ibm.debug.connection;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/connection/PipedConnection.java, java-connection, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:29:34)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

/** Establish a connection between SUI and the debug engine using a pipe.
 */

public class PipedConnection extends Connection
{
  public PipedConnection()
  throws IOException
  {
    PipedInputStream pipedInputStreamForThisConnectionObject = new PipedInputStream();

    PipedOutputStream pipedOutputStreamForTheOtherConnectionObject = new PipedOutputStream(pipedInputStreamForThisConnectionObject);

    PipedInputStream pipedInputStreamForTheOtherConnectionObject = new PipedInputStream();

    PipedOutputStream pipedOutputStreamForThisConnectionObject = new PipedOutputStream(pipedInputStreamForTheOtherConnectionObject);

    super.setInputStream(pipedInputStreamForThisConnectionObject);
    super.setOutputStream(pipedOutputStreamForThisConnectionObject);

    _theOtherConnectionObject = new PipedConnection(pipedInputStreamForTheOtherConnectionObject,
                                                    pipedOutputStreamForTheOtherConnectionObject,
                                                    this);
  }

  private PipedConnection(PipedInputStream inputStream, PipedOutputStream outputStream, PipedConnection theOtherConnectionObject)
  {
    // In a piped connection we don't want both ends of the pipe dumping
    // the streams so we'll suppress it in this ctor:

    super(false);

    super.setInputStream(inputStream);
    super.setOutputStream(outputStream);

    _theOtherConnectionObject = theOtherConnectionObject;
  }

  public PipedConnection getConnectionForOtherEndOfPipe()
  {
    return _theOtherConnectionObject;
  }

  private PipedConnection _theOtherConnectionObject;
}
