package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepGetStackColumns.java, java-epdc, eclipse-dev, 20011128
// Version 1.7.1.2 (last modified 11/28/01 16:23:36)
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
 * ERepGetStackColumns EPDC structure.  Used by ERepStackDetailsGet.
 */
public class ERepGetStackColumns extends EPDC_Base {

   ERepGetStackColumns(int columnID, String columnName, int columnNameAlignment, int columnTextAlignment) {
      _columnID = columnID;
      _columnName = new EStdString(columnName);
      _columnNameAlignment = columnNameAlignment;
      _columnTextAlignment = columnTextAlignment;
   }

  //Decode reply
  ERepGetStackColumns(byte[] packetBuffer, DataInputStream dataInputStream)
  throws IOException
  {
    _columnID = dataInputStream.readInt();

    int offset;
    if ((offset = dataInputStream.readInt()) != 0) //Offset to column name
      _columnName = new EStdString(packetBuffer,
                                    new OffsetDataInputStream(packetBuffer, offset)
                                    );

    _columnNameAlignment = dataInputStream.readInt();
    _columnTextAlignment = dataInputStream.readInt();

  }

  public int getColumnID()
  {
    return _columnID;
  }

  public String getColumnName()
  {
    if (_columnName != null)
      return _columnName.string();
    else
      return null;
  }

  public int getColumnNameAlignment()
  {
    return _columnNameAlignment;
  }

  public int getColumnTextAlignment()
  {
    return _columnTextAlignment;
  }

   /**
    * Return fixed component size
    */
   protected int fixedLen() {
      return _fixed_length;
   }

   /**
    * Static function that returns fixed component size
    */
   protected static int _fixedLen() {
      return _fixed_length;
   }

   /**
    * Return variable component size
    */
   protected int varLen() {
      return super.varLen() + totalBytes(_columnName);
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
      writeInt(fixedData, _columnID);

      offset += writeOffsetOrZero(fixedData, offset, _columnName);

      if (_columnName != null)
         _columnName.output(varData);

      writeInt(fixedData, _columnNameAlignment);
      writeInt(fixedData, _columnTextAlignment);

      return _fixed_length + offset - baseOffset;
   }

   // data fields
   private int _columnID;
   private EStdString _columnName;
   private int _columnNameAlignment;
   private int _columnTextAlignment;

   private static final int _fixed_length = 16;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
