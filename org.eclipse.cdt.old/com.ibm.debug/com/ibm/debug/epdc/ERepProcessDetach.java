package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepProcessDetach.java, java-epdc, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:24:53)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class ERepProcessDetach extends EPDC_Reply {

   public ERepProcessDetach(int whyStop, String exceptionMsg) {
      super();
      setReplyCode(EPDC.Remote_ProcessDetach);
      _WhyStop = whyStop;
      _ExceptionMsg = new EStdString(exceptionMsg);
   }

   ERepProcessDetach(byte[] packetBuffer, DataInputStream dataInputStream)
      throws IOException
   {
      super( packetBuffer, dataInputStream );

      _WhyStop = (int) dataInputStream.readShort();
      if ((_ExceptionMsgOffset = dataInputStream.readInt()) != 0)
         _ExceptionMsg = new EStdString
                            (
                               packetBuffer,
                               new OffsetDataInputStream (packetBuffer, _ExceptionMsgOffset)
                            );
   }

   public String exceptionMsg()
   {
     if (_ExceptionMsg != null)
       return _ExceptionMsg.string();
     else
       return null;
   }

   public int whyStop()
   {
      return _WhyStop;
   }

   /** Output class to data streams according to EPDC protocol.
    *  @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {
      int offset = baseOffset;

      super.toDataStreams(fixedData, varData, offset);
      offset += super.varLen();

      writeShort(fixedData, (short) _WhyStop);

      offset += writeOffsetOrZero(fixedData, offset, _ExceptionMsg);

      if (_ExceptionMsg != null)
         _ExceptionMsg.output(varData);

      return fixedLen() + varLen();
   }

   public int fixedLen() {
      return super.fixedLen() + _fixed_length;
   }

   public int varLen() {
      int total = 0;
      total += super.varLen();
      total += totalBytes(_ExceptionMsg);
      return total;
   }

   private int    _WhyStop;
   private int    _ExceptionMsgOffset;
   private EStdString _ExceptionMsg;

   private static int _fixed_length = 6;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
