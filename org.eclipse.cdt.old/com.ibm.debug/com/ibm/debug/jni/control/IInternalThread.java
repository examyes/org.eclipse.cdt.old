package com.ibm.debug.jni.control;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/jni/control/IInternalThread.java, eclipse-jni, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 16:38:02)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import org.eclipse.debug.core.DebugException;

/**
 * Interface to be implemented by threads that want to support internal
 * control.
 */
public interface IInternalThread {

    /**
     * Get the value of an integer variable in the debuggee.
     * @param variable The name of the variable to be read.
     * @return The integer value of the variable.
     */
    public int readIntVariable(String variable) throws DebugException;

    /**
     * Set the value of an integer variable in the debuggee.
     * @param variable The name of the variable to be written.
     * @param value The integer value to write.
     */
    public void writeVariable(String variable, int value) throws DebugException;

    /**
     * Get the value of a string variable in the debuggee.
     * @param variable The name of the variable to be read.
     * @return The string value of the variable.
     */
    public String readStringVariable(String variable) throws DebugException;

    /**
     * Set the value of a string variable in the debuggee.
     * @param variable The name of the variable to be written.
     * @param value The string value to write.
     */
    public void writeVariable(String variable, String value) throws DebugException;

    /**
     * Request a step into.
     */
    public void internalStepInto() throws DebugException;

    /**
     * Request a step over.
     */
    public void internalStepOver() throws DebugException;

    /**
     * Request a step return.
     */
    public void internalStepReturn() throws DebugException;
}
