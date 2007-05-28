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

import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration3;

/**
 * @author Doug Schaefer
 *
 */
public class WinCDITargetConfiguration implements ICDITargetConfiguration3 {

	private final WinCDITarget target;
	
	public WinCDITargetConfiguration(WinCDITarget target) {
		this.target = target;
	}
	
	public boolean needsRegistersUpdated(ICDIEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean needsVariablesUpdated(ICDIEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsAddressBreaksOnStartup() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsPassiveVariableUpdate() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsRuntimeTypeIdentification() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsThreadControl() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsBreakpoints() {
		return true;
	}

	public boolean supportsDisconnect() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsExpressionEvaluation() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsInstructionStepping() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsMemoryModification() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsMemoryRetrieval() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsRegisterModification() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsRegisters() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsRestart() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsResume() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSharedLibrary() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsStepping() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsSuspend() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean supportsTerminate() {
		return true;
	}

	public ICDITarget getTarget() {
		return target;
	}

}
