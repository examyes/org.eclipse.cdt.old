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

package org.eclipse.cdt.windows.debug.cdi.core.events;

import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDIEvent implements ICDIEvent {

	private final ICDIObject source;
	
	public WinCDIEvent(ICDIObject source) {
		this.source = source;
	}
	
	public ICDIObject getSource() {
		return source;
	}

}
