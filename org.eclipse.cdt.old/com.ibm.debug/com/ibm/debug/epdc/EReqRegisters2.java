package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqRegisters2.java, java-epdc, eclipse-dev, 20011128
// Version 1.1.1.2 (last modified 11/28/01 16:25:33)
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
 * Monitor registers request
 */
public class EReqRegisters2 extends EPDC_Request
{
  public EReqRegisters2(int registersDU, int groupID, int registersAttr)
  {
    super(EPDC.Remote_Registers2);

    _registersDU = registersDU;
    _groupID = groupID;
    _registersAttr = registersAttr;
  }

   /**
    * Decodes request from an input buffer
    * @exception IOException if an I/O error occurs
    */
   EReqRegisters2 (byte[] inBuffer) throws IOException {
      super(inBuffer);

      _registersDU = readInt();
      _groupID = readInt();
      _registersAttr = readInt();
   }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    dataOutputStream.writeInt(_registersDU);
    dataOutputStream.writeInt(_groupID);
    dataOutputStream.writeInt(_registersAttr);
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
     * Returns 'true' if enable registers on the backend. Register change
     * packets will be generated. Otherwise, returns 'false'.
     */
     public boolean isRegistersEnabled()
     {
        return (_registersAttr == EPDC.RegistersEnabled);
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
   private int _registersAttr;

   private static final int _fixed_length = 12;
}

