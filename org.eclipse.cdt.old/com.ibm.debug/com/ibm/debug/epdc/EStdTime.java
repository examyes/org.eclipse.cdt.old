package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdTime.java, java-epdc, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:24:42)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class EStdTime extends EPDC_Base{

   public EStdTime(int secs, int minutes, int hours) {
      super();
      _secs = (short) secs;
      _minutes = (short) minutes;
      _hours = (short) hours;
   }

   EStdTime(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
     _secs = dataInputStream.readShort();
     _minutes = dataInputStream.readShort();
     _hours = dataInputStream.readShort();
   }

   /** Outputs
    *  the class into two byte streams for fixed and variable data,
    *  corresponding to the EPDC protocol.
    *
    *  @param fixedData output stream for the fixed data
    *  @param varData output stream for the variable data
    *  @param baseOffset the base offset to add to all offsets
    *
    *  @return total size of written data
    *  @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {
      writeShort(fixedData, _secs);
      writeShort(fixedData, _minutes);
      writeShort(fixedData, _hours);
      return _fixed_length;
   }

   /** Return the length of the fixed component */
   protected int fixedLen() {
      return _fixed_length;
   }

      /** Return the length of the variable component */
   protected int varLen() {
      return 0;
   }

   /* Data members */
   private short _secs;
   private short _minutes;
   private short _hours;

   private static int _fixed_length = 6;

}

