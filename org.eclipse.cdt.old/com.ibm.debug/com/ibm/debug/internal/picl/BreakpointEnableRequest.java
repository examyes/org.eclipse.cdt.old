package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/BreakpointEnableRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.7 (last modified 11/28/01 16:00:47)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.IOException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.IDebugConstants;

import com.ibm.debug.model.Breakpoint;

public class BreakpointEnableRequest extends BreakpointRequest {

	protected final String msgKey = super.msgKey + "enable.";
	private Breakpoint fBreakpoint = null;
    private IMarker fMarker = null;

	public BreakpointEnableRequest(PICLDebugTarget debugTarget, Breakpoint breakpoint, IMarker marker) {
		super(debugTarget);

		fBreakpoint = breakpoint;
        fMarker = marker;
	}

	/**
	 * @see PICLEngineRequest#execute()
	 */
	public void execute() throws PICLException {

		beginRequest();

    	boolean rc = true;

    	try {
    		if (fMarker.getAttribute(IDebugConstants.ENABLED, true))
    			rc = fBreakpoint.enable(syncRequest(), fMarker);
    		else
    			rc = fBreakpoint.disable(syncRequest(), fMarker);
			if (!rc)
 	      		throw new PICLException(PICLUtils.getResourceString(msgKey + "enable_error"));
    	} catch(IOException ioe) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "sendError")));
    	} finally {
    		endRequest();
    	}
	}

}
