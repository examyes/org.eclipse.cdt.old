package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqProcessDetach.java, java-epdc, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:24:57)
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
 * ProcessDetach Request
 */
public class EReqProcessDetach extends EPDC_Request {

  /**
   * Given an array of bytes this constructor will decode the request
   *
   */
   EReqProcessDetach( byte[] inBuffer ) throws IOException {
      super( inBuffer );
      _ReqProcessId = readInt();
      _ReqProcessDetachAction = readInt();

      markOffset();   // save current position as the end of fixed part
   }

   public EReqProcessDetach(int processId, int processDetachAction)
   {
     super(EPDC.Remote_ProcessDetach);
     _ReqProcessId = processId;
     _ReqProcessDetachAction = processDetachAction;
   }

   public int processId() {
      return _ReqProcessId;
   }

   public int processDetachAction() {
      return _ReqProcessDetachAction;
   }

   /** Return the length of the fixed component */
   protected int fixedLen() {
      return _fixed_length + super.fixedLen();
   }

  public void output(DataOutputStream dataOutputStream)
    throws IOException
  {
      super.output(dataOutputStream);

      writeInt(dataOutputStream, _ReqProcessId);
      writeInt(dataOutputStream, _ReqProcessDetachAction);
   }

   private int _ReqProcessId;
   private int _ReqProcessDetachAction;

   private static final int _fixed_length = 8;
}
