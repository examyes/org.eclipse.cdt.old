/////////////////////////////////////////////////////////////////////////
//                     IBM Confidential
//  xxxx-xxx (C) Copyright IBM Corp. 1995, 2001 All rights reserved.
//             OCO Source Materials-Property of IBM.
//
// The source code for this program is not published or otherwise
// divested of its trade secrets, irrespective of what has been
// deposited with the U.S. Copyright Office.
//
// %W%
// Version %I% (last modified %G% %U%)   (based on Jde 3/9/01 1.2)
///////////////////////////////////////////////////////////////////////
package com.ibm.debug.gdbPicl;

import java.lang.*;
import java.io.*;

class BufferedStreamRedirector implements Runnable
{
  /**
   * Create a new BufferedStreamRedirector object
   * @param istream The input stream to be redirected.
   * @param ostream The output stream to write to.
   */

  BufferedStreamRedirector(final InputStream istream,
                           final OutputStream ostream)
  {
    super();
    _in = new BufferedReader(new InputStreamReader(istream));
    _out = new BufferedWriter(new OutputStreamWriter(ostream));
  }

  /**
   * Start redirecting the stream.
   */

  public void start()
  {
    _redirectorThread = new Thread(this);
    _redirectorThread.start();
  }

  /**
   * Stop redirecting the stream.
   */

  public void stop()
  {
    if (_redirectorThread != null && _redirectorThread.isAlive())
    {
      Thread tmpThread = _redirectorThread;
      _redirectorThread = null;
      tmpThread.interrupt();
    }
    else
    {
      _redirectorThread = null;
    }
  }
  
  /**
   * Implementation of Runnable run method, performs the redirection.
   */
  public void run()
  {
    try
    {
      int bytesRead = 0;
      int bufferSize = 8192;
      char[] buffer = new char[bufferSize];

      bytesRead = _in.read(buffer, 0, bufferSize);
      while(_redirectorThread != null && bytesRead != -1)
      {
        _out.write(buffer, 0, bytesRead);
        _out.flush();
        bytesRead = _in.read(buffer, 0, bufferSize);
      }
    }
    catch (IOException excp)
    {
      // If the JVM goes away (because the debuggee finished) we may get
      // an IOException, just ignore it.
    }
  }

  private final BufferedReader _in;
  private final BufferedWriter _out;
  private volatile Thread _redirectorThread;
}
  
