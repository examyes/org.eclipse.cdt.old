package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/TransientSerialization.java, java-model, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:13:28)
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
 * This class allows us to serialize Model objects into a buffer (byte
 * array) and reconstruct those objects from the buffer.
 */

class TransientSerialization extends Serialization
{
  TransientSerialization(int flags)
  {
    super(flags);
  }

  /**
   * Serialize an object graph starting with the object 'object'. The graph
   * can later be deserialized using getGraph.
   */

  synchronized void saveGraph(Object object)
  throws java.io.IOException
  {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    // Serialize the graph to the stream:

    super.saveGraph(outputStream, object);

    // Save the byte array containing the serialized graph so that it can
    // be used to deserialize in getGraph:

    _buffer = outputStream.toByteArray();
  }

  /**
   * Deserialize the object graph that was saved using saveGraph. If saveGraph
   * has not yet been called, returns null.
   */

  synchronized Object getGraph()
  throws java.io.IOException,
         java.lang.ClassNotFoundException
  {
    if (_buffer == null)
       return null;

    ByteArrayInputStream inputStream = new ByteArrayInputStream(_buffer);

    return super.getGraph(inputStream);
  }

  private byte[] _buffer;
}
