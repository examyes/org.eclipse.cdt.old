package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqViewSearchPath.java, java-epdc, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:25:53)
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
 * TypesNumGet Request
 */
public class EReqViewSearchPath extends EPDC_Request {

   EReqViewSearchPath(byte[] inBuffer) throws IOException {
      super(inBuffer);
      _partId       = readShort();
      _viewNum      = readShort();
      _srcFileIndex = readInt();
   }

   public EReqViewSearchPath(short partId, short viewNum, int srcFileIndex)
   {
     super(EPDC.Remote_ViewSearchPath);
     _partId       = partId;
     _viewNum      = viewNum;
     _srcFileIndex = srcFileIndex;
   }

   protected int fixedLen()
   {
      return _fixed_length + super.fixedLen();
   }

   protected int varLen()
   {
      return super.varLen();
   }

   /**
    * return the part id
    */
   public short partId()
   {
      return _partId;
   }

   /**
    * return the view num
    */
   public short viewNum()
   {
      return _viewNum;
   }

   /**
    * return the src file index
    */
   public int srcFileIndex()
   {
      return _srcFileIndex;
   }

   public void output(DataOutputStream dataOutputStream)
      throws IOException
   {
      super.output(dataOutputStream);
      dataOutputStream.writeShort(_partId);
      dataOutputStream.writeShort(_viewNum);
      dataOutputStream.writeInt(_srcFileIndex);
   }

   // Datafields
   private short _partId;
   private short _viewNum;
   private int   _srcFileIndex;

   private static final int _fixed_length = 8;
}

