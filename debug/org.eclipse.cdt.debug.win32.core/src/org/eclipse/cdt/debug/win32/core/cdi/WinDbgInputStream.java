/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.debug.win32.core.cdi;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

public class WinDbgInputStream extends InputStream {

	LinkedList strings = new LinkedList();
	int pos = 0;
	
	public synchronized int read() throws IOException {
		if (strings.isEmpty())
			return -1;
		
		String text = (String)strings.getFirst();
		if (pos >= text.length()) {
			strings.removeFirst();
			if (strings.isEmpty()) {
//				try {
//					wait();
//				} catch (InterruptedException e) {
//					throw new IOException("interrupted");
//				}
				return -1;
			}
			text = (String)strings.getFirst();
			pos = 0;
		}
			
		return text.charAt(pos++);
	}

	public synchronized void put(String text) {
		if (text.length() == 0)
			// ignore zero length strings
			return;
		
		strings.addLast(text);
		notifyAll();
	}
	
}
