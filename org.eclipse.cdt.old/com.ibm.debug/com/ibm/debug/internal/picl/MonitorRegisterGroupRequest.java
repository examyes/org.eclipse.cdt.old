package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/MonitorRegisterGroupRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.7 (last modified 11/28/01 15:59:37)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.DebuggeeThread;
import com.ibm.debug.model.MonitoredRegisterGroup;
import java.io.IOException;

/**
 * Request to monitor a register group.   The groups are defined by the engine.  The result
 * is that all the registers in the group are monitored
 */

public class MonitorRegisterGroupRequest extends MonitorRequest {

	PICLRegisterGroup fRegisterGroup = null;
	PICLThread fThread = null;
	MonitoredRegisterGroup fMonitoredRegisterGroup = null;


    /**
     * Constructor for MonitorRegisterGroupRequest
     */
    public MonitorRegisterGroupRequest(PICLDebugTarget debugTarget,
    									PICLThread debuggeeThread,
    									PICLRegisterGroup registerGroup) {
        super(debugTarget);
		fThread = debuggeeThread;
        fRegisterGroup = registerGroup;
    }

    /**
     * @see PICLEngineRequest#execute()
     */
    public void execute() throws PICLException {

		DebuggeeThread threadToMonitor = fThread.getDebuggeeThread();

		beginRequest();

		boolean rc = true;

		try {
			rc = threadToMonitor.monitorRegisterGroup(fRegisterGroup.getRegisterGroup(), syncRequest());
            if (!rc)
 	      		throw new PICLException(PICLUtils.getResourceString(msgKey + "send_error"));
    	} catch(IOException ioe) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "send_error")));
    	} finally {
	    	endRequest();
    	}

    }

	/**
	 * Gets the monitoredRegisterGroup
	 * @return Returns a MonitoredRegisterGroup
	 */
	public MonitoredRegisterGroup getMonitoredRegisterGroup() {
		return fMonitoredRegisterGroup;
	}
    /**
     * Sets the monitoredRegisterGroup
     * @param monitoredRegisterGroup The monitoredRegisterGroup to set
     */
    public void setMonitoredRegisterGroup(MonitoredRegisterGroup monitoredRegisterGroup) {
        fMonitoredRegisterGroup = monitoredRegisterGroup;
    }

}

