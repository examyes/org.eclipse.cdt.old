package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqStorageFree.java, java-epdc, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:25:29)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class EReqStorageFree extends EPDC_Request
{
  public EReqStorageFree(short id)
  {
    super(EPDC.Remote_StorageFree);

    _id = id;
  }

  protected int fixedLen()
  {
    return _fixed_length + super.fixedLen();
  }

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqStorageFree (byte[] inBuffer) throws IOException {
      super(inBuffer);

      _id = readShort();
   }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
     super.output(dataOutputStream);

     dataOutputStream.writeShort(_id);
  }

  private short _id;
  public short  getID()
  { return _id;  }

  private final static int _fixed_length = 2;
}
