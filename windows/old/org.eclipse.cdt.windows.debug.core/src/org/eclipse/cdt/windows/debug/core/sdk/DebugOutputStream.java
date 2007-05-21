/**********************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.windows.debug.core.sdk;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Doug Schaefer
 *
 */
public class DebugOutputStream extends OutputStream {

	private final long handle;
	private final byte[] buffer = new byte[512];
	private int pos;
	
	public DebugOutputStream(long handle) {
		this.handle = handle;
	}
	
	public void write(int b) throws IOException {
		if (pos == buffer.length)
			flush();
		
		buffer[pos++] = (byte)b;
		
		if (b == '\n')
			flush();
	}

	public void flush() throws IOException {
		int[] numWritten = new int[1];
		boolean rc = WriteFile(handle, buffer, pos, numWritten);
		if (!rc)
			throw new IOException("Failed to write");
		
		int len = numWritten[0];
		if (len < pos)
			System.arraycopy(buffer, len, buffer, 0, pos - len);
		pos -= len;
	}
	
	private static native boolean WriteFile(long handle, byte[] buffer, int numberOfBytesToWrite,
			int[] numberOfBytesWritten);

}
