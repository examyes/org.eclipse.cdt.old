package com.ibm.debug.connection;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1998, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/connection/SocketConnectionInfo.java, java-connection, eclipse-dev, 20011128
// Version 1.7.1.2 (last modified 11/28/01 16:29:48)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.net.InetAddress;

public class SocketConnectionInfo extends TCPIPConnectionInfo
{
  private int _connectAttempts;
  private boolean _hasConnectAttempts = false;
  /**
   * @param ipAddress If this argument is null, the getNewConnection method
   * will use the local host by default.
   * @param port If this argument is null, the getNewConnection method
   * will use port 8000 by default.
   */

  public SocketConnectionInfo(String ipAddress, String port)
  {
	super(ipAddress, port);
  }
  /**
   * @param ipAddress If this argument is null, the getNewConnection method
   * will use the local host by default.
   * @param port If this argument is null, the getNewConnection method
   * will use port 8000 by default.
   * @param connectAttempts number of connection attempts before timeing out
   */
  public SocketConnectionInfo(String ipAddress, String port,
							  int connectAttempts)
  {
	super(ipAddress, port);
	_connectAttempts = connectAttempts;
	_hasConnectAttempts = true;
  }

 /**
   * If the host contained in this SocketConnectionInfo object is null,
   * the getNewConnection method will use the local host by default.
   * If the port # contained in this SocketConnectionInfo object is null,
   * the getNewConnection method will use a default of 8000.
   * @param connectionMode This arg is ignored in this override of the
   * @param noWait If true and fail to connect the first try do not wait
   *               and try again.
   * getNewConnection method since socket connections can only be clients.
   */

  public Connection getNewConnection(int connectionMode, boolean noWait)
  throws java.io.IOException
  {
	 String port = (_conduit == null) ? getDefaultConduit() : _conduit;

	 // We will pass "localhost" vs. InetAddress.getLocalHost() because the
	 // latter converts the localhost to an actual valid host and that will
	 // cause a problem if the user does not have a tcpip connection but
	 // still wants to debug his application using "localhost".

	 InetAddress addr = (_host == null) ? InetAddress.getByName(getDefaultHost()) :
										  InetAddress.getByName(_host);

         if (noWait)
                 return new SocketConnection(addr, Integer.parseInt(port), 1, 500);
	 else if (!_hasConnectAttempts)
		 return new SocketConnection(addr, Integer.parseInt(port));
	 else
		 // 500 (500ms) is the default sleep interval
		 return new SocketConnection(addr, Integer.parseInt(port),
									 _connectAttempts, 500);
  }
}
