package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/AddressBreakpointModifyRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.6 (last modified 11/28/01 16:00:39)
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
import org.eclipse.core.resources.IMarkerDelta;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import com.ibm.debug.model.AddressBreakpoint;
import com.ibm.debug.model.Breakpoint;
import com.ibm.debug.model.LineBreakpoint;
import com.ibm.debug.model.Location;

public class AddressBreakpointModifyRequest extends BreakpointModifyRequest {

	private IMarker fMarker = null;
	private IMarkerDelta fDelta = null;

	/**
	 * Constructor for AddressBreakpointModifyRequest
	 */

	public AddressBreakpointModifyRequest(PICLDebugTarget debugTarget,
										IMarker marker,
										IMarkerDelta delta,
										Breakpoint breakpoint) {

		super(debugTarget, breakpoint);
		fDelta = delta;
		fMarker = marker;
	}


	/**
	 * @see PICLEngineRequest#execute()
	 */
	public void execute() throws PICLException {

		beginRequest();

    	boolean rc = true;

    	String threadStr = null;

    	// first get all of the attributes from the breakpoint


    	try {
    		String[] attributes = {	IPICLDebugConstants.ADDRESS_EXPRESSION,
    								IPICLDebugConstants.EVERY_VALUE,
   									IPICLDebugConstants.FROM_VALUE,
   									IPICLDebugConstants.TO_VALUE,
   									IPICLDebugConstants.CONDITIONAL_EXPRESSION,
   									IPICLDebugConstants.THREAD };

    		Object[] values = fMarker.getAttributes(attributes);

    		// check the thread value, if "Every" then make it 0
    		if (((String)values[5]).equalsIgnoreCase("every"))
    			values[5] = "0";

    		int threadID = Integer.parseInt((String)values[5]);


   			// compare the line from the existing breakpoint to see if a new location is required.

   			Location loc = ((AddressBreakpoint)fBreakpoint).getLocationWithinView(getDebugEngine().getSourceViewInformation());

    		rc = ((AddressBreakpoint)fBreakpoint).modify((String)values[0],
    													  loc,
															((Integer)values[1]).intValue(),
															((Integer)values[2]).intValue(),
															((Integer)values[3]).intValue(),
  	  												  		(String)values[4],
    												  		threadID,
    												  		syncRequest(),
															fMarker);

            if (!rc)
 	      		throw new PICLException(PICLUtils.getResourceString(msgKey + "address_error"));


    	} catch(IOException ioe) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "send_error")));
    	} catch(CoreException ce) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "modify_error")));
    	} finally {
    		endRequest();
    	}


	}

}

