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
package org.eclipse.cdt.debug.win32.core.cdi.events;

import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;

public class ResumedEvent implements ICDIResumedEvent {
	
	private ICDIObject source;
	private int type;
	
	public ResumedEvent(ICDIObject source, int type) {
		this.source = source;
		this.type = type;
	}
	
	public int getType() {
		return type;
	}

	public ICDIObject getSource() {
		return source;
	}
}
