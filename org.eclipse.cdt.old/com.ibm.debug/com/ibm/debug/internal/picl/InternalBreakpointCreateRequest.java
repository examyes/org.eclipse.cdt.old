package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/InternalBreakpointCreateRequest.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:01:09)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.ErrorOccurredEvent;

abstract class InternalBreakpointCreateRequest extends BreakpointRequest {

    private Object fRequestProperty;

    private String fThread = "0";

    private int fEveryValue = 0;
    private int fToValue = 0;
    private int fFromValue = 0;

    private int fViewNumber = 1;  // default to view 1

    private boolean fEnabled = true;

    InternalBreakpointCreateRequest(PICLDebugTarget debugTarget, Object requestProperty) {
        super(debugTarget);
        fRequestProperty = requestProperty;
    }

    /**
     * Sets the breakpoint
     * @return returns true if request to set breakpoint was successful.   This does not mean that the breakpoint
     * was actually set.
     */
    protected abstract boolean setBreakpoint() throws PICLException;

    /**
     * executes the set breakpoint request
     * @return true if set breakpoint was successful
     */
    public void execute() throws PICLException {

        beginRequest();

        try {
            setBreakpoint();
        } finally {
            endRequest();
        }
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
     * Gets the breakpoint id
     * @return breakpoint id as Object
     */
    protected Object getRequestProperty() {
        return fRequestProperty;
    }

    /**
     * Gets the thread
     * @return Returns a String
     */
    protected String getThread() {
        return fThread;
    }

    /**
     * Sets the thread
     * @param thread The thread to set
     */
    protected void setThread(String thread) {
        fThread = thread;
    }

    /**
     * Gets the enabled
     * @return Returns a boolean
     */
    protected boolean getEnabled() {
        return fEnabled;
    }

    /**
     * Sets the enabled
     * @param enabled The enabled to set
     */
    protected void setEnabled(boolean enabled) {
        fEnabled = enabled;
    }

    /**
     * Gets the everyValue
     * @return Returns a int
     */
    protected int getEveryValue() {
        return fEveryValue;
    }

    /**
     * Sets the everyValue
     * @param everyValue The everyValue to set
     */
    protected void setEveryValue(int everyValue) {
        fEveryValue = everyValue;
    }

    /**
     * Gets the fromValue
     * @return Returns a int
     */
    protected int getFromValue() {
        return fFromValue;
    }

    /**
     * Sets the fromValue
     * @param fromValue The fromValue to set
     */
    protected void setFromValue(int fromValue) {
        fFromValue = fromValue;
    }

    /**
     * Gets the toValue
     * @return Returns a int
     */
    protected int getToValue() {
        return fToValue;
    }

    /**
     * Sets the toValue
     * @param toValue The toValue to set
     */
    protected void setToValue(int toValue) {
        fToValue = toValue;
    }

    /**
     * Gets the thread ID as a number
     * @return thread ID (returns 0 if invalid)
     */
    protected int getThreadAsNumber() {

        int threadAsNumber = 0;
        try {
            threadAsNumber = Integer.parseInt(getThread());   // try to get a number from the thread passed in
        } catch(NumberFormatException e) {}

        return threadAsNumber;
    }

    /**
     * @see IPICLEngineRequestError#setError(ErrorOccurredEvent)
     * Handle errors when setting breakpoints by updating the attributes of the marker with the error message
     * and the attribute in error if available.
     */
    public void setError(ErrorOccurredEvent errorEvent) {
    }

}
