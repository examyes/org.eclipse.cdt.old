package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/MonitorStorageDeleteRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 16:00:38)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.Storage;
import java.io.IOException;

public class MonitorStorageDeleteRequest extends MonitorRequest {

	private PICLStorage fPICLStorage = null;

	/**
	 * Constructor for MonitorStorageDeleteRequest
	 */
	public MonitorStorageDeleteRequest(PICLDebugTarget debugTarget, PICLStorage storage) {
		super(debugTarget);

		fPICLStorage = storage;
	}

	/**
	 * @see PICLEngineRequest#execute()
	 */
	public void execute() throws PICLException {

		beginRequest();

		Storage storage = fPICLStorage.getStorage();

		boolean rc = true;

		try {
			rc = storage.remove(syncRequest());
            if (!rc)
 	      		throw new PICLException(PICLUtils.getResourceString(msgKey + "send_error"));
    	} catch(IOException ioe) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "send_error")));
    	} finally {
	    	endRequest();
    	}

	}

}

