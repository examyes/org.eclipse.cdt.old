package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/BreakpointDeleteRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.11 (last modified 11/28/01 16:00:41)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.resources.IMarker;

import com.ibm.debug.model.Breakpoint;

import java.io.IOException;

public class BreakpointDeleteRequest extends BreakpointRequest {

	protected final String msgKey = super.msgKey + "delete.";

	private Breakpoint fBreakpoint = null;
        private Object fRequestProperty = null;

	public BreakpointDeleteRequest(PICLDebugTarget debugTarget, Breakpoint breakpoint, Object requestProperty) {
		super(debugTarget);

		fBreakpoint = breakpoint;
                fRequestProperty = requestProperty;
	}

	/**
	 * @see PICLEngineRequest#execute()
	 */
	public void execute() throws PICLException {

		beginRequest();

    	boolean rc = true;

    	try {
    		rc = fBreakpoint.remove(syncRequest(), fRequestProperty);
			if (!rc)
 	      		throw new PICLException(PICLUtils.getResourceString(msgKey + "delete_error"));
    	} catch(IOException ioe) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "sendError")));
    	} finally {
    		endRequest();
    	}
	}

}
