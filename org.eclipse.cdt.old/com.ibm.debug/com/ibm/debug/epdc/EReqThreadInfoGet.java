package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqThreadInfoGet.java, java-epdc, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:26:06)
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
 * ThreadInfoGet Request.
 */
public class EReqThreadInfoGet extends EPDC_Request
{
  public EReqThreadInfoGet(int DU)
  {
    super(EPDC.Remote_ThreadInfoGet);
    _DU = DU;
  }

  /**
   * Constructs a new EReqThreadInfoGet object
   */
  EReqThreadInfoGet(byte[] inBuffer)
  throws IOException
  {
    super (inBuffer);
    _DU = readInt();
  }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
     super.output(dataOutputStream);
     dataOutputStream.writeInt(_DU);
  }

  /**
   * Return the length of the fixed component
   */
  protected int fixedLen()
  {
    return _fixed_length + super.fixedLen();
  }

  /**
   * Return the length of the variable component
   */
  protected int varLen()
  {
    return super.varLen();
  }

  /**
   * Return the thread DU number for this EReqThreadInfoGet
   */
  public int getDU()
  {
    return _DU;
  }

  // Datafields
  private int _DU;                            // Thread DU number

  private static final int _fixed_length = 4;
}
