package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/MonitorStorageRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.8 (last modified 11/28/01 16:00:35)
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

import com.ibm.debug.epdc.EPDC;
import com.ibm.debug.model.Location;
import com.ibm.debug.model.StorageStyle;
import com.ibm.debug.model.ViewFile;
import com.ibm.debug.model.ViewInformation;



/**
 * Request to start monitoring storage
 */

public class MonitorStorageRequest extends MonitorRequest {

	private PICLThread fEvaluationThread = null;
	private IMarker fMarker = null;
	private ViewInformation fViewInformation = null;
	private String fAddressExpression = null;
	private int fNumberOfBytes = PICLStorage.NUM_BYTES;

	/**
	 * Constructor for MonitorStorageRequest
	 */
	public MonitorStorageRequest(PICLDebugTarget debugTarget,
								 PICLThread evaluationThread,
								 IMarker marker,
								 ViewInformation viewInformation,
								 String addressExpression) {
		super(debugTarget);

		fEvaluationThread = evaluationThread;
		fMarker = marker;
		fViewInformation = viewInformation;
		fAddressExpression = addressExpression;
	}

	/**
	 * Constructor for MonitorStorageRequest
	 */
	public MonitorStorageRequest(PICLDebugTarget debugTarget,
								 PICLThread evaluationThread,
								 IMarker marker,
								 ViewInformation viewInformation,
								 String addressExpression,
								 int numberOfBytes) {

		this(debugTarget, evaluationThread, marker, viewInformation, addressExpression);

		fNumberOfBytes = numberOfBytes;
	}

	/**
	 * @see PICLEngineRequest#execute()
	 */
	public void execute() throws PICLException {

		beginRequest();

		Location loc = null;

		ViewFile viewFile = fDebugTarget.getViewFile(fMarker, fViewInformation);

		if (viewFile == null) {   // file not found using marker
			// check to see if this is a project marker.  If it is then use the current location in the thread.
			if (fMarker.getResource() instanceof IProject) {
				try {
					ViewInformation vi = ((PICLStackFrame)fEvaluationThread.getTopStackFrame()).findSupportedViewInformation();
					loc = fEvaluationThread.getDebuggeeThread().currentLocationWithinView(vi);
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

		int firstOffset = -1;   // get 1 storage line before and after for scrolling
		int lastOffset = 1;

		try {
			rc = fDebugTarget.getDebuggeeProcess().monitorStorage(fAddressExpression,
																  loc,
																  fEvaluationThread.getDebuggeeThread(),
																  firstOffset,   // offset from address is zero
																  lastOffset,  // get # lines
																  StorageStyle.getStorageStyle(EPDC.StorageStyleByteHexCharacter),
																  fNumberOfBytes,
																  true,
																  true,
																  null,
																  null,
																  syncRequest());
            if (!rc)
 	      		throw new PICLException(PICLUtils.getResourceString(msgKey + "monitor_storage_error"));
    	} catch(IOException ioe) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "send_error")));
    	} finally {
	    	endRequest();
    	}
	}

}

