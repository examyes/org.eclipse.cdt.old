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
 */
public class ExitProcessEvent extends DebugEvent {

	private final int exitCode;
	
	public ExitProcessEvent(int exitCode) {
		this.exitCode = exitCode;
	}
	
	public int getExitCode() {
		return exitCode;
	}
	
	@Override
	public int getType() {
		return EXIT_PROCESS;
	}
	
	@Override
	protected String getName() {
		return "ExitProcess";
	}
	
}
