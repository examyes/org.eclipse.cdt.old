package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqViewFileInfoSet.java, java-epdc, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:25:52)
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
 * ViewFileInfoSet request
 */
public class EReqViewFileInfoSet extends EPDC_Request {

  public EReqViewFileInfoSet(short partID, short viewNum, short srcFileIndex, EViews eview)
  {
     super(EPDC.Remote_ViewFileInfoSet);
     _partID       = partID;
     _viewNum      = viewNum;
     _srcFileIndex = srcFileIndex;
     _eview        = eview;
  }

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqViewFileInfoSet (byte[] inBuffer) throws IOException {
      super(inBuffer);

      _partID       = readShort();
      _viewNum      = readShort();
      _srcFileIndex = readInt();

      DataInputStream dataInputStream = getDataInputStream();
      _eview        = new EViews(inBuffer, dataInputStream);
   }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    dataOutputStream.writeShort(_partID);
    dataOutputStream.writeShort(_viewNum);
    dataOutputStream.writeInt(_srcFileIndex);

    _eview.output(dataOutputStream, fixedLen() + super.varLen());
  }

   /**
    * Get the part id
    */
   public short partID() {
      return _partID;
   }

   /**
    * Get the view number
    */
   public short viewNum() {
      return _viewNum;
   }

   /**
    * Get the source file index
    */
   public int srcFileIndex() {
      return _srcFileIndex;
   }

   /**
    * Get the eview structure
    */
   public EViews getEView() {
      return _eview;
   }

   /**
    * Return size of "fixed" portion
    */
   protected int fixedLen()
   {
      return _fixed_length + _eview.fixedLen() + super.fixedLen();
   }

   /**
    * Return size of "variable" portion
    */
   protected int varLen()
   {
      return super.varLen() + _eview.varLen();
   }

   // data fields
   short  _partID;
   short  _viewNum;
   int    _srcFileIndex;
   EViews _eview;

   private static final int _fixed_length = 8;
}

