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
	
	private int HRESULT;
	
	@SuppressWarnings("unused")
	private HRESULTFailure(int hr, String message) {
		super(message);
		HRESULT = hr;
	}
	
	public int getHRESULT() {
		return HRESULT;
	}

}
