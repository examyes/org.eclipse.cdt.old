package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqVerifyViews.java, java-epdc, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:24:34)
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
 * Verify Views request
 */
public class EReqVerifyViews extends EPDC_Request {

   public EReqVerifyViews(short partID, int fileIndex)
   {
     super(EPDC.Remote_ViewsVerify);
     _partID = partID;
     _srcFileIndex = fileIndex;
   }

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqVerifyViews(byte[] inBuffer) throws IOException {
      super(inBuffer);
      _partID = readShort();
      _srcFileIndex = readInt();
   }

  public void output(DataOutputStream dataOutputStream)
    throws IOException
  {
    super.output(dataOutputStream);

    dataOutputStream.writeShort(_partID);
    dataOutputStream.writeInt(_srcFileIndex);
  }

   /**
    * Get part ID to verify
    */
   public short partID() {
      return _partID;
   }

   /**
    * Get source file index
    */
   public int srcFileIndex() {
      return _srcFileIndex;
   }

   protected int fixedLen() {
      return _fixed_length + super.fixedLen();
   }

   // Data fields
   private short _partID;
   private int _srcFileIndex;
   private static final int  _fixed_length = 6;
}
