package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepPutEngineSettings.java, java-epdc, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:26:11)
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
 *  Remote_PutEngineSettings reply
 */
public class ERepPutEngineSettings extends EPDC_Reply {

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   ERepPutEngineSettings (byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      super(packetBuffer, dataInputStream);

      if ((_XMLStreamOffset = dataInputStream.readInt()) != 0)
      {
           _XMLStream = new EExtString(packetBuffer,
                      new OffsetDataInputStream(packetBuffer,_XMLStreamOffset));

      }
   }

   public ERepPutEngineSettings(int streamSize, byte[] XMLStream)
   {
     super(EPDC.Remote_PutEngineSettings);

     _XMLStream = new EExtString(XMLStream);
   }

   /**
    * Return the size of the XML stream
    */
   public int streamLength() {
      return _XMLStream.streamLength();
   }

   /**
    * Return a handle to the XML stream
    * @exception IOException if an I/O error occurs
    */
   public String XMLStream() throws IOException {
      if ((_XMLStream == null) && (_XMLStreamOffset != 0)) {
         posBuffer(_XMLStreamOffset);
         _XMLStream = readExtString();
      }

      if (_XMLStream == null)
          return null;

      return _XMLStream.string();
   }

   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException
   {
     int offset = baseOffset;
     super.toDataStreams(fixedData, varData, baseOffset);
     offset += super.varLen();

     writeOffsetOrZero(fixedData, offset, _XMLStream);

     if (_XMLStream != null)
         _XMLStream.output(varData);

     return fixedLen() + varLen();
   }

   /**
    * Return the length of the fixed component
    */
   protected int fixedLen()
   {
     return _fixed_length + super.fixedLen();
   }

   /**
    * Return the length of the variable component
    */
   protected int varLen()
   {
     return totalBytes(_XMLStream);
   }

   // data fields
   private int _XMLStreamOffset;
   private EExtString _XMLStream;

   private static final int _fixed_length = 4;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";
}
