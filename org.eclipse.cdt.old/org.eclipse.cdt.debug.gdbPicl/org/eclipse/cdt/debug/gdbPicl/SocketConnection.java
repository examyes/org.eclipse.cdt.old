package org.eclipse.cdt.debug.gdbPicl;

/*
 * Copyright (c) 1997, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.IOException;
import java.net.Socket;

/** Establish a connection between the model and the debug engine using a socket.
 */

public class SocketConnection extends Connection {
	private Socket socket;

	public SocketConnection(Socket socket) throws IOException {
		this.socket = socket;
		super.setOutputStream(socket.getOutputStream());
		super.setInputStream(socket.getInputStream());
	}

	public void close() throws IOException {
		if (socket != null)
			socket.close();
	}
	/**
	 * @see Connection#flush()
	 */
	public void flush() throws IOException {
		if (getOutputStream() != null)
			getOutputStream().flush();
	}

}