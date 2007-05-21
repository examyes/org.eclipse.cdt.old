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
import java.io.InputStream;

/**
 * @author Doug Schaefer
 *
 */
public class DebugInputStream extends InputStream {

	private final long handle;
	private final byte[] buffer = new byte[512];
	private int pos = 0;
	private int len = 0;
	
	public DebugInputStream(long handle) {
		this.handle = handle;
	}
	
	public int read() throws IOException {
		if (pos > len) {
			int[] numRead = new int[1];
			boolean rc = ReadFile(handle, buffer, numRead);
			if (!rc)
				throw new IOException("Railed to read");
			
			len = numRead[0];
			if (len == 0)
				// End of File
				return -1;
			
			pos = 0;
		}
		
		return buffer[pos++];
	}

	private static native boolean ReadFile(long handle, byte[] buffer, int[] numberOfBytesRead);

}
