/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.debug.win32.core.cdi;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;

public class WinDbgTargetConfiguration implements ICDITargetConfiguration {

	public boolean supportsTerminate() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsDisconnect() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSuspend() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsResume() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsRestart() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsStepping() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsInstructionStepping() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsBreakpoints() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsRegisters() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsRegisterModification() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSharedLibrary() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsMemoryRetrieval() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsMemoryModification() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsExpressionEvaluation() {
		// TODO Auto-generated method stub
		return false;
	}

	public ICDITarget getTarget() {
		// TODO Auto-generated method stub
		return null;
	}

}
