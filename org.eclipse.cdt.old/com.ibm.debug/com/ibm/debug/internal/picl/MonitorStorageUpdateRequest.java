package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/MonitorStorageUpdateRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:01:01)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.Storage;
import com.ibm.debug.model.StorageColumn;
import java.io.IOException;

public class MonitorStorageUpdateRequest extends MonitorRequest {

	private StorageColumn fStorageColumn = null;
	private String fNewValue = null;

	/**
	 * Constructor for MonitorStorageUpdateRequest
	 */
	public MonitorStorageUpdateRequest(PICLDebugTarget debugTarget, StorageColumn column, String newValue) {
		super(debugTarget);

		fStorageColumn = column;
		fNewValue = newValue;
	}

	/**
	 * @see PICLEngineRequest#execute()
	 */
	public void execute() throws PICLException {

		beginRequest();

		boolean rc = true;

		try {
			rc = fStorageColumn.update(fNewValue,syncRequest());
            if (!rc)
 	      		throw new PICLException(PICLUtils.getResourceString(msgKey + "send_error"));
    	} catch(IOException ioe) {
			throw(new PICLException(PICLUtils.getResourceString(msgKey + "send_error")));
    	} finally {
	    	endRequest();
    	}

	}

}

