package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqStorageUsageCheckSet.java, java-epdc, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:25:57)
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
 * Enable or Disable memory (heap) checking on the backend.
 */
public class EReqStorageUsageCheckSet extends EPDC_Request
{
  public EReqStorageUsageCheckSet(int attribute)
  {
    super(EPDC.Remote_StorageUsageCheckSet);

    _storageUsageCheckAttribute = attribute;
  }

  public EReqStorageUsageCheckSet(byte[] inBuffer)
  throws IOException
  {
    super(inBuffer);
    _storageUsageCheckAttribute = readInt();
  }

  /**
   * Return the heap check attribute (enable/disable)
   */
  public int getStorageUsageCheckAttribute()
  {
    return _storageUsageCheckAttribute;
  }

  protected int fixedLen()
  {
    return _fixed_length + super.fixedLen();
  }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    dataOutputStream.writeInt(_storageUsageCheckAttribute);
  }

  // data fields
  private int _storageUsageCheckAttribute;

  private static final int _fixed_length = 4;

  public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}

