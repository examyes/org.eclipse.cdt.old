package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/EventBreakpointRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 15:58:51)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.DebugException;
public abstract class EventBreakpointRequest extends BreakpointCreateRequest {

    /**
     * Constructor for EventBreakpointRequest
     */
    EventBreakpointRequest(IMarker marker, PICLDebugTarget debugTarget)
        throws DebugException {
        super(marker, debugTarget);

    }

}
