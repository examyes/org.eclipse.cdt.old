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
package org.eclipse.cdt.debug.win32.core.os;

/**
 * @author Doug Schaefer
 */
public class CONTEXT {

	/**
	 * Native goodies
	 */
	private long p;

	private static native void staticInitNative();
	
	static {
		staticInitNative();
	}
	
	private native void initNative();
	
	protected native void finalize() throws Throwable;
	
	/**
	 * Create and zero out the context structure
	 */
	public CONTEXT() {
		initNative();
	}
	
	/**
	 * Zero out the values in the context
	 */
	public native void clear();

}
