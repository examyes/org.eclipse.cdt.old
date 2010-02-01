/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Doug Schaefer (WRS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.build.core;

import java.io.OutputStream;

import org.eclipse.cdt.build.core.IBuildConsole;

/**
 * A build console that just sends output to stdout/stderr.
 */
public class StdBuildConsole implements IBuildConsole {

	@Override
	public boolean isAvailable() {
		// always available since we just dump to stdout/stderr
		return true;
	}
	
	@Override
	public OutputStream getOutputStream() {
		return System.out;
	}
	
	@Override
	public OutputStream getErrorStream() {
		return System.err;
	}
	
	@Override
	public void activate() {
		// we're active all the time anyway
	}
	
}
