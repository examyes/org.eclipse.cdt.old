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

package org.eclipse.cdt.windows.debug.core.engine;

/**
 * @author Doug Schaefer
 *
 * The root class for debugger events that are raised by the
 * engine and passed to any listeners.
 */
public abstract class DebugEvent {

	public static final int UNKNOWN = 0;
	
	public int getType() {
		return UNKNOWN;
	}
	
}
