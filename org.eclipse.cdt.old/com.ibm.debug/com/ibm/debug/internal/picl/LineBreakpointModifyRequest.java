package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/LineBreakpointModifyRequest.java, eclipse, eclipse-dev, 20011129
// Version 1.7 (last modified 11/29/01 14:16:00)
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
import com.ibm.debug.model.Breakpoint;
import com.ibm.debug.model.LineBreakpoint;
import com.ibm.debug.model.Location;

public class LineBreakpointModifyRequest extends BreakpointModifyRequest {

	private IMarker fMarker = null;
	private IMarkerDelta fDelta = null;

	/**
	 * Constructor for LineBreakpointModifyRequest
	 */

	public LineBreakpointModifyRequest(PICLDebugTarget debugTarget,
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
    	// suggestion to check to see if any attributes changed.   If none then it was just to enable/disable

    	try {
    		String[] attributes = {IPICLDebugConstants.MODULE_NAME,
   									IPICLDebugConstants.OBJECT_NAME,
   									IPICLDebugConstants.SOURCE_FILE_NAME,
   									IPICLDebugConstants.EVERY_VALUE,
   									IPICLDebugConstants.FROM_VALUE,
   									IPICLDebugConstants.TO_VALUE,
   									IPICLDebugConstants.CONDITIONAL_EXPRESSION,
   									IPICLDebugConstants.THREAD };

    		Object[] values = fMarker.getAttributes(attributes);

    		// check the thread value, if "Every" then make it 0
    		if (((String)values[7]).equalsIgnoreCase("every")) {
    			values[7] = "0";
    		}

    		int threadID = Integer.parseInt((String)values[7]);
    		int fLineNumber = getDebugTarget().getBreakpointManager().getLineNumber(fMarker);

    		if (fBreakpoint.isDeferred()) {
 				rc = ((LineBreakpoint)fBreakpoint).modify(fLineNumber,
 														  (String)values[0],
 														  (String)values[1],
 														  (String)values[2],
 														  ((Integer)values[3]).intValue(),
 														  ((Integer)values[4]).intValue(),
 														  ((Integer)values[5]).intValue(),
 														  (String)values[6],
 														  threadID,
 														  syncRequest(),
														  fMarker);
    		} else {

    			// compare the line from the existing breakpoint to see if a new location is required.

    			Location loc = ((LineBreakpoint)fBreakpoint).getLocationWithinView(getDebugEngine().getSourceViewInformation());
    			if (loc.lineNumber() != fLineNumber) {
    				loc = new Location(loc.file(),fLineNumber);
    			}

	    		rc = ((LineBreakpoint)fBreakpoint).modify(loc,
															((Integer)values[3]).intValue(),
															((Integer)values[4]).intValue(),
															((Integer)values[5]).intValue(),
  	  												  		(String)values[6],
    												  		threadID,
    												  		syncRequest(),
															fMarker);

    		}

            if (!rc)
 	      		throw new PICLException(PICLUtils.getResourceString(msgKey + "line_error"));

    	} catch(IOException ioe) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "send_error")));
    	} catch(CoreException ce) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "modify_error")));
    	} finally {
    		endRequest();
    	}


	}

}

