package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqStack.java, java-epdc, eclipse-dev, 20011128
// Version 1.7.1.2 (last modified 11/28/01 16:24:23)
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
 * Monitor stack request
 */
public class EReqStack extends EPDC_Request {

  public EReqStack(int stackDU)
  {
    super(EPDC.Remote_Stack);

    _stackDU = stackDU;
  }

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqStack (byte[] inBuffer) throws IOException {
      super(inBuffer);

      _stackDU = readInt();
   }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    dataOutputStream.writeInt(_stackDU);
  }

   /**
    * Get the dispatchable unit for stack
    */
   public int stackDU() {
      return _stackDU;
   }

   /**
    * Return size of "fixed" portion
    */
   protected int fixedLen()
   {
      int total = _fixed_length + super.fixedLen();

      return total;
   }

   // data fields
   private int _stackDU;

   private static final int _fixed_length = 4;
}

