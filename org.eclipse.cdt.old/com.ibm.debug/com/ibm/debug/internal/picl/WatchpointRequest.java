package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/WatchpointRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.13 (last modified 11/28/01 15:59:04)
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;

import com.ibm.debug.model.Breakpoint;
import com.ibm.debug.model.Watchpoint;

class WatchpointRequest extends EventBreakpointRequest {
	protected final String msgKey = super.msgKey + "watch.";
	private String fAddressExpression = null;
	private int fNumberOfBytes = 0;

    /**
     * Constructor for WatchpointRequest
     */
    WatchpointRequest(IMarker marker, PICLDebugTarget debugTarget) throws DebugException {
        super(marker,debugTarget);
        if (fAttributes.containsKey(IPICLDebugConstants.ADDRESS_EXPRESSION))
            fAddressExpression = (String)fAttributes.get(IPICLDebugConstants.ADDRESS_EXPRESSION);
        if (fAttributes.containsKey(IPICLDebugConstants.NUM_BYTES_MONITORED))
        	fNumberOfBytes = ((Integer)fAttributes.get(IPICLDebugConstants.NUM_BYTES_MONITORED)).intValue();
    }

    /**
     * @see BreakpointRequest#setBreakpoint()
     */
    public boolean setBreakpoint() throws PICLException {
         boolean rc = false;
        try {
            rc = getDebugTarget().getDebuggeeProcess().setWatchpoint(getAddressExpression(),
                                                       getEveryValue(),getFromValue(),getToValue(),
                                                       getNumberOfBytes(),
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
	 * Gets the numberOfBytes
	 * @return Returns a int
	 */
	protected int getNumberOfBytes() {
		return fNumberOfBytes;
	}

	/**
	 * Update the attributes of the marker with the values from the breakpoint
	 * @param The marker that matches the breakpoint
	 * @param The breakpoint from the engine
	 * @return true if successful
	 */

	public static boolean updateAttributes(IMarker marker, Breakpoint breakpoint, PICLDebugTarget debugTarget) {

		Watchpoint bkp = (Watchpoint)breakpoint;

		String[] attributeNames = {IPICLDebugConstants.UPDATE_BREAKPOINT,
								   IPICLDebugConstants.THREAD,
								   IPICLDebugConstants.EVERY_VALUE,
								   IPICLDebugConstants.TO_VALUE,
								   IPICLDebugConstants.FROM_VALUE,
								   IPICLDebugConstants.ADDRESS_EXPRESSION,
								   IPICLDebugConstants.NUM_BYTES_MONITORED};

		Object[] values = {new Boolean(false),
						   String.valueOf(bkp.getThreadID()),
						   new Integer(bkp.getEveryVal()),
						   new Integer(bkp.getToVal()),
						   new Integer(bkp.getFromVal()),
						   bkp.getExpression(),
						   new Integer(bkp.getByteCount())};


		try {
	        marker.setAttributes(attributeNames, values);
		} catch(CoreException ce) {
			return false;
		}

		return true;
	}

}
