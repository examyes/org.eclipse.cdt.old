package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepContextConvert.java, java-epdc, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:25:18)
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
 * Build Remote_ContextConvert reply
 */
public class ERepContextConvert extends EPDC_Reply
{
  public ERepContextConvert(EStdView context) {
    super(EPDC.Remote_ContextConvert);

    _context = context;
  }

  ERepContextConvert(byte[] packetBuffer, DataInputStream dataInputStream)
  throws IOException
  {
    super(packetBuffer, dataInputStream);

    _context = new EStdView(packetBuffer, dataInputStream);
  }

  public EStdView context()
  {
    return _context;
  }


   /**
    * Return "fixed" component size
    */
   protected int fixedLen()
   {
      return super.fixedLen() + _context.fixedLen();
   }

   /**
    * Return "variable" component size
    */
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
         throws IOException, BadEPDCCommandException
  {

    int total = super.toDataStreams(fixedData, varData, baseOffset);
    _context.output(fixedData);

    total += _fixed_length;
    return total;
  }

  public void write(PrintWriter printWriter)
  {
    super.write(printWriter);

    indent(printWriter);

    printWriter.print("Location: ");
    _context.write(printWriter);
    printWriter.println();
  }

   // data fields
   EStdView _context;

   private static final int _fixed_length = 0;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
