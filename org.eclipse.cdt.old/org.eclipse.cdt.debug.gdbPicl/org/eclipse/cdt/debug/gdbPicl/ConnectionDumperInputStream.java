package org.eclipse.cdt.debug.gdbPicl;

/*
 * Copyright (c) 1998, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */


import java.io.*;

class ConnectionDumperInputStream extends InputStream
{
  ConnectionDumperInputStream(InputStream sourceStream,
                              OutputStream dumpStream)
  {
    _sourceStream = sourceStream;
    _dumpStream = dumpStream;
  }

  public int read()
  throws IOException
  {
    int b = _sourceStream.read();

    // If dumping has not been suspended, write data to buffer:

    if (!_dumpingSuspended)
       _buffer.write(b);

    return b;
  }

  public int read(byte b[], int off, int len)
  throws IOException
  {
    int result = _sourceStream.read(b, off, len);

    // If dumping has not been suspended, write data to buffer:

    if (!_dumpingSuspended)
       _buffer.write(b, off, result);

    return result;
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
         dataOutputStream.writeByte(ConnectionDumperTimer.timerFlag);
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

  private InputStream _sourceStream;
  private OutputStream _dumpStream;
  private ByteArrayOutputStream _buffer = new ByteArrayOutputStream();
  private boolean _dumpingSuspended = false;
}
