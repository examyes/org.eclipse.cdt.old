package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/Serialization.java, java-model, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:13:29)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

abstract class Serialization
{
  Serialization(int flags)
  {
    _flags = flags;
  }

  abstract void saveGraph(Object object) throws java.io.IOException;

  abstract Object getGraph()
  throws java.io.IOException, java.lang.ClassNotFoundException;

  void saveGraph(OutputStream stream, Object object)
  throws java.io.IOException
  {
    // Create a ModelObjectOutputStream and tell it to save the graph:

    ModelObjectOutputStream objectStream =
                            new ModelObjectOutputStream(stream, _flags);

    objectStream.writeObject(object);
  }

  static Object getGraph(InputStream stream)
  throws java.io.IOException,
         java.lang.ClassNotFoundException
  {
    // Create a ModelObjectInputStream and tell it to read the graph:

    ModelObjectInputStream objectStream =
                            new ModelObjectInputStream(stream);

    return objectStream.readObject();
  }

  void setFlags(int flags)
  {
    _flags = flags;
  }

  int getFlags()
  {
    return _flags;
  }

  boolean canBeDeserialized()
  {
    try
    {
      if (getGraph() == null)
         return false;
      else
         return true;
    }
    catch (java.io.IOException excp)
    {
      return false;
    }
    catch (java.lang.ClassNotFoundException excp)
    {
      return false;
    }
  }

  private int _flags;
}
