package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqCommandLogExecute.java, java-epdc, eclipse-dev, 20011128
// Version 1.2.1.2 (last modified 11/28/01 16:26:16)
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
 * Class for Execute Program request.
 */
public class EReqCommandLogExecute extends EPDC_Request {

   public EReqCommandLogExecute(String cmd)
   {
     super(EPDC.Remote_CommandLogExecute);
     _commandString = new EStdString(cmd);
   }

   public void output(DataOutputStream dataOutputStream)
   throws IOException
   {
     super.output(dataOutputStream);
     _commandString.output(dataOutputStream);
   }

   EReqCommandLogExecute(byte[] inBuffer) throws IOException {
      super (inBuffer);
     _commandString = readStdString();
   }

  EReqCommandLogExecute(byte[] packetBuffer,  DataInputStream dataInputStream)
    throws IOException
  {
    super(packetBuffer);

    int _commandStringOffset = 0;
    _commandString = new EStdString(packetBuffer,
                         new OffsetDataInputStream(packetBuffer, _commandStringOffset)   );
  }

  public String getCommandString()
  {
     if(_commandString==null)
        return null;
     else
        return _commandString.string();
  }

  protected int fixedLen()
  {
     return super.fixedLen() + _fixed_length;
  }

  protected int varLen()
  {
      return super.varLen() + totalBytes(_commandString);
  }

   // data fields
   private EStdString _commandString;
   private static final int _fixed_length = 0;
}
