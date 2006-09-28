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
 * This exception holds the HRESULT and is thrown when a Windows operation fails.
 */
public class HRESULTFailure extends Exception {

	private static final long serialVersionUID = 1;
	
	@SuppressWarnings("unused")
	private int hr;
	@SuppressWarnings("unused")
	private String file;
	@SuppressWarnings("unused")
	private int line;
	
	@SuppressWarnings("unused")
	private HRESULTFailure(int hr, String message, String file, int line) {
		super(message);
		this.hr = hr;
		this.file = file;
		this.line = line;
	}
	
	public static final int S_OK = 0;
	public static final int S_FALSE = 1;
	public static final int E_UNEXPECTED = 0x8000FFFF;
	
	public int getHR() {
		return hr;
	}
	
}
