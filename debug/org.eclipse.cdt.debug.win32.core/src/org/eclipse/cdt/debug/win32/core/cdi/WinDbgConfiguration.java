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
package org.eclipse.cdt.debug.win32.core.cdi;

import org.eclipse.cdt.debug.core.cdi.ICDIConfiguration;

/**
 */
public class WinDbgConfiguration implements ICDIConfiguration {

	public boolean supportsTerminate() {
		return true;
	}

	public boolean supportsDisconnect() {
		return false;
	}

	public boolean supportsSuspend() {
		return false;
	}

	public boolean supportsResume() {
		return true;
	}

	public boolean supportsRestart() {
		return false;
	}

	public boolean supportsStepping() {
		return false;
	}

	public boolean supportsInstructionStepping() {
		return false;
	}

	public boolean supportsBreakpoints() {
		return true;
	}

	public boolean supportsRegisters() {
		return false;
	}

	public boolean supportsRegisterModification() {
		return false;
	}

	public boolean supportsSharedLibrary() {
		return false;
	}

	public boolean supportsMemoryRetrieval() {
		return false;
	}

	public boolean supportsMemoryModification() {
		return false;
	}

	public boolean supportsExpressionEvaluation() {
		return false;
	}

	public boolean terminateSessionOnExit() {
		return true;
	}
}
