package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqThreadThaw.java, java-epdc, eclipse-dev, 20011128
// Version 1.4.1.2 (last modified 11/28/01 16:24:32)
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
 * ThreadThaw Program Request.
 */
public class EReqThreadThaw extends EPDC_Request
{
   public EReqThreadThaw(int DU)
   {
      super(EPDC.Remote_ThreadThaw);
      _DU = DU;
   }

   public void output(DataOutputStream dataOutputStream)
      throws IOException
   {
      super.output(dataOutputStream);
      dataOutputStream.writeInt(_DU);
   }

   /**
     * Constructs a new EReqThreadThaw object
     */
    EReqThreadThaw(byte[] inBuffer) throws IOException
    {
   super (inBuffer);
   _DU = readInt();
    }

   /**
     * Return the length of the fixed component
     */
    protected int fixedLen()
    {
   return _fixed_length + super.fixedLen();
    }

   /**
     * Return the length of the variable component
     */
    protected int varLen()
    {
   return super.varLen();
    }

   /**
     * Return the thread DU number for this EReqThreadThaw
     */
    public int getDU()
    {
   return _DU;
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
   throws IOException, BadEPDCCommandException
   {

   int subtotal;
   int offset = baseOffset;

   // Output header
   subtotal = super.toDataStreams(fixedData, varData, baseOffset);

   offset += super.varLen();

   writeInt(fixedData, _DU);

   return subtotal + _fixed_length + offset - baseOffset;
   }


    // Datafields
    private int _DU; // Thread DU number to freeze

    private static final int _fixed_length = 4;
}
