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

/**
 * @author Doug Schaefer
 *
 */
public abstract class WinCDILocation {

	public class LocationNotFound extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	public abstract long getAddress() throws LocationNotFound;
	
}
