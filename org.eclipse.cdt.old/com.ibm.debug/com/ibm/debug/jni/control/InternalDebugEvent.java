package com.ibm.debug.jni.control;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/jni/control/InternalDebugEvent.java, eclipse-jni, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:38:04)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * Abstract class for internal debug events.
 */

public abstract class InternalDebugEvent extends java.util.EventObject {

    /**
     * Constructor for internal debug events.
     * @param source The source of the event.
     */
    public InternalDebugEvent(Object source) {
        super(source);
    }

    /**
     * Fire the event to the given listener.
     * @param listener The listener to receive the event.
     */
    public abstract void fireEvent(IInternalEventListener listener);

}
