package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepVersion.java, java-epdc, eclipse-dev, 20011128
// Version 1.8.1.4 (last modified 11/28/01 16:25:01)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;
import java.util.*;

/**
 * Version reply
 */

// NOTE: Even though we extend frmo EPDC_Reply, we DO NOT output the
// reply header (via super.toDataStreams) since the FE is only expecting our
// data members and nothing more.  Also, the protocol specifically states the
// message string should be encoded in ascii.  Hence the
// writeStdAsciiString() was added

public class ERepVersion extends EPDC_Reply
{
   /**
    * Create version reply packet.
    * @see EPDC
    */

   // removed the default constructor to avoid using its default EPDC version


   public ERepVersion(int debug_engine_version) {
      _reply_code           = EPDC.Remote_Version;
      _debug_engine_version = debug_engine_version;
   }

   ERepVersion( byte[] packetBuffer, DataInputStream dataInputStream )
   throws IOException
   {
     _reply_code = dataInputStream.readInt();
     _total_bytes = dataInputStream.readInt();
     _return_code = dataInputStream.readInt();
     _debug_engine_version = dataInputStream.readInt();

     if ((_message_offset = dataInputStream.readInt()) != 0)
         _message_text = new EStdString
                            (
                              packetBuffer,
                              new OffsetDataInputStream(packetBuffer, _message_offset)
                            );
   }

   /**
    * Set the engine version
    */
   public void setVersion(int debug_engine_version) {
      _debug_engine_version = debug_engine_version;
   }

   /**
    * Get the engine version
    */
   public int getVersion() {
      return _debug_engine_version;
   }

   /**
    * Output class to data streams according to EPDC protocol.
    * @exception IOException if an I/O error occurs
    * @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      int offset = baseOffset;

      // We DON'T output the reply header here

      _total_bytes = fixedLen() + varLen();

      fixedData.writeInt(_reply_code);
      fixedData.writeInt(_total_bytes);
      fixedData.writeInt(_return_code);

      fixedData.writeInt(_debug_engine_version);

      offset += writeOffsetOrZero(fixedData, offset, _message_text);

      if (_message_text != null)
         _message_text.output(varData);

      return fixedLen() + varLen();
   }

   protected int fixedLen() {
      return _fixed_length;
   }

   protected int varLen() {
      return totalBytes(_message_text);
   }

   public void write(PrintWriter printWriter)
   {
     super.write(printWriter);

     indent(printWriter);
     printWriter.println("Engine EPDC Version: " + _debug_engine_version);
   }

   // Data fields

   private int _debug_engine_version;  // the debug engine epdc version num

   private static final int _fixed_length = 20;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
