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

package org.eclipse.cdt.windows.debug.core.cdi;

import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDIDestroyedEvent extends WinCDIEvent implements ICDIDestroyedEvent {

	public WinCDIDestroyedEvent(ICDIObject source) {
		super(null, source);
	}

	public WinCDIDestroyedEvent() {
		super(null, null);
	}
	
}
