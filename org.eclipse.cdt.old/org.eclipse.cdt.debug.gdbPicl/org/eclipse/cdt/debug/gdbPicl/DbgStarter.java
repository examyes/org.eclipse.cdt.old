/*
 * Copyright (c) 1995, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

package org.eclipse.cdt.debug.gdbPicl;

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
