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

package org.eclipse.cdt.windows.debug.cdi.core.model;

import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDIObject implements ICDIObject {

	private final WinCDITarget target;
	
	public WinCDIObject(WinCDITarget target) {
		this.target = target;
	}
	
	public ICDITarget getTarget() {
		return target;
	}
	
}
