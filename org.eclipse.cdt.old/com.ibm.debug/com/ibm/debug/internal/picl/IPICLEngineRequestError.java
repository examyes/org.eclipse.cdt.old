package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/IPICLEngineRequestError.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 15:59:09)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.ErrorOccurredEvent;

/**
 * This interface must be implemented by all requests to the model.
 * It allows for error feedback on a request to the debug engine
 */
public interface IPICLEngineRequestError {

	/**
	 * Updates the request with any errors that occured as a result of the request
	 * @param The error event that the model returns from the debug engine
	 */
	public abstract void setError(ErrorOccurredEvent errorEvent);

}

