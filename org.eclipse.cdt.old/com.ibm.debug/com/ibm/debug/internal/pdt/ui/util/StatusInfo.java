package com.ibm.debug.internal.pdt.ui.util;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/util/StatusInfo.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 15:58:46)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.DialogPage;
import com.ibm.debug.internal.picl.PICLUtils;

/**
 * A settable IStatus
 * Can be an error, warning, info or ok. For error, info and warning states,
 * a message describes the problem
 */
public class StatusInfo implements IStatus {

	private String fStatusMessage;
	private int fSeverity;

	public StatusInfo() {
		fStatusMessage= null;
		fSeverity= OK;
	}
	/**
	 * @see IStatus#getChildren()
	 */
	public IStatus[] getChildren() {
		return new IStatus[0];
	}
	/**
	 * @see IStatus#getCode()
	 */
	public int getCode() {
		return fSeverity;
	}
	/**
	 * @see IStatus#getException()
	 */
	public Throwable getException() {
		return null;
	}
	/**
	 * @see IStatus#getMessage
	 */
	public String getMessage() {
		return fStatusMessage;
	}
	/**
	 * @see IStatus#getPlugin()
	 */
	public String getPlugin() {
		return PICLUtils.getModelIdentifier();
	}
	/**
	 * @see IStatus#getSeverity()
	 */
	public int getSeverity() {
		return fSeverity;
	}
	public boolean isError() {
		return fSeverity == IStatus.ERROR;
	}
	public boolean isInfo() {
		return fSeverity == IStatus.INFO;
	}
	/**
	 * @see IStatus#isMultiStatus()
	 */
	public boolean isMultiStatus() {
		return false;
	}
	public boolean isOK() {
		return fSeverity == IStatus.OK;
	}
	public boolean isWarning() {
		return fSeverity == IStatus.WARNING;
	}
	/**
	 * @see IStatus#matches(int)
	 */
	public boolean matches(int severityMask) {
		return (fSeverity & severityMask) != 0;
	}
	public void setError(String errorMessage) {
		fStatusMessage= errorMessage;
		fSeverity= IStatus.ERROR;
	}
	public void setInfo(String infoMessage) {
		fStatusMessage= infoMessage;
		fSeverity= IStatus.INFO;
	}
	public void setOK() {
		fStatusMessage= null;
		fSeverity= IStatus.OK;
	}
	public void setWarning(String warningMessage) {
		fStatusMessage= warningMessage;
		fSeverity= IStatus.WARNING;
	}
}
