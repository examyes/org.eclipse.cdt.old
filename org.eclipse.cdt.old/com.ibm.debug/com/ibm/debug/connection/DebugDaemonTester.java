package com.ibm.debug.connection;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/connection/DebugDaemonTester.java, java-connection, eclipse-dev, 20011128
// Version 1.5.1.2 (last modified 11/28/01 16:29:43)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.lang.Thread;

public class DebugDaemonTester extends DebugDaemon.EngineConnectingAdapter
{
  public void engineConnecting(DebugDaemon.EngineConnectingEvent e)
  {
    DebugDaemon.EngineParameters parms = e.getEngineParameters();

    System.out.println("Engine parameters: ");
    System.out.println("  host = " + parms.getConnectionInfo().getHost());
    System.out.println("  conduit = " + parms.getConnectionInfo().getConduit());
    System.out.println("  title = " + parms.getTitle());
    System.out.println("  arguments = " + parms.getArguments());
  }

  static public void main(String[] args)
  {
    String tsap = "8000";      // transport service address point

    if (args.length > 0)
      tsap = args[0];

    DebugDaemonTester tester = new DebugDaemonTester();

    // create the debug daemons
    DebugDaemon tcpipDaemon = new TCPIPDebugDaemon(tsap);
    DebugDaemon npipeDaemon = new NamedPipeDebugDaemon(tsap);
    DebugDaemon appcDaemon = new APPCDebugDaemon("APPC" + tsap);

    // add myself as the engine connecting event listener
    tcpipDaemon.addEngineConnectingListener(tester);
    npipeDaemon.addEngineConnectingListener(tester);
    appcDaemon.addEngineConnectingListener(tester);

    // start the daemon threads
    tcpipDaemon.start();
    npipeDaemon.start();
    appcDaemon.start();

    Thread.currentThread().stop();
  }
}
