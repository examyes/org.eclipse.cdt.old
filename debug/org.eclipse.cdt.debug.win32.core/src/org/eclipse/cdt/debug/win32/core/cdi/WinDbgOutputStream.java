/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.debug.win32.core.cdi;

import java.io.IOException;
import java.io.OutputStream;

public class WinDbgOutputStream extends OutputStream {

	private WinDbgProcess process;
	private int fd;
	
	WinDbgOutputStream(WinDbgProcess process, int fd) {
		this.process = process;
		this.fd = fd;
	}

	public void write(int b) throws IOException {
		process.write(fd, b);
	}
}
