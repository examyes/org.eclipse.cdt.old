package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/RunRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 15:59:21)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.IOException;

/**
 * Request to run the debuggee
 * This is an asynchronous request
 */

public class RunRequest extends ProgramControlRequest {

    /**
     * Constructor for RunRequest
     */
    public RunRequest(PICLDebugTarget debugTarget) {
        super(debugTarget);
    }

    /**
     * @see PICLEngineRequest#execute()
     */
    public void execute() throws PICLException {

    	beginRequest();

    	try {
	    	fDebugTarget.getDebuggeeProcess().run(asyncRequest());
    	} catch(IOException ioe) {
			throw new PICLException(PICLUtils.getResourceString(msgKey + "send_error"));
    	}

    	// NOTE: there is no call to endRequest() because this request is not complete yet...  At some time later the
    	//       model will fire an event that represents the end of this request.

    }

}

