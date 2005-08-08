/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.debug.win32.core.dbgeng;

public class IDebugControl {

	private long p;
	
	public IDebugControl() {
		p = create();
	}
	
	private static native long create();

	public void release() {
		release(p);
		p = 0;
	}
	
	private static native void release(long p);
	
	private static final int INFINITE = 0xFFFFFFFF;
	
	public int waitForEvent(int timeout) {
		return waitForEvent(p, timeout);
	}
	
	public int waitForEvent() {
		return waitForEvent(p, INFINITE);
	}
	
	private static native int waitForEvent(long p, int timeout);
	
}
