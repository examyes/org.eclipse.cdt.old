package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/MonitorExpressionRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.11 (last modified 11/28/01 15:59:20)
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
import org.eclipse.core.resources.IProject;

import com.ibm.debug.model.Location;
import com.ibm.debug.model.MonitoredExpression;
import com.ibm.debug.model.ViewFile;
import com.ibm.debug.model.ViewInformation;
/**
 * Request to monitor an expression.   The expression is a String that will be evaluated at the location
 * indicated by the marker.
 */

public class MonitorExpressionRequest extends MonitorRequest {

	private IMarker fMarker = null;
	private ViewInformation fViewInformation = null;
	private String fExpression = null;
	private PICLThread fThreadContext = null;
	private MonitoredExpression fMonitoredExpression = null;
	private PICLVariable fMonitorResult = null;

    /**
     * Constructor for MonitorExpressionRequest
     */
    public MonitorExpressionRequest(PICLDebugTarget debugTarget,
    								PICLThread threadContext,
    								IMarker marker,
    								ViewInformation viewInformation,
    								String expression) {
        super(debugTarget);
        fThreadContext = threadContext;
		fMarker = marker;
		fViewInformation = viewInformation;
		fExpression = expression;

    }

    /**
     * @see PICLEngineRequest#execute()
     */
    public void execute() throws PICLException {

		beginRequest();
		Location loc = null;

		// try to get a viewFile from marker

		ViewFile viewFile = fDebugTarget.getViewFile(fMarker, fViewInformation);

		if (viewFile == null) {   // file not found using marker
			// check to see if this is a project marker.  If it is then use the current location in the thread.
			if (fMarker.getResource() instanceof IProject) {
				try {
					ViewInformation vi = ((PICLStackFrame)fThreadContext.getTopStackFrame()).findSupportedViewInformation();
					loc = fThreadContext.getDebuggeeThread().currentLocationWithinView(vi);
				} catch(IOException ioe) {}
			}
		} else {
			loc = new Location(viewFile, fDebugTarget.getDebugPlugin().getBreakpointManager().getLineNumber(fMarker));
		}

		if (loc == null) {
            endRequest();
            throw new PICLException(PICLUtils.getResourceString(msgKey + "file_not_found"));
        }

		boolean rc = true;

		try {
			rc = fThreadContext.getDebuggeeThread().monitorExpression(loc,fExpression,syncRequest());
			// if ok then get monitored expression
			if (rc && !isError())
				fMonitorResult = new PICLVariable(fThreadContext,fMonitoredExpression);
			else
				throw(new PICLException(PICLUtils.getResourceString(msgKey + "send_error")));
		}
		catch(IOException ioe) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "send_error")));
		} finally {
			endRequest();
		}
    }


	/**
	 * Gets the monitorResult
	 * @return Returns a PICLVariable
	 */
	public PICLVariable getMonitorResult() {
		return fMonitorResult;
	}
    /**
     * Sets the monitoredExpression
     * @param monitoredExpression The monitoredExpression to set
     */
    public void setMonitoredExpression(MonitoredExpression monitoredExpression) {
        fMonitoredExpression = monitoredExpression;
    }

}

