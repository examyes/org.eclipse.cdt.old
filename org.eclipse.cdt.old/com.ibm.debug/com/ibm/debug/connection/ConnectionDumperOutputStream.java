package com.ibm.debug.connection;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/connection/ConnectionDumperOutputStream.java, java-connection, eclipse-dev, 20011128
// Version 1.4.1.3 (last modified 11/28/01 16:29:40)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.*;

class ConnectionDumperOutputStream extends OutputStream
{
  ConnectionDumperOutputStream(OutputStream targetStream,
                               OutputStream dumpStream)
  {
    _targetStream = targetStream;
    _dumpStream = dumpStream;
  }

  public void write(int b)
  throws IOException
  {
    // Always send the data along to its ultimate destination:

    _targetStream.write(b);

    // If dumping has not been suspended, write data to buffer:

    if (!_dumpingSuspended)
       _buffer.write(b);
  }

  public void write(byte b[], int off, int len)
  throws IOException
  {
    // Always send the data along to its ultimate destination:

    _targetStream.write(b, off, len);

    // If dumping has not been suspended, write data to buffer:

    if (!_dumpingSuspended)
       _buffer.write(b, off, len);
  }


  void dumpBuffer(int packetType)
  throws IOException
  {
    int bufferSize = _buffer.size();

    if (bufferSize > 0)
    {
       try // If the dump stream is closed we don't want to fail - just
           // ignore the exception:
       {
         DataOutputStream dataOutputStream = new DataOutputStream(_dumpStream);
         // write 2 bytes of 0
         dataOutputStream.writeByte(0);
         dataOutputStream.writeByte(0);
         // write the timer flag
         dataOutputStream.writeByte(ConnectionDumperTimer.timerFlag | epdcFormatFlag);
         dataOutputStream.writeByte((byte)packetType);    // packet type
         dataOutputStream.writeInt(bufferSize);    // packet size
         dataOutputStream.writeInt((int)ConnectionDumperTimer.elapsedTime());  // time
         _buffer.writeTo(_dumpStream);
       }
       catch(IOException excp)
       {
       }

       _buffer.reset();
    }
  }

  void suspendDumping()
  {
    _dumpingSuspended = true;
  }

  void resumeDumping()
  {
    _dumpingSuspended = false;
  }

  public void close()
  throws IOException
  {
    _dumpStream.close();
  }

  private OutputStream _targetStream;
  private OutputStream _dumpStream;
  private ByteArrayOutputStream _buffer = new ByteArrayOutputStream();
  private boolean _dumpingSuspended = false;
  protected static final byte epdcFormatFlag = 0x02;
}
