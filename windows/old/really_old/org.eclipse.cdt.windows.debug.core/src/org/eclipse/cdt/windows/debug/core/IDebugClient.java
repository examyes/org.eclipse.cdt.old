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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 * 
 * This is actually IDebugClient5.
 * 
 */
public class IDebugClient {

	@SuppressWarnings("unused")
	private long p;
	
	private native int init();

	public IDebugClient() throws CoreException {
		if (HRESULT.FAILED(init()))
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					"Failed to init"));
	}
	
	public native int getIdentity(String[] identity);

	public native int createProcess2(long server, String commandLine,
			DebugCreateProcessOptions options,
			String initialDirectory,
			Map<String, String> environment);
	
	public native int setEventCallbacks(IDebugEventCallbacks callbacks);
	
}
