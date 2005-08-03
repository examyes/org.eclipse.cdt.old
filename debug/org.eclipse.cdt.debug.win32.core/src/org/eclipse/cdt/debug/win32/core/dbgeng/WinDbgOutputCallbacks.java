/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.debug.win32.core.dbgeng;

import org.eclipse.cdt.debug.win32.core.cdi.WinDbgInputStream;


public class WinDbgOutputCallbacks extends IDebugOutputCallbacks {

	private WinDbgInputStream stdout;
	private WinDbgInputStream stderr;
	
	public WinDbgOutputCallbacks(WinDbgInputStream stdout, WinDbgInputStream stderr) {
		this.stdout = stdout;
		this.stderr = stderr;
	}
	
	public void output(int mask, String text) {
		switch (mask) {
		case DEBUG_OUTPUT.DEBUG_OUTPUT_ERROR:
			stderr.put(text);
			break;
		default:
			stdout.put(text);
			break;
		}
	}

}
