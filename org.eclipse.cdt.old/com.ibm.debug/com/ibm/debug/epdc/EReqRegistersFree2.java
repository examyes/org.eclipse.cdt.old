package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqRegistersFree2.java, java-epdc, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:25:36)
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
 * Free registers request
 */
public class EReqRegistersFree2 extends EPDC_Request
{
  public EReqRegistersFree2(int registersDU, int groupID)
  {
    super(EPDC.Remote_RegistersFree2);

    _registersDU = registersDU;
    _groupID = groupID;
  }

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqRegistersFree2 (byte[] inBuffer) throws IOException {
      super(inBuffer);

      _registersDU = readInt();
      _groupID = readInt();
   }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    dataOutputStream.writeInt(_registersDU);
    dataOutputStream.writeInt(_groupID);
  }

   /**
    * Get the dispatchable unit for stack
    */
   public int registersDU()
   {
      return _registersDU;
   }

   /**
    * Returns the register group id
    */
    public int groupID()
    {
      return _groupID;
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
   private int _registersDU;
   private int _groupID;

   private static final int _fixed_length = 8;
}

