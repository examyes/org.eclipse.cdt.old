package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepBreakpointLocation.java, java-epdc, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:23:15)
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
 * EPDC location breakpoint reply
 */
public class ERepBreakpointLocation extends EPDC_Reply
{
   ERepBreakpointLocation( byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      super( packetBuffer, dataInputStream );
      _dupBkpID = dataInputStream.readInt();
   }

   public ERepBreakpointLocation() {
      super(EPDC.Remote_BreakpointLocation);
      _dupBkpID = 0;
   }

   public void setDuplicateBkpID(int dupBkpID) {
      _dupBkpID = dupBkpID;
   }

   /** Output class to data streams according to EPDC protocol.
     * @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      super.toDataStreams(fixedData, varData, baseOffset);
      writeInt(fixedData, _dupBkpID);

      return fixedLen() + varLen();
   }

   protected int fixedLen() {
      return super.fixedLen() + _fixed_length;
   }

   protected int varLen() {
      return super.varLen();
   }

   // Data fields
   private int _dupBkpID;

   private static final int _fixed_length = 4;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";


}
