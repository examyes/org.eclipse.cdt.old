package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepStackDetailsGet.java, java-epdc, eclipse-dev, 20011128
// Version 1.7.1.2 (last modified 11/28/01 16:23:49)
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
 * Get stack details reply
 */
public class ERepStackDetailsGet extends EPDC_Reply {

   public ERepStackDetailsGet() {
      super(EPDC.Remote_StackDetailsGet);

      _columnInfo = new Vector();
      _defaultColumnIds = null;
   }

  //Decode EPDC reply
  ERepStackDetailsGet(byte[] packetBuffer, DataInputStream dataInputStream)
  throws IOException
  {
    super (packetBuffer, dataInputStream);  // epdc reply header

    //get numbers of columns available, as determined by the backend
    int numColumns = dataInputStream.readInt();

    _columnInfo = new Vector();

    //get offset to array of ERepGetStackColumns
    int offset = dataInputStream.readInt();
    if (offset != 0)
    {
      OffsetDataInputStream offsetDataInputStream = new OffsetDataInputStream(packetBuffer,offset);

      //get the array of ERepGetStackColumns
      for (int i = 0; i < numColumns; i++)
        _columnInfo.addElement (new ERepGetStackColumns(packetBuffer,offsetDataInputStream));
    }

    //get number of default columns to present as set by the backend
    int numDefaultColumns = dataInputStream.readInt();

    //get the array of default column ids, 1 based
    _defaultColumnIds = new int[numDefaultColumns];
    for (int i = 0; i < numDefaultColumns; i++)
      _defaultColumnIds[i] = dataInputStream.readInt();

  }

  public Vector getColumnInfo()
  {
    return _columnInfo;
  }

  public int[] getDefaultColumnIds()
  {
    return _defaultColumnIds;
  }

   /**
    * Add a new column
    */
   public void addColumn(String columnName, int columnNameAlignment, int columnTextAlignment) {
      int colId = _columnInfo.size() + 1;

      _columnInfo.addElement(new ERepGetStackColumns(colId, columnName, columnNameAlignment, columnTextAlignment));
   }

   /**
    * Set the default column list
    */
   public void setDefaultColumns(int[] defaultColumnIds) {
      _defaultColumnIds = defaultColumnIds;
   }

   /**
    * Return "fixed" component size
    */
   protected int fixedLen() {
      return super.fixedLen() + _fixed_length + _defaultColumnIds.length * 4;
   }

   /**
    * Return "variable" component size
    */
   protected int varLen() {
      int total = super.varLen();
      ERepGetStackColumns column;

      for (int i=0; i<_columnInfo.size(); i++) {
         column = (ERepGetStackColumns) _columnInfo.elementAt(i);
         total += column.fixedLen() + column.varLen();
      }

      return total;
   }

   /** Output class to data streams according to EPDC protocol.
     * @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      ERepGetStackColumns column;

      int total = super.toDataStreams(fixedData, varData, baseOffset);
      baseOffset += super.varLen();
      total += _fixed_length + _defaultColumnIds.length;

      ByteArrayOutputStream varByte2 = new ByteArrayOutputStream();
                   // data stream for variable portion of column informatin
      DataOutputStream varData2 = new DataOutputStream(varByte2);

      int offset1 = baseOffset;
      int offset2 = offset1 + _columnInfo.size() * ERepGetStackColumns._fixedLen();

      writeInt(fixedData, _columnInfo.size());

      // write out column info
      writeOffset(fixedData, offset1);
      for (int i=0; i<_columnInfo.size(); i++) {
         column = (ERepGetStackColumns) _columnInfo.elementAt(i);
         total += column.toDataStreams(varData, varData2, offset2);
         offset2 += column.varLen();
      }

      writeInt(fixedData, _defaultColumnIds.length);
      for (int i=0; i<_defaultColumnIds.length; i++)
         writeInt(fixedData, _defaultColumnIds[i]);

      varData.write(varByte2.toByteArray());

      return total;
   }

   // data fields
   private Vector _columnInfo;
   private int[] _defaultColumnIds;

   private static final int _fixed_length = 12;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
