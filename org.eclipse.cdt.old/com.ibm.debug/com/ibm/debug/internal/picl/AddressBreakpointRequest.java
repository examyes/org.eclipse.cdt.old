package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/AddressBreakpointRequest.java, eclipse, eclipse-dev, 20011129
// Version 1.13 (last modified 11/29/01 14:15:55)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.AddressBreakpoint;
import com.ibm.debug.model.Breakpoint;
import com.ibm.debug.model.DebugEngine;
import com.ibm.debug.model.Location;
import com.ibm.debug.model.ViewFile;
import java.io.IOException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;

class AddressBreakpointRequest extends LocationBreakpointRequest {

	protected final String msgKey = super.msgKey + "address.";

	private String fAddressExpression = null;

    /**
     * Constructor for AddressBreakpointRequest
     */
    AddressBreakpointRequest(IMarker marker, PICLDebugTarget debugTarget) throws DebugException {
        super(marker,debugTarget);
        if (fAttributes.containsKey(IPICLDebugConstants.ADDRESS_EXPRESSION))
            fAddressExpression = (String)fAttributes.get(IPICLDebugConstants.ADDRESS_EXPRESSION);
    }

    /**
     * @see BreakpointRequest#setBreakpoint()
     */
    protected boolean setBreakpoint() throws PICLException {

		if (getDeferred()) {   // The model doesn't support deferred address breakpoints
	        throw new PICLException(PICLUtils.getResourceString(msgKey + "not_supported"));
		}

        boolean rc = false;
		//     I think that the location is required for the conditional expression.   The address is passed in
		//     the address expression attribute
		//     If the location can't be determined then still try to set the breakpoint

		ViewFile viewFile = getDebugTarget().getViewFile(getMarker(),getDebugTarget().getDebugEngine().getSourceViewInformation());

		Location loc = null;
		if (viewFile != null)
			loc = new Location(viewFile,getLineNumber());

        try {
            rc = getDebugTarget().getDebuggeeProcess().setAddressBreakpoint(getEnabled(),
            										   getAddressExpression(),
                                                       loc,
                                                       getEveryValue(),getFromValue(),getToValue(),
                                                       getConditionalExpression(),
                                                       getThreadAsNumber(),
                                                       syncRequest(),
                                                       getMarker());
        } catch(IOException e) {
      		throw new PICLException(PICLUtils.getResourceString(super.msgKey + "sendError"));
        }

		if (!rc)
        	throw new PICLException(PICLUtils.getResourceString(super.msgKey + "setError"));


        return true;
    }

	/**
	 * Gets the addressExpression
	 * @return Returns a String
	 */
	protected String getAddressExpression() {
		return fAddressExpression;
	}

	/**
	 * Update the attributes of the marker with the values from the breakpoint
	 * @param The marker that matches the breakpoint
	 * @param The breakpoint from the engine
	 * @return true if successful
	 */

	public static boolean updateAttributes(IMarker marker, Breakpoint breakpoint, PICLDebugTarget debugTarget) {

		AddressBreakpoint bkp = (AddressBreakpoint)breakpoint;

		String[] attributeNames = {IPICLDebugConstants.UPDATE_BREAKPOINT,
								   IPICLDebugConstants.THREAD,
								   IPICLDebugConstants.EVERY_VALUE,
								   IPICLDebugConstants.TO_VALUE,
								   IPICLDebugConstants.FROM_VALUE,
								   IPICLDebugConstants.CONDITIONAL_EXPRESSION,
								   IPICLDebugConstants.DEFERRED,
								   IPICLDebugConstants.ADDRESS_EXPRESSION};

		Object[] values = {new Boolean(false),
						   String.valueOf(bkp.getThreadID()),
						   new Integer(bkp.getEveryVal()),
						   new Integer(bkp.getToVal()),
						   new Integer(bkp.getFromVal()),
						   bkp.getExpression(),
						   new Boolean(bkp.isDeferred()),
						   bkp.getAddress()};


		try {
	        marker.setAttributes(attributeNames, values);
		} catch(CoreException ce) {
			return false;
		}

		return true;
	}

}
