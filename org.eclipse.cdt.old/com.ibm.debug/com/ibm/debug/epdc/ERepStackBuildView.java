package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2000, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepStackBuildView.java, java-epdc, eclipse-dev, 20011128
// Version 1.7.1.2 (last modified 11/28/01 16:23:48)
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
 * Build stack view reply
 */
public class ERepStackBuildView extends EPDC_Reply {

  public ERepStackBuildView(short viewNo, short partID, int srcFileIndex, int srcFileLineNum)
  {
    super(EPDC.Remote_StackBuildView);
    _stackView = new EStdView(partID, viewNo, srcFileIndex, srcFileLineNum);
  }

  public ERepStackBuildView(EStdView view)
  {
    super(EPDC.Remote_StackBuildView);
    _stackView = new EStdView(view);
  }

  ERepStackBuildView(byte[] packetBuffer, DataInputStream dataInputStream)
  throws IOException
  {
    super(packetBuffer, dataInputStream);

    _stackView = new EStdView(packetBuffer, dataInputStream);
  }

  public EStdView stackView()
  {
    return _stackView;
  }


   /**
    * Return "fixed" component size
    */
   protected int fixedLen() {
      return super.fixedLen() + _stackView.fixedLen();
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
         throws IOException, BadEPDCCommandException {

      int total = super.toDataStreams(fixedData, varData, baseOffset);

      total += _stackView.toDataStreams(fixedData, null, 0);
      return total;
   }

   public void write(PrintWriter printWriter)
   {
     super.write(printWriter);

     indent(printWriter);

     _stackView.write(printWriter);
     printWriter.println();
   }

   // data fields
   EStdView _stackView;

   private static final int _fixed_length = 0;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
