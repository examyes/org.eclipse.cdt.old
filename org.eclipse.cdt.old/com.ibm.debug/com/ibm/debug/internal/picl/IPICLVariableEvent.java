package com.ibm.debug.internal.picl;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/picl/IPICLVariableEvent.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 15:59:29)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

/**
 * PICLVariable will trigger the events that represent changes to it
 */

public interface IPICLVariableEvent {

	/**
	 * Called whenever anything to do with the passed PICLVariable has changed.
	 * This includes changes to its representation or contents
	 */
	public void monitoredExpressionChanged(PICLVariable changedVariable);


	/**
	 * Called whenever this monitored expression is deleted
	 */
	public void monitoredExpressionDeleted(PICLVariable deletedVariable);

}

