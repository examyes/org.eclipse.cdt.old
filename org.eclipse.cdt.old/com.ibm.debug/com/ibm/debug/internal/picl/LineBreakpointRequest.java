package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/LineBreakpointRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.19 (last modified 11/28/01 15:58:52)
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
import com.ibm.debug.model.LineBreakpoint;
import com.ibm.debug.model.Location;
import com.ibm.debug.model.ViewFile;

class LineBreakpointRequest extends LocationBreakpointRequest {


	protected final String msgKey = super.msgKey + "line.";

    /**
     * Constructor for LineBreakpointRequest
     */
    LineBreakpointRequest(IMarker marker, PICLDebugTarget debugTarget) throws DebugException {
        super(marker,debugTarget);


    }

    /**
     * Sets the breakpoint
     * @return returns true if request to set breakpoint was successful.
     */
    protected boolean setBreakpoint() throws PICLException {

		boolean rc = false;

        if (getDeferred()) {

            try {
                rc = getDebugTarget().getDebuggeeProcess().setDeferredLineBreakpoint(getEnabled(),
                                                           getLineNumber(),
                                                           getModuleName(),
                                                           getFoundPartName(),
                                                           getResourceFileName(),
                                                           getThreadAsNumber(),
                                                           getEveryValue(),getFromValue(),getToValue(),
                                                           getConditionalExpression(),
                                                           syncRequest(),
                                                           getMarker());
            } catch(IOException e) {
                throw new PICLException(PICLUtils.getResourceString(super.msgKey + "sendError"));
            }


        } else {  // set the breakpoint.   This will only work if the file is actually loaded.

			// Initally try to find a part that has a source file that matches the name in the marker
			// failing that try to set a deferred source line breakpoint

        	// The following line searches for a part that contains the source file name

			ViewFile viewFile = getDebugTarget().getViewFile(getMarker(),getDebugTarget().getDebugEngine().getSourceViewInformation());

			if (viewFile == null) {  // can't find a source file in the parts that the engine knows about
	            try {				// try setting a deferred breakpoint because it allows us to send just the names
	                rc = getDebugTarget().getDebuggeeProcess().setDeferredLineBreakpoint(getEnabled(),
	                                                           getLineNumber(),
	                                                           getModuleName(),
	                                                           getFoundPartName(),
	                                                           getResourceFileName(),
	                                                           getThreadAsNumber(),
	                                                           getEveryValue(),getFromValue(),getToValue(),
	                                                           getConditionalExpression(),
	                                                           syncRequest(),
	                                                           getMarker());
	            } catch(IOException e) {
	                throw new PICLException(PICLUtils.getResourceString(super.msgKey + "sendError"));
	            }
			} else {
				try {
					rc = viewFile.setBreakpoint(getEnabled(),
										   getLineNumber(),
	                                       getEveryValue(),getFromValue(),getToValue(),
	                                       getConditionalExpression(),
										   getThreadAsNumber(),
										   syncRequest(),
										   getMarker());
				} catch(IOException e) {
	                throw new PICLException(PICLUtils.getResourceString(super.msgKey + "sendError"));
				}
	        }
        }

		if (!rc)
			throw new PICLException(PICLUtils.getResourceString(super.msgKey + "setError"));



        return true;
    }

	/**
	 * Update the attributes of the marker with the values from the breakpoint
	 * @param The marker that matches the breakpoint
	 * @param The breakpoint from the engine
	 * @return true if successful
	 */

	public static boolean updateAttributes(IMarker marker, Breakpoint breakpoint, PICLDebugTarget debugTarget) {

		LineBreakpoint bkp = (LineBreakpoint)breakpoint;

		String[] attributeNames = {IPICLDebugConstants.UPDATE_BREAKPOINT,
								   IMarker.LINE_NUMBER,
								   IPICLDebugConstants.MODULE_NAME,
								   IPICLDebugConstants.OBJECT_NAME,
								   IPICLDebugConstants.SOURCE_FILE_NAME,
								   IPICLDebugConstants.THREAD,
								   IPICLDebugConstants.EVERY_VALUE,
								   IPICLDebugConstants.TO_VALUE,
								   IPICLDebugConstants.FROM_VALUE,
								   IPICLDebugConstants.CONDITIONAL_EXPRESSION,
								   IPICLDebugConstants.DEFERRED};

		int lineNumber = 0;
		try {
			Location location = bkp.getLocationWithinView(debugTarget.getDebugEngine().getSourceViewInformation());
            if(location == null)
                return false;
			lineNumber = location.lineNumber();
		} catch(IOException ioe) {
			return false;
		}


		Object[] values = {new Boolean(false),
						   new Integer(lineNumber),
						   bkp.getModuleName(),
						   bkp.getPartName(),
						   bkp.getFileName(),
						   String.valueOf(bkp.getThreadID()),
						   new Integer(bkp.getEveryVal()),
						   new Integer(bkp.getToVal()),
						   new Integer(bkp.getFromVal()),
						   bkp.getExpression(),
						   new Boolean(bkp.isDeferred())};


		try {
	        marker.setAttributes(attributeNames, values);
		} catch(CoreException ce) {
			return false;
		}


		return true;
	}


}
