package org.eclipse.cdt.debug.gdbPicl;

/*
 * Copyright (c) 1997, 2002 International Business Machines Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms of
 * the Common Public License which accompanies this distribution.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class Connection {

	/**
	 * Get the output stream used
	 */
	final public OutputStream getOutputStream() {
		return _outputStream;
	}

	final public InputStream getInputStream() {
		return _inputStream;
	}

	/** Set the output stream that clients should use when sending data
	 *  via this connection. This method should generally only be called from a
	 *  subclass.
	 */

	protected void setOutputStream(OutputStream outputStream) {
		_outputStream = outputStream;
	}

	/** Set the input stream that clients should use when reading data
	 *  from this connection. This method should generally only be called from a
	 *  subclass.
	 */

	protected void setInputStream(InputStream inputStream) {
		_inputStream = inputStream;
	}

	public abstract void close() throws IOException;

	public abstract void flush() throws IOException;

	private OutputStream _outputStream;
	private InputStream _inputStream;
}