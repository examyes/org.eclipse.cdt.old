package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/MonitorExpressionDisableRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 16:00:30)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.MonitoredExpression;
import java.io.IOException;


public class MonitorExpressionDisableRequest extends MonitorRequest {

	private PICLVariable fPICLVariable = null;

	/**
	 * Constructor for MonitorExpressionDisableRequest
	 */
	public MonitorExpressionDisableRequest(PICLDebugTarget debugTarget,
										  PICLVariable piclVariable) {
		super(debugTarget);
		fPICLVariable = piclVariable;
	}

	/**
	 * @see PICLEngineRequest#execute()
	 */
	public void execute() throws PICLException {

		beginRequest();

		MonitoredExpression mon = fPICLVariable.getMonitoredExpression();

		boolean rc = true;

		try {
			rc = mon.disable(syncRequest());
            if (!rc)
 	      		throw new PICLException(PICLUtils.getResourceString(msgKey + "send_error"));
    	} catch(IOException ioe) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "send_error")));
    	} finally {
	    	endRequest();
    	}
	}

}

