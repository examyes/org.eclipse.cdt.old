package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqExpressionFree.java, java-epdc, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:24:10)
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
 * Free expression monitor request
 */
public class EReqExpressionFree extends EPDC_Request {

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqExpressionFree(byte[] inBuffer) throws IOException {
      super(inBuffer);

      _exprID = readShort();
   }

   public EReqExpressionFree(short id)
   {
      super(EPDC.Remote_ExpressionFree);

      _exprID = id;
   }

   /**
    * Return expression ID
    */
   public short exprID() {
      return _exprID;
   }

   protected int fixedLen()
   {
     return _fixed_length + super.fixedLen();
   }

   public void output(DataOutputStream dataOutputStream)
   throws IOException
   {
      super.output(dataOutputStream);

      dataOutputStream.writeShort(_exprID);
   }

   // data fields
   private short _exprID;
   private static final int _fixed_length = 2;

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

}
