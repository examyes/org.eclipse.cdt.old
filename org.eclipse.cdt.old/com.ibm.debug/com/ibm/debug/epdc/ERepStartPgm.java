package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepStartPgm.java, java-epdc, eclipse-dev, 20011128
// Version 1.8.1.2 (last modified 11/28/01 16:23:51)
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
 * Start Program reply
 */
public class ERepStartPgm extends EPDC_Reply {

   /**
    * Create new start program packet.
    * @param DU dispatchable unit for program
    * @param Whystop why the program stopped
    * @param ExceptionMsg exception message string, null if no message
    */
   public ERepStartPgm(int DU, int Whystop, String ExceptionMsg) {
      super(EPDC.Remote_StartPgm);
      _DU = DU;
      _whyStop = (short)Whystop;

      if (ExceptionMsg != null && ExceptionMsg.length() > 0)
         _exceptionMsg = new EStdString(ExceptionMsg);
   }

   ERepStartPgm( byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      super( packetBuffer, dataInputStream );

      _DU = dataInputStream.readInt();
      _whyStop = dataInputStream.readShort();

      int offset;

      if ((offset = dataInputStream.readInt()) != 0)
         _exceptionMsg = new EStdString
                             (
                               packetBuffer,
                               new OffsetDataInputStream (packetBuffer, offset)
                             );
   }


   /** Output class to data streams according to EPDC protocol.
     * @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      int offset = baseOffset;

      super.toDataStreams(fixedData, varData, baseOffset);
      offset += super.varLen();

      writeInt(fixedData, _DU);
      writeShort(fixedData, _whyStop);

      offset += writeOffsetOrZero(fixedData, offset, _exceptionMsg);

      if (_exceptionMsg != null)
         _exceptionMsg.output(varData);

      return fixedLen() + varLen();
   }

   protected int fixedLen() {
      return super.fixedLen() + _fixed_length;
   }

   protected int varLen() {
      return super.varLen() + totalBytes(_exceptionMsg);
   }

   public short getWhyStop()
   {
     return _whyStop;
   }

   public int getThreadID()
   {
     return _DU;
   }

   public String getExceptionMsg()
   {
     if (_exceptionMsg == null)
        return null;
     else
        return _exceptionMsg.string();
   }

   // Data fields
   private int _DU;
   private short _whyStop;
   private EStdString _exceptionMsg;

   private static final int _fixed_length = 10;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";


}
