package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/InternalEntryBreakpointRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:01:10)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.io.IOException;

class InternalEntryBreakpointRequest extends InternalBreakpointCreateRequest {

    private String fEntryPointName = null;
    private String fModuleName = null;
    private String fPartName = null;

    InternalEntryBreakpointRequest(String entryPointName, String moduleName, String partName, PICLDebugTarget debugTarget, Object requestProperty) {
        super(debugTarget, requestProperty);

        fEntryPointName = entryPointName;
        fModuleName = moduleName;
        fPartName = partName;
    }

    /**
     * Sets the breakpoint
     * @return returns true if request to set breakpoint was successful.   This does not mean that the breakpoint
     * was actually set.
     */
    public boolean setBreakpoint() throws PICLException {
        boolean rc = false;

        try {
            rc = getDebugTarget().getDebuggeeProcess().setDeferredEntryBreakpoint(
                                                           getEnabled(),
                                                           getEntryPointName(),
                                                           getModuleName(),
                                                           getPartName(),
                                                           getThreadAsNumber(),
                                                           getEveryValue(), getFromValue(), getToValue(),
                                                           null,
                                                           syncRequest(),
                                                           getRequestProperty());
        } catch (IOException e) {
            throw new PICLException(PICLUtils.getResourceString(super.msgKey + "sendError"));
        }

        return rc;
    }

    /**
     * remoteExtension(String)
     * @param File name that will have extension removed.
     * @return Returns string minus extension
     */
    protected String removeExtension(String name) {
        int posn = name.lastIndexOf('.');
        if (posn > 0)
            return name.substring(0,posn);
        else
            return name;
    }

    /**
     * Gets the entry point name
     * @return Returns a String
     */
    protected String getEntryPointName() {
        return fEntryPointName;
    }

    /**
     * Gets the module name
     * @return Returns a String
     */
    protected String getModuleName() {
        return fModuleName;
    }

    /**
     * Gets the part name
     * @return Returns a String
     */
    protected String getPartName() {
        return fPartName;
    }

}
