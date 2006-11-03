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
public class DebugModuleAndId {

	private final long moduleBase;
	private final long id;

	public DebugModuleAndId(long moduleBase, long id) {
		this.moduleBase = moduleBase;
		this.id = id;
	}

	public long getModuleBase() {
		return moduleBase;
	}

	public long getId() {
		return id;
	}
	
}
