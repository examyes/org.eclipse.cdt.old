package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/MonitorRegisterGroupEndRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.6 (last modified 11/28/01 15:59:49)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.IOException;


public class MonitorRegisterGroupEndRequest extends MonitorRequest {

	private PICLRegisterGroup fRegisterGroup = null;

    /**
     * Constructor for MonitorRegisterGroupEndRequest
     */
    public MonitorRegisterGroupEndRequest(PICLDebugTarget debugTarget, PICLRegisterGroup registerGroup) {
        super(debugTarget);
        fRegisterGroup = registerGroup;
    }

    /**
     * @see PICLEngineRequest#execute()
     */
    public void execute() throws PICLException {

    	beginRequest();

    	boolean rc = true;

    	try {
    		rc = fRegisterGroup.getMonitoredRegisterGroup().stopMonitoring(syncRequest());
            if (!rc)
 	      		throw new PICLException(PICLUtils.getResourceString(msgKey + "send_error"));
    	} catch(IOException ioe) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "send_error")));
    	} finally {
	    	endRequest();
    	}

    }

}

