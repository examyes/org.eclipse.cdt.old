package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepGetFCT.java, java-epdc, eclipse-dev, 20011128
// Version 1.6.1.2 (last modified 11/28/01 16:23:31)
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
 * Class for FCT Change item
 */
public class ERepGetFCT extends EPDC_ChangeItem {

  ERepGetFCT(byte[] packetBuffer, DataInputStream dataInputStream)
  throws IOException
  {
    _functCustTable = new EFunctCustTable(packetBuffer, dataInputStream);
  }

   /**
    * Create a new FCT change item
    */
   public ERepGetFCT(EFunctCustTable functCustTable) {
      super();
      _functCustTable = functCustTable;
   }

   /**
    * Return fixed component length
    */
   protected int fixedLen() {
      return _functCustTable.fixedLen();
   }

   /**
    * Return variable component length
    */
   protected int varLen() {
      return 0;
   }


   /**
    * Output class to data streams according to EPDC protocol.
    * @exception IOException if an I/O error occurs
    * @exception BadEPDCCommandException if the EPDC command
    *   is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      return _functCustTable.toDataStreams(fixedData, varData, baseOffset);

   }

   public EFunctCustTable getFunctionCustomizationTable()
   {
     return _functCustTable;
   }

   public void write(PrintWriter printWriter)
   {
      printWriter.println();
      _functCustTable.write(printWriter);
   }

   // data fields
   private EFunctCustTable _functCustTable;
   private static final int _fixed_length = 0;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}