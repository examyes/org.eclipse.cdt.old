package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/PICLEngineRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.12 (last modified 11/28/01 15:59:10)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.DebugEngine;
import com.ibm.debug.model.ErrorOccurredEvent;


public abstract class PICLEngineRequest implements IPICLEngineRequestError {

	protected PICLDebugTarget fDebugTarget = null;
	private ErrorOccurredEvent fErrorEvent = null;
    protected boolean fIsInternal = false;

	protected final String msgKey = "picl_engine_request.";
	public PICLEngineRequest(PICLDebugTarget debugTarget) {
		fDebugTarget = debugTarget;
	}


	/**
	 * Marks the beginning of a request to the engine
	 * @return true if ok to proceed (i.e. there is no pending request); false indicates
	 * that there is already a pending request
	 */

	protected synchronized void beginRequest() throws PICLEngineBusyException {
		PICLEngineRequest pendingRequest = fDebugTarget.getPendingEngineRequest();
		if (pendingRequest == null) {   // no engine request pending
			fDebugTarget.setPendingEngineRequest(this);
			return;
		} else {
			String errorMsg = PICLUtils.getFormattedString(msgKey + "busy",PICLUtils.getBaseName(pendingRequest));
			PICLUtils.logText(errorMsg);
			throw new PICLEngineBusyException(errorMsg);
		}

	}

	/**
	 * Marks the completion of a request, successful or not.
	 * Will log an error if the pending request doesn't match
	 * @throws PICLException if error occurs during engine execution.
	 */
	protected synchronized void endRequest() throws PICLException {

		// first check to see if this is the matching request.   If not then log an error
		if (!fDebugTarget.getPendingEngineRequest().equals(this))
			PICLUtils.logText("#*#*#*# Mismatching pending request #*#*#*#");
		else
			PICLUtils.logText("Pending request matched " + this.toString());

		fDebugTarget.setPendingEngineRequest(null);

		if (isError())
			throw new PICLException(fErrorEvent);
	}


	/**
	 * This is called to send the request to the engine.   Each request type must implement what
	 * is required to perform the request.
	 * The implementor of this method should throw a @see PICLEngineBusy exception if the engine
	 * is not accepting the request.
	 * @throws an exception if the request fails
	 */
	public abstract void execute() throws PICLException;


    /**
     * @see IPICLEngineRequestError#setError(ErrorOccurredEvent)
     */
    public void setError(ErrorOccurredEvent errorEvent) {
    	fErrorEvent = errorEvent;  // copy of the event
    }


	/**
	 * Returns true if this request had an error
	 */
	public boolean isError() {
		return (fErrorEvent != null);
	}
	/**
	 * returns the error message text if there was an error.  @see PICLEngineRequest#isError()
	 * @return Error message text
	 */
	public String getErrorMessage() {
    	return fErrorEvent.getMessage();
	}


	/**
	 * returns the errorCode if there was an error.  @see PICLEngineRequest#isError()
	 * @return Returns a int
	 */
	public int getErrorCode() {
		return fErrorEvent.getReturnCode();
	}

	/**
	 * returns the debug engine
	 */
	public DebugEngine getDebugEngine() {
		return fDebugTarget.getDebugEngine();
	}

	/**
	 * Returns the request type... synchronous
	 */
	protected int syncRequest() {
		return DebugEngine.sendReceiveSynchronously;
	}

	/**
	 * Returns the request type ... asynchronous
	 */
	protected int asyncRequest() {
		return DebugEngine.sendReceiveDefault;
	}
	/**
	 * Gets the debugTarget
	 * @return Returns a PICLDebugTarget
	 */
	public PICLDebugTarget getDebugTarget() {
		return fDebugTarget;
	}

    /**
     * Returns whether this request is internal or not.
     * @return boolean
     */
    public boolean isInternal() {
        return fIsInternal;
    }

    /**
     * Sets the is internal flag indicating whether this is an internal
     * request or not.  The default is false (not internal).
     * @parm isInternal set the is internal flag to this value
     */
    public void setInternal(boolean isInternal) {
        fIsInternal = isInternal;
    }
}

