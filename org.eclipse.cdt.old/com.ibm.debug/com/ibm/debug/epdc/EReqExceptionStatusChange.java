package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqExceptionStatusChange.java, java-epdc, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:24:05)
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
 * Exception status change request
 */
public class EReqExceptionStatusChange extends EPDC_Request {

  public EReqExceptionStatusChange (int[] exceptionStatusFlags)
  {
    super(EPDC.Remote_ExceptionStatusChange);
    _exceptionStatusFlags = exceptionStatusFlags;
  }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    int numExceptions = _exceptionStatusFlags.length;
    dataOutputStream.writeInt(numExceptions);
    for (int i=0; i<numExceptions; i++)
      dataOutputStream.writeInt(_exceptionStatusFlags[i]);
  }

  protected int fixedLen()
  {
    return _fixed_length + super.fixedLen();
  }

  protected int varLen()
  {
    int total = 4 * _exceptionStatusFlags.length;

    return total;
  }

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqExceptionStatusChange(byte[] inBuffer) throws IOException {
      super(inBuffer);

      int numExceptions = readInt();
      _exceptionStatusFlags = new int[numExceptions];

      for (int i=0; i<numExceptions; i++)
         _exceptionStatusFlags[i] = readInt();

   }

   /**
    * Returns exception status flags
    */
   public int[] exceptionStatusFlags() {
      return _exceptionStatusFlags;
   }

   // data fields
   private int[] _exceptionStatusFlags;
   private static final int _fixed_length = 4;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}

