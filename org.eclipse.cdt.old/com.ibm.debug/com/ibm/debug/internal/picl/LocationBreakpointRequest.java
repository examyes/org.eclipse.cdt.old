package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/LocationBreakpointRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.9 (last modified 11/28/01 15:58:53)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.DebugException;

public abstract class LocationBreakpointRequest extends BreakpointCreateRequest {

    private String fConditionalExpression = null;
	private boolean fDeferred = false;


    /**
     * Constructor for LocationBreakpointRequest
     */
    LocationBreakpointRequest(IMarker marker, PICLDebugTarget debugTarget) throws DebugException {
        super(marker,debugTarget);

        // Get the line number from the marker
        setLineNumber(getDebugTarget().getBreakpointManager().getLineNumber(marker));


        if (fAttributes != null) {
            if (fAttributes.containsKey(IPICLDebugConstants.CONDITIONAL_EXPRESSION))
                fConditionalExpression = (String)fAttributes.get(IPICLDebugConstants.CONDITIONAL_EXPRESSION);

            if (fAttributes.containsKey(IPICLDebugConstants.DEFERRED))
                fDeferred = ((Boolean)fAttributes.get(IPICLDebugConstants.DEFERRED)).booleanValue();

        }

    }

    /**
     * Gets the deferred
     * @return Returns a boolean
     */
    protected boolean getDeferred() {
        return fDeferred;
    }
    /**
     * Gets the conditionalExpression
     * @return Returns a String
     */
    protected String getConditionalExpression() {
        return fConditionalExpression;
    }

	/**
	 * Sets the deferred
	 * @param deferred The deferred to set
	 */
	protected void setDeferred(boolean deferred) {
		fDeferred = deferred;
	}

}
