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
// Version %I% (last modified %G% %U%)   (based on Jde 12/29/98 1.6)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl;

import java.io.*;

/**
 * Thread which redirectes JDE's standard input to the TCP/IP socket stream
 * defined as the debuggee standard input
 */
class InputRedirector extends Thread
{
   /**
    * Create a new InputRedirector object
    * @param dbgStdIn the debuggee standard in (System.in) thread
    */
   InputRedirector(OutputStream dbgStdIn)
   {
      super();
      setDaemon(true);
      _dbgStdIn = dbgStdIn;
   }

   /**
    * Start the thread -- redirects all System.in input to the debuggee's
    * System.in
    */
   public void run()
   {
      int bytesRead = 0;
      byte[] b = new byte[8192];       // data buffer

      try {
         while (true) {
            if (System.in.available() > 0) {
               bytesRead = System.in.read(b);
               _dbgStdIn.write(b, 0, bytesRead);
            }

            sleep(50);
         }
      } catch (Exception e) {
         Gdb.handleException(e);
      }
   }

   // data fields
   private OutputStream _dbgStdIn;
}

