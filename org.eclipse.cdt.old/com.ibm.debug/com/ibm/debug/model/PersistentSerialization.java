package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/PersistentSerialization.java, java-model, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:13:30)
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
 * This class allows us to serialize Model objects into a file
 * and reconstruct those objects from the file.
 */

class PersistentSerialization extends Serialization
{
  PersistentSerialization(String fileName, int flags)
  {
    super(flags);
    _fileName = fileName;
  }

  /**
   * Serialize an object graph starting with the object 'object'. The graph
   * can later be deserialized using getGraph.
   */

  synchronized void saveGraph(Object object)
  throws java.io.IOException
  {
    FileOutputStream outputStream = new FileOutputStream(_fileName);

    // Serialize the graph to the stream:

    super.saveGraph(outputStream, object);

    outputStream.close();
  }

  /**
   * Deserialize the object graph.
   */

  synchronized Object getGraph()
  throws java.io.IOException,
         java.lang.ClassNotFoundException
  {
    FileInputStream inputStream = new FileInputStream(_fileName);

    Object result = super.getGraph(inputStream);

    inputStream.close();

    return result;
  }

  String getFileName()
  {
    return _fileName;
  }

  private String _fileName;
}
