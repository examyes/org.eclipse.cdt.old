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
 */
public class DebugLastEventInfoSystemError {

	private int error;
	private int level;

	@SuppressWarnings("unused")
	private DebugLastEventInfoSystemError(int error, int level) {
		this.error = error;
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}
	
	public int getError() {
		return error;
	}
	
}
