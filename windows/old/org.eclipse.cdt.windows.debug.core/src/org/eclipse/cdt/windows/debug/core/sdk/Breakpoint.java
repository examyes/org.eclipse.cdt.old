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

/**
 * @author Doug Schaefer
 *
 */
public class Breakpoint {

	private final long address;
	
	public Breakpoint(long address) {
		this.address = address;
	}
	
	public long getAddress() {
		return address;
	}
	
}
