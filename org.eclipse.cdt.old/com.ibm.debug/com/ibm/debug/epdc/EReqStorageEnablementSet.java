package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqStorageEnablementSet.java, java-epdc, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:25:59)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class EReqStorageEnablementSet extends EPDC_Request
{
  public EReqStorageEnablementSet(short storageID,
                                  boolean enableStorage,
                                  boolean enableExpression)
  {
    super(EPDC.Remote_StorageEnablementSet);

    _storageID = storageID;

    if (enableStorage)
       _enablementFlags = EPDC.StorageEnabled;

    if (enableExpression)
       _enablementFlags |= EPDC.StorageExprEnabled;
  }

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqStorageEnablementSet (byte[] inBuffer) throws IOException {
      super(inBuffer);

      _storageID = readShort();
      _enablementFlags = readInt();
   }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    dataOutputStream.writeShort(_storageID);
    dataOutputStream.writeInt(_enablementFlags);
  }

  protected int fixedLen()
  {
     return _fixed_length + super.fixedLen();
  }

  private short _storageID;
  public  short getID() { return _storageID; }
  private int _enablementFlags;
  public  int getEnablementFlags() { return _enablementFlags; }

  private static final int _fixed_length = 6;
}
