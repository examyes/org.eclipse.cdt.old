package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqStackBuildView.java, java-epdc, eclipse-dev, 20011128
// Version 1.7.1.2 (last modified 11/28/01 16:24:24)
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
 * Request to get stack view information
 */

public class EReqStackBuildView extends EPDC_Request {

  public EReqStackBuildView(int stackDU, int stackEntryID)
  {
    super (EPDC.Remote_StackBuildView);

    _stackDU = stackDU;
    _stackEntryID = stackEntryID;
  }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    dataOutputStream.writeInt(_stackDU);
    dataOutputStream.writeInt(_stackEntryID);
  }

   EReqStackBuildView(byte[] inBuffer) throws IOException {
      super(inBuffer);

      _stackDU = readInt();
      _stackEntryID = readInt();
   }

   public int stackDU() {
      return _stackDU;
   }

   public int stackEntryID() {
      return _stackEntryID;
   }

   /**
    * Return size of "fixed" portion
    */
   protected int fixedLen()
   {
      int total = _fixed_length + super.fixedLen();

      return total;
   }

   public void write(PrintWriter printWriter)
   {
     super.write(printWriter);

     indent(printWriter);

     printWriter.print("Thread: " + _stackDU);
     printWriter.println("    Stack frame ID: " + _stackEntryID);
   }

   // data fields
   private int _stackDU;
   private int _stackEntryID;

   private static final int _fixed_length = 8;
}
