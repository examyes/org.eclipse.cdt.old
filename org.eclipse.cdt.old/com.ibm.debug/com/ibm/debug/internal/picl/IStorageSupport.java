package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/IStorageSupport.java, eclipse, eclipse-dev, 20011128
// Version 1.6 (last modified 11/28/01 16:01:22)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * <code>IStorageSupport</code> defines functionality for
 * debug targets supporting storage. The storage
 * view will only appear if the target returns <code>true</code>
 * from <code>supportsStorage</code>.
 */

import java.util.Vector;

public interface IStorageSupport {

	/**
	 * Returns <code>true</code> if this target supports monitoring
	 * storage, otherwise <code>false</code>. The default value should be false.
	 * This method will control the ability to monitor specific blocks
	 * of storage.
	 */
	boolean supportsStorageMonitors();

	/**
	 * Returns <code>true</code> if this target supports monitoring
	 * storage, otherwise <code>false</code>. The default value should be false.
	 * This method will control the ability to map storage
	 * in the mapping view.
	 */
	boolean supportsStorageMapping();
}
