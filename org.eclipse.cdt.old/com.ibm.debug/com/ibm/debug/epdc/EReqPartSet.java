package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqPartSet.java, java-epdc, eclipse-dev, 20011128
// Version 1.7.1.2 (last modified 11/28/01 16:24:20)
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
 * Set part file name request
 */
public class EReqPartSet extends EPDC_Request
{
   public EReqPartSet(String newFileName,
               short partID,
               short viewNum,
               int fileIndex)
   {
     super(EPDC.Remote_PartSet);

     _partFileName = new EStdString(newFileName);

     _partID = partID;
     _viewID = viewNum;
     _srcFileIndex = fileIndex;
   }

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqPartSet (byte[] inBuffer) throws IOException {
      super(inBuffer);

      _offsetPartFileName = readOffset();
      _partID = readShort();
      _viewID = readShort();
      _srcFileIndex = readInt();
      markOffset();

      _partFileName = null;
   }

  public void output(DataOutputStream dataOutputStream)
    throws IOException
  {
      super.output(dataOutputStream);

      // Write out the offsets of the variable length data:

      writeOffsetOrZero(dataOutputStream, fixedLen() + super.varLen(), _partFileName);

      dataOutputStream.writeShort(_partID);
      dataOutputStream.writeShort(_viewID);
      dataOutputStream.writeInt(_srcFileIndex);

      // Now write out the variable length data:

      if (_partFileName != null)
        _partFileName.output(dataOutputStream);
  }

   /**
    * Return the part file name
    * @exception IOException if an I/O error occurs
    */
   public String partFileName() throws IOException {
      if ((_partFileName == null) && (_offsetPartFileName != 0)) {
         posBuffer(_offsetPartFileName);
         _partFileName = readStdString();
      }

      if (_partFileName != null)
         return _partFileName.string();
      else
         return null;
   }

   /**
    * Return the part ID
    */
   public short partID() {
      return _partID;
   }

   /**
    * Return the view ID
    */
   public short viewID() {
      return _viewID;
   }

   /**
    * Return the source file index to change
    */
   public int srcFileIndex() {
      return _srcFileIndex;
   }

   protected int fixedLen()
   {
      return _fixed_length + super.fixedLen();
   }

   protected int varLen()
   {
      return super.varLen() + totalBytes(_partFileName);
   }

   // data fields
   private int _offsetPartFileName;
   private short _partID;
   private short _viewID;
   private int _srcFileIndex;

   private EStdString _partFileName;

   private static final int  _fixed_length = 12;
}
