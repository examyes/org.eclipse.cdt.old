package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/StepReturnRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.6 (last modified 11/28/01 15:59:25)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.ViewInformation;
import java.io.IOException;

/**
 * Represents a request to step return
 */

public class StepReturnRequest extends StepRequest {

    /**
     * Constructor for StepReturnRequest
     */
    public StepReturnRequest(PICLDebugTarget debugTarget, PICLThread threadContext, ViewInformation viewInformation) {
        super(debugTarget, threadContext, viewInformation);
    }

    /**
     * @see PICLEngineRequest#execute()
     */
    public void execute() throws PICLException {
    	beginRequest();

    	try {
	    	fThreadContext.fDebuggeeThread.stepReturn(fViewInformation,asyncRequest(), this);
    	} catch(IOException ioe) {
			throw new PICLException(PICLUtils.getResourceString(msgKey + "send_error"));
    	}

    	// NOTE: there is no call to endRequest() because this request is not complete yet...  At some time later the
    	//       model will fire an event that represents the end of this request.

    }

}

