package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EReqContextConvert.java, java-epdc, eclipse-dev, 20011128
// Version 1.3.1.2 (last modified 11/28/01 16:25:17)
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
 * Request to get the context information for the required view
 */

public class EReqContextConvert extends EPDC_Request
{
  public EReqContextConvert(EStdView context, short viewNum)
  {
    super (EPDC.Remote_ContextConvert);

    _context = context;
    _viewNum = viewNum;
  }

  public void output(DataOutputStream dataOutputStream)
  throws IOException
  {
    super.output(dataOutputStream);

    _context.output(dataOutputStream);
    dataOutputStream.writeShort(_viewNum);
  }

  EReqContextConvert(byte[] inBuffer, DataInputStream inStream) throws IOException
  {
    super(inBuffer, inStream);

    _context = new EStdView(inBuffer, inStream);
    _viewNum = inStream.readShort();
  }

  public EStdView context()
  {
    return _context;
  }

  public short viewNum()
  {
    return _viewNum;
  }

  protected int fixedLen()
  {
    return _fixed_length + super.fixedLen();
  }

  public void write(PrintWriter printWriter)
  {
    super.write(printWriter);

    indent(printWriter);

    printWriter.print("Convert location: ");
    _context.write(printWriter);
    printWriter.println("    To view #: " + _viewNum);
  }

  //data fields
  private EStdView _context;
  private short _viewNum;
  private static final int _fixed_length = 14;
}


