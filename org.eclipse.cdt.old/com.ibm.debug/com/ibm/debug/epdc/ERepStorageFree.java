package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepStorageFree.java, java-epdc, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:26:20)
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
 * Monitor storageFree reply -- only contains reply header
 */
public class ERepStorageFree extends EPDC_Reply {

   public ERepStorageFree() {
      super(EPDC.Remote_StorageFree);
   }

  //Decode EPDC reply
  ERepStorageFree(byte[] packetBuffer, DataInputStream dataInputStream)
  throws IOException
  {
    super (packetBuffer, dataInputStream);  // epdc reply header
  }


   protected int fixedLen() {
      return super.fixedLen() + _fixed_length;
   }

   protected int varLen() {
      return super.varLen();
   }

   /** Output class to data streams according to EPDC protocol.
     * @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {
      int ret = super.toDataStreams(fixedData, varData, baseOffset);
      return ret;
   }

   private static final int _fixed_length = 0;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 2001 - All Rights Reserved";

}

