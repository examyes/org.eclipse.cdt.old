package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqContextQualGet.java, java-epdc, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:25:22)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class EReqContextQualGet extends EPDC_Request
{
  // Debug Engine Functions and Data

  EReqContextQualGet(byte[] inBuffer) throws IOException {
    super(inBuffer);

    // Read in EStdView
    _context = new EStdView(inBuffer, getOffset());
  }

  public EStdView context() {
    return _context;
  }

  private EStdView _context;

   // Debug Front End Functions and Data

   public EReqContextQualGet(EStdView context)
   {
     super(EPDC.Remote_ContextQualGet);
     _context = context;
   }

   protected int fixedLen() {
      return totalBytes(_context) + super.fixedLen();
   }

  public void output(DataOutputStream dataOutputStream)
    throws IOException
  {
      super.output(dataOutputStream);
      _context.output(dataOutputStream);
  }
}

