package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EStdDate.java, java-epdc, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:24:37)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class EStdDate extends EPDC_Base{

   public EStdDate(int day, int month, int year) {
      super();
      _day = (short) day;
      _month = (short) month;
      _year = (short) year;
   }

   EStdDate(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
     _day = dataInputStream.readShort();
     _month = dataInputStream.readShort();
     _year = dataInputStream.readShort();
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
      writeShort(fixedData, _day);
      writeShort(fixedData, _month);
      writeShort(fixedData, _year);
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
   private short _day;
   private short _month;
   private short _year;

   private static int _fixed_length = 6;

}

