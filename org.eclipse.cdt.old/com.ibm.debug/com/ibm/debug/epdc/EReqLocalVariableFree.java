package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqLocalVariableFree.java, java-epdc, eclipse-dev, 20011128
// Version 1.7.1.2 (last modified 11/28/01 16:24:17)
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
 * Local variable free request packet
 */
public class EReqLocalVariableFree extends EPDC_Request {

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   protected EReqLocalVariableFree (byte[] inBuffer) throws IOException {
      super(inBuffer);

      _localVarDU = readInt();
      _stackEntryNum = readShort();
   }

   public EReqLocalVariableFree(int threadID, int stackEntryNumber)
   {
     super(EPDC.Remote_LocalVariableFree);

     _localVarDU = threadID;
     _stackEntryNum = stackEntryNumber;
  }

   /**
    * Return disptachible unit
    */
   public int getDU() {
      return _localVarDU;
   }

   /**
    * Return stack entry number
    */
   public int getStackEntryNum() {
      return _stackEntryNum;
   }

   /**
    * Return the length of the fixed component
    */
   protected int fixedLen()
   {
      return _fixed_length + super.fixedLen();
   }

   public void output(DataOutputStream dataOutputStream)
   throws IOException
   {
     super.output(dataOutputStream);

     dataOutputStream.writeInt(_localVarDU);
     dataOutputStream.writeShort(_stackEntryNum);
   }

   // data fields
   private int _localVarDU;
   private int _stackEntryNum;

   private static final int _fixed_length = 6;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
