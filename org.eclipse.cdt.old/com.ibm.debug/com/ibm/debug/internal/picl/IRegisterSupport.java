package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/IRegisterSupport.java, eclipse, eclipse-dev, 20011128
// Version 1.7 (last modified 11/28/01 16:01:19)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import java.util.Vector;

/**
 * <code>IRegisterSupport</code> defines functionality for
 * debug targets supporting registers. The register manager
 * automatically adds debug targets as register listeners,
 * as launches are registered. The registers view will only appear
 * if the target returns <code>true</code>
 * from <code>supportsRegisters</code>.
 */
public interface IRegisterSupport {

	/**
	 * Returns <code>true</code> if this target supports
	 * registers, otherwise <code>false</code>.
	 */
	boolean supportsRegisters();

}

