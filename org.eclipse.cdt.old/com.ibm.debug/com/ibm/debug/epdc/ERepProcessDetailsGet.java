package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepProcessDetailsGet.java, java-epdc, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:24:54)
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

public class ERepProcessDetailsGet extends EPDC_Reply {

   public ERepProcessDetailsGet()
   {
      super();
      setReplyCode(EPDC.Remote_ProcessDetailsGet);
      _columnInfo = new Vector();
   }

   public void addColumn(String columnName, int columnNameAlignment,
      int columnTextAlignment)
   {
      int colId = _columnInfo.size() + 1;
      _columnInfo.addElement(new ERepGetProcessColumns(colId, columnName,
         columnNameAlignment,columnTextAlignment));
   }

   public ERepProcessDetailsGet(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      super(packetBuffer, dataInputStream);

      int numColumns = dataInputStream.readInt();
      int columnInfoOffset = dataInputStream.readInt();

      _columnInfo = new Vector(numColumns);

      OffsetDataInputStream offsetDataInputStream =
                            new OffsetDataInputStream(packetBuffer,
                                                      columnInfoOffset);

      for (int i = 0; i < numColumns; i++)
          _columnInfo.addElement(new ERepGetProcessColumns(packetBuffer,
                                                           offsetDataInputStream
                                                          )
                                );
   }

   public int numColumns()
   {
       return _columnInfo.size();
   }

   public Vector columnInfo()
   {
      return _columnInfo;
   }

   /** Output class to data streams according to EPDC protocol.
    *  @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      int total = super.toDataStreams(fixedData, varData, baseOffset);
      baseOffset += super.varLen();
      total += _fixed_length;

      ByteArrayOutputStream varByte2 = new ByteArrayOutputStream();
                   // data stream for variable portion of column informatin
      DataOutputStream varData2 = new DataOutputStream(varByte2);

      int offset1 = baseOffset;
      int offset2 = offset1 + _columnInfo.size() * ERepGetProcessColumns._fixedLen();

      writeInt(fixedData, _columnInfo.size());

      writeOffset(fixedData, offset1);
      ERepGetProcessColumns column;
      for (int i=0;i <_columnInfo.size() ; i++) {
         column = (ERepGetProcessColumns) _columnInfo.elementAt(i);
         total += column.toDataStreams(varData, varData2, offset2);
         offset2 += column.varLen();
      }

      varData.write(varByte2.toByteArray());

      return total;
   }

   public int fixedLen() {
      return super.fixedLen() + _fixed_length;
   }

   public int varLen() {
      int total = super.varLen();
      ERepGetProcessColumns column;

      for (int i=0; i<_columnInfo.size(); i++) {
         column = (ERepGetProcessColumns) _columnInfo.elementAt(i);
         total += column.fixedLen() + column.varLen();
      }

      return total;
   }

   private Vector _columnInfo;

   private static int _fixed_length = 8;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
