package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/ERepGetNextStackEntry.java, java-epdc, eclipse-dev, 20011128
// Version 1.13.1.2 (last modified 11/28/01 16:23:34)
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
 * ERepGetNextStackEntry EPDC strucutre
 */
public class ERepGetNextStackEntry extends EPDC_Base {

   public ERepGetNextStackEntry(EPDC_EngineSession engineSession) {
      _columns = new EStdString[9];
      for (int i=0; i<9; i++)
         _columns[i] = null;
      _stackEntryRemStkSize = null;
      _numOfParms = 0;
      _stackEntryViewInfo = new EStdView[engineSession._viewInfo.length];
   }

  //Decode reply stream
  ERepGetNextStackEntry(byte[] packetBuffer, DataInputStream dataInputStream, EPDC_EngineSession engineSession)
  throws IOException
  {
    dataInputStream.readShort();  //reserved field (used to be entry id)

    //get the column contents
    _columns = new EStdString[9];
    int offset;
    for (int i=0; i<9; i++)
      if ((offset = dataInputStream.readInt()) != 0)
        _columns[i] = new EStdString(packetBuffer,
                                      new OffsetDataInputStream(packetBuffer, offset)
                                      );

    //get remaining stack size
    if ((offset = dataInputStream.readInt()) != 0)
      _stackEntryRemStkSize = new EStdString(packetBuffer,
                                      new OffsetDataInputStream(packetBuffer, offset)
                                      );

    //get number of parameters
    _numOfParms = dataInputStream.readInt();

    dataInputStream.readInt(); //reserved field (used to be # of views)

    if ((offset = dataInputStream.readInt()) != 0)
    {
       //get an array for all EStdView for this stack entry

       _stackEntryViewInfo = new EStdView[engineSession._viewInfo.length];

       OffsetDataInputStream offsetDataInputStream =
                             new OffsetDataInputStream(packetBuffer, offset);

       for (int i=0; i<engineSession._viewInfo.length; i++)
           _stackEntryViewInfo[i] = new EStdView(packetBuffer,
                                                 offsetDataInputStream);
    }
  }

  public String[] columns()
  {
    String[] str = new String[_columns.length];

    for (int i=0;i<str.length;i++)
    {
       if (_columns[i] != null)
          str[i] = _columns[i].string();
    }
    return str;
  }

  public String stackEntryRemStkSize()
  {
    if (_stackEntryRemStkSize != null)
      return _stackEntryRemStkSize.string();
    else
      return null;
  }

  public int numOfParms()
  {
    return _numOfParms;
  }

  public EStdView[] stackEntryViewInfo()
  {
    return _stackEntryViewInfo;
  }

   /**
    * Set column text, columns number 1 to 9
    */
   public void setColumn(int columnNum, String columnInfo) {
      _columns[columnNum-1] = new EStdString(columnInfo);
   }

   /**
    * Set remaining stack size (I don't know what this is -- EP)
    */
   public void setRemStkSize(String remStkSize) {
      _stackEntryRemStkSize = new EStdString(remStkSize);
   }

   /**
    * Set number of parameters
    */
   public void setNumParms(int numParms) {
      _numOfParms = numParms;
   }

   /**
    * Set view information.  This must be set for each view.
    */
   public void setStackEntryViewInfo(short viewNo, short PPID, int srcFileIndex, int lineNum) {
      _stackEntryViewInfo[viewNo-1] = new EStdView(PPID, viewNo, srcFileIndex, lineNum);
   }

   /**
    * Set view information.  This must be set for each view.
    */
   public void setStackEntryViewInfo(EStdView view) {
      _stackEntryViewInfo[view.getViewNo() - 1] = view;
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

      writeShort(fixedData, (short) 0);        // entry id (NOT USED)

      // write out columns
      for (int i=0; i<9; i++)
      {
         offset += writeOffsetOrZero(fixedData, offset, _columns[i]);

         if (_columns[i] != null)
            _columns[i].output(varData);
      }

      offset += writeOffsetOrZero(fixedData, offset, _stackEntryRemStkSize);

      if (_stackEntryRemStkSize != null)
         _stackEntryRemStkSize.output(varData);

      writeInt(fixedData, _numOfParms);
      writeInt(fixedData, _stackEntryViewInfo.length);
      writeOffset(fixedData, offset);
      for (int i=0; i<_stackEntryViewInfo.length; i++) {
         offset += _stackEntryViewInfo[i].toDataStreams(varData, null, 0);
      }
      return _fixed_length + offset - baseOffset;
   }


   /**
    * Return size of fixed length portion
    */
   protected int fixedLen() {
      return _fixed_length;
   }

   /**
    * Static function that returns size of fixed length portion
    */
   protected static int _fixedLen() {
      return _fixed_length;
   }

   /**
    * Return size of variable length portion
    */
   protected int varLen() {
      int total=0;

      // add column information size
      for (int i=0; i<_columns.length; i++)
         total += totalBytes(_columns[i]);

      total += totalBytes(_stackEntryRemStkSize);

      total += _stackEntryViewInfo.length * EStdView._fixedLen();

      return total;
   }

   // data fields
   private EStdString[] _columns;
   private EStdString _stackEntryRemStkSize;
   private int _numOfParms;
   private EStdView[] _stackEntryViewInfo;

   private static int _fixed_length = 54;
}
