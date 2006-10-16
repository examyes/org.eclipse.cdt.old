/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.windows.debug.core;

/**
 * @author Doug Schaefer
 *
 * Collection of static methods and constants for dealing with HRESULT
 * return values.
 */
public abstract class HRESULT {
	
	public static final int S_OK = 0;
	public static final int S_FALSE = 1;
	public static final int E_UNEXPECTED = 0x8000FFFF;

	public static native boolean FAILED(int hr);
	
}
