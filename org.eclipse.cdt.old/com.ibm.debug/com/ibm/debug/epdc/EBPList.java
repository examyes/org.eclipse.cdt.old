package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EBPList.java, java-epdc, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:23:02)
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
 * Breakpoint information returned by an EPDC Execute reply packet
 */
public class EBPList extends EPDC_Base {

   EBPList(int BPId, byte BPKind) {
      _StdBPId = BPId;
      _StdBPKind = BPKind;
   }

   EBPList( byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      _StdBPId = dataInputStream.readInt();
      _StdBPKind = dataInputStream.readByte();

      for (int i=0; i<3; i++)
          dataInputStream.readByte();         // reserved
   }

   /** Output class to data streams according to EPDC protocol.
    * @exception IOException if an I/O error occurs
    * @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      writeInt(fixedData, _StdBPId);
      writeChar(fixedData, _StdBPKind);

      for (int i=0; i<3; i++)
         writeChar(fixedData, (byte) 0);         // reserved

      return fixedLen();
   }

   protected int fixedLen() {
      return _fixed_length;
   }

   /**
    * Static function that returns the fixed length.  Used by ERepExecute so that it
    * does not have to call the instance method fixedLen()
    * @see ERepExecute
    */
   protected static int statfixedLen() {
      return _fixed_length;
   }

   protected int varLen() {
      return 0;
   }

   public int getBreakid() {
      return _StdBPId;
   }

   // data fields
   private int _StdBPId;         // breakpoint id
   private byte _StdBPKind;       // breakpoint kind

   private static final int _fixed_length = 8;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}

