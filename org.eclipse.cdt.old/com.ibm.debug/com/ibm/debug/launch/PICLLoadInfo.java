package com.ibm.debug.launch;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/launch/PICLLoadInfo.java, eclipse, eclipse-dev, 20011128
// Version 1.4 (last modified 11/28/01 16:00:09)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * This class is used to provide startup information for a debug
 * session where the program will be loaded by the debugger.
 * This class is not intended to be subclassed.
 */

public class PICLLoadInfo extends PICLStartupInfo {

    /**
     * Used to specify the debugger startup behaviour.
     * DEBUG_INITIALIZATION indicates that the debugger should stop
     *                      in the initialization code.
     * RUN_TO_MAIN indicates that the debugger should stop at main.
     * RUN_TO_BREAKPOINT indicates that the debugger should run to the
     *                   first breakpoints it hits.  If no breakpoints
     *                   are hit or exceptions caught it will run to
     *                   completion.
     * @see #setStartupBehaviour
     */
    public final static int DEBUG_INITIALIZATION = 0;
    public final static int RUN_TO_MAIN = 1;
    public final static int RUN_TO_BREAKPOINT = 2;

    /**
     * For internal use by PICL Debug Plugin only.
     */
    public final static int ENV_COMMAND_WINDOW = 0;
    public final static int ENV_PREVIOUS_SESSION = 1;
    public final static int ENV_DEBUGGER_STARTUP = 2;

    private String fProgramName;
    private String fProgramParms;
    private int fStartupBehaviour = RUN_TO_MAIN;

    /**
     * Sets the program name.  This is the name of the program to be
     * loaded by the debugger.  The program name must be set for a
     * successful launch.
     * @param programName The program name
     */
    public void setProgramName(String programName) {
    	fProgramName = programName;
    }

    /**
     * Gets the program name
     * @return Returns a String
     */
    public String getProgramName() {
    	return fProgramName;
    }

    /**
     * Sets the program parameters.  These are the parameters that are
     * to be passed to the program specified using setProgramName.
     * @param programParms The program parameters
     */
    public void setProgramParms(String programParms) {
    	fProgramParms = programParms;
    }

    /**
     * Gets the program parameters
     * @return Returns a String
     */
    public String getProgramParms() {
    	return fProgramParms;
    }

    /**
     * Sets the startup behaviour for the launch.  The default is
     * RUN_TO_MAIN.
     * @param startupBehaviour The startup behaviour.  Must be one
     *        of DEBUG_INITIALIZATION, RUN_TO_MAIN, RUN_TO_BREAKPOINT.
     * @see #DEBUG_INITIALIZATION
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
