package com.ibm.debug.model;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/model/ModelObjectOutputStream.java, java-model, eclipse-dev, 20011128
// Version 1.4.1.2 (last modified 11/28/01 16:13:27)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

class ModelObjectOutputStream extends java.io.ObjectOutputStream
{
  ModelObjectOutputStream(OutputStream stream, int saveFlags)
  throws java.io.IOException
  {
    super(stream);

    // enableReplaceObject(true);

    writeInt(_saveFlags = saveFlags);
  }

/*
  See note in DebugEngine.java re: serialization defect # 4065313 for
  why this is commented out. Apparently the defect has been fixed in
  JDK 1.2 and the "proper" sol'n, below, could be used in that release.

  protected Object replaceObject(Object obj)
  throws IOException
  {
    if (obj instanceof Type)
       if (((Type)obj).needsToBeSaved())
          return obj;
       else
          return null;
    else
       return obj;
  }
*/

  int getSaveFlags()
  {
    return _saveFlags;
  }

  int _saveFlags;
}
