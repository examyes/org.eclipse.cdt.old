package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/IPICLPreferencesConstants.java, eclipse, eclipse-dev, 20011128
// Version 1.5 (last modified 11/28/01 15:57:54)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * Defines constants which are used to refer to values in the plugin's preference bundle.
 */

public interface IPICLPreferencesConstants {


	// keys
	static final String ATTACH_LISTENER_PORT= "com.ibm.debug.attacherlaunch.port";
	static final String PGM_NAME= "com.ibm.debug.launcher.pgmname";
	static final String PGM_PARMS= "com.ibm.debug.launcher.pgmparms";
}
