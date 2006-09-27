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

import java.util.Map;

/**
 * @author Doug Schaefer
 * 
 * This is actually IDebugClient5.
 * 
 */
public class IDebugClient {

	@SuppressWarnings("unused")
	private long p;
	
	private native long init() throws HRESULTFailure;

	public IDebugClient() throws HRESULTFailure {
		p = init();
	}
	
	public native String getIdentity();

	public native void createProcess2(long server, String commandLine,
			DebugCreateProcessOptions options,
			String initialDirectory,
			Map<String, String> environment) throws HRESULTFailure;
	
	public native void setEventCallbacks(IDebugEventCallbacks callbacks) throws HRESULTFailure;
	
}
