package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepVerifyViews.java, java-epdc, eclipse-dev, 20011128
// Version 1.4.1.2 (last modified 11/28/01 16:23:57)
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
 * Verify views reply.  At this point, you cannot add file path info to
 * the reply packet.  This feature will have to be added at a later date
 * for completeness.
 */
public class ERepVerifyViews extends EPDC_Reply {

   public ERepVerifyViews() {
      super(EPDC.Remote_ViewsVerify);
   }

   protected int fixedLen() {
      return _fixed_length + super.fixedLen();
   }

   protected int varLen() {
      return 0;
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

      writeInt(fixedData, 0);          // number of file paths
      writeOffset(fixedData, 0);       // offset to file paths

      return fixedLen() + varLen();
   }

   // data fields
   private static final int _fixed_length = 8;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
