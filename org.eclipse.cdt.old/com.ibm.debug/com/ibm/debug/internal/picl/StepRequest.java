package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/StepRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 15:59:26)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.ViewInformation;

/**
 * All requests to step the debuggee inherit from this class
 */

public abstract class StepRequest extends ProgramControlRequest {

	protected PICLThread fThreadContext = null;
	protected ViewInformation fViewInformation = null;

    /**
     * Constructor for StepRequest
     */
    public StepRequest(PICLDebugTarget debugTarget, PICLThread threadContext, ViewInformation viewInformation) {
        super(debugTarget);
        fThreadContext = threadContext;
        fViewInformation = viewInformation;
    }

}

