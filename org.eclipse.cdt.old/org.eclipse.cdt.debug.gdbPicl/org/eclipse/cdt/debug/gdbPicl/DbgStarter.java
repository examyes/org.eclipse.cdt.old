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
// Version %I% (last modified %G% %U%)   (based on Jde 12/29/98 1.8)
///////////////////////////////////////////////////////////////////////

package com.ibm.debug.gdbPicl;

import java.io.*;
import java.net.*;

/**
 * This class is used to start the debug session.  The RemoteDebugger starts
 * executing this class when it is created.   DbgStarter.main() redefines
 * System.in to be a socket input stream which will be piped from the Jde's
 * System.in
 */
class DbgStarter
{
   public static void main(String[] args) {

      // loop until signalled to stop
      try {

         // set up the socket

         ServerSocket server = new ServerSocket(Integer.parseInt(args[0]));
         Socket sock = server.accept();
         InputStream is = sock.getInputStream();
         BufferedReader br = new BufferedReader(new InputStreamReader(is));

         // wait until a carriage return has been sent

         br.readLine();

         System.setIn(is);             // set System.in to be the socket input stream
      }
      catch (Exception e)
      {
         Gdb.handleException(e);
         System.exit(-1);
      }
   }
}
