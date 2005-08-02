/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.debug.win32.core.dbgeng;

public class IDebugClient {

	private long p;
	
	public IDebugClient() {
		p = create();
	}
	
	private static native long create();
	
	public void createProcess(long server, String commandLine, int createFlags) {
		createProcess(p, server, commandLine, createFlags);
	}
	
	public void createProcess(String commandLine, int createFlags) {
		createProcess(p, 0, commandLine, createFlags);
	}
	
	private static native int createProcess(long p,
											long server,
											String commandLine,
											int createFlags);
	
	public void setOutputCallbacks(IDebugOutputCallbacks callback) {
		setOutputCallbacks(p, callback.p);
	}
	
	private static native int setOutputCallbacks(long p, long callbackp);
	
}
