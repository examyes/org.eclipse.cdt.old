package com.ibm.debug.launch;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/launch/PICLAttachInfo.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:00:08)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.debug.model.SystemProcess;

/**
 * This class is used to provide startup information for a debug
 * session where the debugger will attach to a running process.
 * This class is not intended to be subclassed.
 */

public class PICLAttachInfo extends PICLStartupInfo {

    /**
     * Used to specify the debugger startup behaviour.
     * STOP indicates that the debugger should stop where it attached.
     * RUN indicates that the debugger should run until it hits a breakpoint,
     *     or an exception, or the program runs to completion.
     * @see setStartupBehaviour
     */
    public final static int STOP = 0;
    public final static int RUN = 1;

    private String fProcessID;
    private SystemProcess fProcess;
    private String fProcessPath;
    private int fStartupBehaviour;

    /**
     * Sets the process id.  This is the id of the process the debugger
     * will attach to.  The process id must be set for a successful
     * launch.
     * @param processID The process id
     */
    public void setProcessID(String processID) {
    	fProcessID = processID;
    }

    /**
     * Gets the process id
     * @return Returns the process id as a String
     */
    public String getProcessID() {
    	return fProcessID;
    }

    /**
     * Sets the system process.  For internal use of PICL Debug Plugin only.
     * @param process The system process
     */
    public void setProcess(SystemProcess process) {
        fProcess = process;
    }

    /**
     * Gets the system process.  For internal use of PICL Debug Plugin only.
     * @returns The system process
     */
    public SystemProcess getProcess() {
        return fProcess;
    }

    /**
     * Sets the process path.  This is the path of the executable that
     * was used to launch the process that the debugger will attach
     * to.  This is only needed for AIX where the path of the executable
     * cannot be determined from the process image.
     * @param processPath The process path
     */
    public void setProcessPath(String processPath) {
    	fProcessPath = processPath;
    }

    /**
     * Gets the process path
     * @return Returns the process path as a String
     */
    public String getProcessPath() {
    	return fProcessPath;
    }

    /**
     * Sets the startup behaviour for the launch.  The default is STOP.
     * @param startupBehaviour The startup behaviour.  Must be one
     *        of STOP, RUN.
     * @see #STOP
     */
    public void setStartupBehaviour(int startupBehaviour) {
        fStartupBehaviour = startupBehaviour;
    }

    /**
     * Gets the startup behaviour
     * @return Returns the startup behaviour
     */
    public int getStartupBehaviour() {
        return fStartupBehaviour;
    }
}
