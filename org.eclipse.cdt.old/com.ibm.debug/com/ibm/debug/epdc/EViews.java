package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EViews.java, java-epdc, eclipse-dev, 20011128
// Version 1.13.1.2 (last modified 11/28/01 16:24:45)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

public class EViews extends EPDC_Base{

   public EViews (int RecLength, int Startline, int Endline, String Filename,
            String BaseFilename, int ViewAttr) {
      super();
      _RecLength = RecLength;
      _Startline = Startline;
      _Endline = Endline;
      _Filename = new EStdString(Filename);
      _BaseFilename = new EStdString(BaseFilename);
      _ViewAttr = (byte) ViewAttr;
   }

   EViews(byte[] packetBuffer, DataInputStream dataInputStream)
   throws IOException
   {
      _RecLength = dataInputStream.readInt(); // Record length
      _Startline = dataInputStream.readInt(); // Start line
      _Endline = dataInputStream.readInt(); // End line

      int offset;

      if ((offset = dataInputStream.readInt()) != 0) // Offset to file name
         _Filename = new EStdString(packetBuffer,
                                     new OffsetDataInputStream(packetBuffer, offset)
                                    );

      if ((offset = dataInputStream.readInt()) != 0) // Offset to base file name
         _BaseFilename = new EStdString(packetBuffer,
                                     new OffsetDataInputStream(packetBuffer, offset)
                                    );

      _ViewAttr = dataInputStream.readByte(); // View attribute

      dataInputStream.readByte(); // reserved
   }

   public int recordLength()
   {
     return _RecLength;
   }

   public int firstLineNumber()
   {
     return _Startline;
   }

   public int lastLineNumber()
   {
     return _Endline;
   }

   public String name()
   {
     if (_Filename != null)
       return _Filename.string();
     else
       return null;
   }

   public String baseFileName()
   {
     if (_BaseFilename != null)
       return _BaseFilename.string();
     else
       return null;
   }

   /** Returns 'true' if the debug engine has verified that this file exists
    *  in the file system (or wherever) and can therefore be used to build the
    *  view containing this file.
    */

   public boolean hasBeenVerified()
   {
     return (_ViewAttr & EPDC.VIEW_VERIFIED) != 0;
   }

   public boolean verificationLocal()
   {
     return (_ViewAttr & EPDC.VIEW_LOCAL) != 0;
   }

   public boolean verificationAttempted()
   {
     return (_ViewAttr & EPDC.VIEW_VERIFY_ATTEMPTED) != 0;
   }

   public boolean verificationAttemptedFE()
   {
     return (_ViewAttr & EPDC.VIEW_VERIFY_ATTEMPTED_FE) != 0;
   }

   public boolean fileNameCanBeOverridden()
   {
     return (_ViewAttr & EPDC.VIEW_CHANGE_TEXT_VALID) != 0;
   }

   public boolean viewCanBeSwitched()
   {
     return (_ViewAttr & EPDC.VIEW_NO_SWITCH) == 0;
   }

   /**
    * Return the list of attributes for this view
    */
   public byte getAttributes()
   {
     return _ViewAttr;
   }

   /** Outputs
    *  the class into two byte streams for fixed and variable data,
    *  corresponding to the EPDC protocol.
    *
    *  @param fixedData output stream for the fixed data
    *  @param varData output stream for the variable data
    *  @param baseOffset the base offset to add to all offsets
    *
    *  @return total size of written data
    *  @exception IOException if an I/O error occurs
    *  @exception BadEPDCCommandException if the EPDC command
    *    is structured incorrectly
    */
   protected int toDataStreams(DataOutputStream fixedData,
         DataOutputStream varData, int baseOffset)
         throws IOException, BadEPDCCommandException {

      int offset = baseOffset;
      writeInt(fixedData, _RecLength);
      writeInt(fixedData, _Startline);
      writeInt(fixedData, _Endline);

      offset += writeOffsetOrZero(fixedData, offset, _Filename);
      offset += writeOffsetOrZero(fixedData, offset, _BaseFilename);

      if (_Filename != null)
         _Filename.output(varData);

      if (_BaseFilename != null)
         _BaseFilename.output(varData);

      writeChar(fixedData, _ViewAttr);
      writeChar(fixedData, (byte) 0);        // reserved
      return fixedLen() + varLen();
   }

   /** Return the length of the fixed component */
   protected int fixedLen() {
      return _fixed_length;
   }

      /** Return the length of the variable component */
   protected int varLen() {
      int total = 0;

      total += super.varLen();
      total += totalBytes(_Filename);
      total += totalBytes(_BaseFilename);

      return total;
   }

   void output(DataOutputStream dataOutputStream, int baseOffset)
   throws IOException
   {
     dataOutputStream.writeInt(_RecLength);
     dataOutputStream.writeInt(_Startline);
     dataOutputStream.writeInt(_Endline);

     int offset = baseOffset;

     offset += writeOffsetOrZero(dataOutputStream, offset, _Filename);
     writeOffsetOrZero(dataOutputStream, offset, _BaseFilename);

     dataOutputStream.writeByte(_ViewAttr);
     dataOutputStream.writeByte(0);           // reserved

     if (_Filename != null)
         _Filename.output(dataOutputStream);

     if (_BaseFilename != null)
         _BaseFilename.output(dataOutputStream);

   }

   /* Data members */
   private int _RecLength;
   private int _Startline;
   private int _Endline;
   private EStdString _Filename;
   private EStdString _BaseFilename;
   private byte _ViewAttr;

   private static int _fixed_length = 22;

}

