package com.ibm.debug.launch;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/launch/IPICLLaunchWizard.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 16:00:17)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.Hashtable;
import org.eclipse.debug.ui.ILaunchWizard;

/**
 * This interface should be implemented by PICL Debug launcher wizards.
 * Launcher wizards that implement this interface may be invoked by the
 * PICL Debug Plugin if it receives an engine connection but is
 * not passed a startup key and does not have enough information
 * to launch a debug session.
 */
public interface IPICLLaunchWizard extends ILaunchWizard {

    /**
     * Initialize the wizard.
     * @param connectionKey Key which must be passed back when
     *                      PICLDebugPlugin.launchDebugSession is called.
     * @param startupInfo This is the startup info created from the
     *                    parameters passed to the daemon from the engine.
     *                    If from the parameters there is no indication
     *                    of whether the user wants load or attach, then
     *                    a PICLLoadInfo object will be passed.
     * @param pairs This is the set of property=value pairs that were
     *              sent to the daemon from the engine.  If the old style
     *              daemon conversation was used this will be null.
     * @see com.ibm.debug.PICLDebugPlugin#launchDebugSession
     */
    public void init(Object connectionKey, PICLStartupInfo startupInfo, Hashtable pairs);

}
