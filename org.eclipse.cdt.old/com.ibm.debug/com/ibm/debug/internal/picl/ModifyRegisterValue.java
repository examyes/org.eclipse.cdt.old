package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/ModifyRegisterValue.java, eclipse, eclipse-dev, 20011128
// Version 1.9 (last modified 11/28/01 15:59:48)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.IOException;

import org.eclipse.debug.core.DebugEvent;


public class ModifyRegisterValue extends MonitorRequest {

	private PICLRegister fRegister = null;
	private String fNewValue = null;

    /**
     * Constructor for ModifyRegisterValue
     * @param The current debug target
     * @param register to be changed
     * @param String that represents the new value
     */
    public ModifyRegisterValue(PICLDebugTarget debugTarget, PICLRegister register, String newValue) {
        super(debugTarget);
        fRegister = register;
        fNewValue = newValue;
    }

    /**
     * @see PICLEngineRequest#execute()
     */
    public void execute() throws PICLException {

    	beginRequest();

    	boolean rc = true;

    	try {
    		rc = fRegister.getMonitoredRegister().modifyValue(fNewValue,syncRequest());

            if (!rc)
 	      		throw new PICLException(PICLUtils.getResourceString(msgKey + "send_error"));
    	} catch(IOException ioe) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "send_error")));
    	} finally {
	    	endRequest();
    	}

    }

}

