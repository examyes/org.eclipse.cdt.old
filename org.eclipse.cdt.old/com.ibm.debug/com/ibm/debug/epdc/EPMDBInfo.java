package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EPMDBInfo.java, java-epdc, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:23:11)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

class EPMDBInfo extends EPDC_Base {

   EPMDBInfo(byte[] inBuffer, int offset) throws IOException {
      super(inBuffer, offset);
      _PMLockMessage = readInt();
      _PMHandle = readInt();
      _PMHandleType = readInt();
   }

   public int PMLockMessage() {
      return _PMLockMessage;
   }

   public int PMHandle() {
      return _PMHandle;
   }

   public int PMHandleType() {
      return _PMHandleType;
   }

   /** NOT YET IMPLEMENTED */
   protected int toDataStreams(DataOutputStream FixedData, DataOutputStream VarData,
         int baseOffset) throws IOException, BadEPDCCommandException {

      return 0;
   }

   protected void output(DataOutputStream dataOutputStream)
         throws IOException
   {
      dataOutputStream.writeInt(_PMLockMessage);
      dataOutputStream.writeInt(_PMHandle);
      dataOutputStream.writeInt(_PMHandleType);
   }

   /** Return the length of the fixed component. */
   protected int fixedLen() {
      return _fixed_length;
   }

   /** Return the length of the variable component.  */
   protected int varLen() {
      return 0;
   }

   private int _PMLockMessage;
   private int _PMHandle;
   private int _PMHandleType;

   private static final int _fixed_length = 12;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}


