package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/Representation.java, java-model, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:13:19)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.epdc.*;
import java.io.*;

/**
 * Class representing format of different types. This class will have the
 * name of the type (of the format) as well as an index into the array
 * of possible formats for a given type.
 */
public class Representation extends DebugModelObject
{
  Representation(EStdString name, short index)
  {
    if (Model.TRACE.EVT && Model.traceInfo())
      Model.TRACE.evt(4, "Creating Representation(name:" + name.string() + ", index:" + index + ")");

    _name = name;
    _index = index;
  }

  /**
   * Return the index of a given representation
   */
  short index()
  {
    return _index;
  }

  /**
   * Return the name of a given representation
   */
  public String name()
  {
    return _name.string();
  }

  /**
   * Print the representation object information
   */
  public void print(PrintWriter printWriter)
  {
    if (Model.includePrintMethods)
    {
        printWriter.println("  Rep " + _index + ":  " + _name.string());
    }
  }

  private EStdString _name;
  private short _index;
}
