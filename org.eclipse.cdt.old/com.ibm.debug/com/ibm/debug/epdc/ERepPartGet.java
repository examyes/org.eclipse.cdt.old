package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepPartGet.java, java-epdc, eclipse-dev, 20011128
// Version 1.8.1.2 (last modified 11/28/01 16:23:42)
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
 * Reply packet for EPDC Part Get request
 */
public class ERepPartGet extends EPDC_Reply {

   public ERepPartGet() {
      super(EPDC.Remote_PartGet);
      _srcLines = new Vector();
   }

   ERepPartGet( byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      super( packetBuffer, dataInputStream );

      int numberOfLines = dataInputStream.readInt();

      _srcLines = new Vector(numberOfLines);

      if (numberOfLines == 1) // The EStdSourceLine is in-place:
         _srcLines.addElement(new EStdSourceLine(packetBuffer, dataInputStream));
      else // It's an array of offsets to EStdSourceLines
      {
        int offset;

        for (int i = 0; i < numberOfLines; i++)
            if ((offset = dataInputStream.readInt()) != 0)
               _srcLines.addElement(new EStdSourceLine
                                        (
                                          packetBuffer,
                                          new OffsetDataInputStream (packetBuffer, offset)
                                        )
                                   );
      }
   }

   /**
    * Add a source line to this part.
    */
   public void addSrcLine(String SourceLine, boolean Executable, boolean isLocal) {
      _srcLines.addElement(new EStdSourceLine(SourceLine, Executable, isLocal));
   }

   /**
    * Retrieve a vector of all source lines in this reply.
    */
   public Vector sourceLines()
   {
      return _srcLines;
   }

   protected int fixedLen()
   {
      if (_srcLines.size() > 1)
         return super.fixedLen() + _fixed_length + 4 * _srcLines.size();
      return super.fixedLen() + _fixed_length;
   }

   protected int varLen()
   {
      int total = super.varLen();
      for (int i=0; i<_srcLines.size(); i++) {
         EStdSourceLine line = (EStdSourceLine) _srcLines.elementAt(i);
         total += line.fixedLen() + line.varLen();
      }
      return total;
   }


   /**
    * Output class to data streams according to EPDC protocol.
    * @exception IOException if an I/O error occurs
    * @exception BadEPDCCommandException if the EPDC command
    *            is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      int offset1 = baseOffset;

      super.toDataStreams(fixedData, varData, offset1);
      offset1 += super.varLen();

      int offset2 = offset1;
      int total = 0;

      offset2 += EStdSourceLine.StaticfixedLen() * _srcLines.size();

      // create variable data streams to pass to EStdSourceLine
      ByteArrayOutputStream varByte = new ByteArrayOutputStream();
      DataOutputStream varData2 = new DataOutputStream(varByte);

      writeInt(fixedData, _srcLines.size());
      if (_srcLines.size() == 1)
         total += ((EStdSourceLine) _srcLines.elementAt(0)).toDataStreams(varData, varData2, offset2);
      else {
         for (int i=0; i<_srcLines.size(); i++) {
            writeOffset(fixedData, offset1);
            EStdSourceLine line = (EStdSourceLine) _srcLines.elementAt(i);
            total += line.toDataStreams(varData, varData2, offset2);
            offset1 += line.fixedLen();
            offset2 += line.varLen();
         }
      }

      varData.write(varByte.toByteArray());    // output second byte array to variable data stream

      return total + fixedLen() + varLen();
   }

   public void write(PrintWriter printWriter)
   {
     super.write(printWriter);

     indent(printWriter);

     printWriter.println("Number of lines returned: " +
                         ((_srcLines == null) ? 0 : _srcLines.size()));
   }

   // data fields
   private Vector _srcLines;

   private int _fixed_length = 4;
}
