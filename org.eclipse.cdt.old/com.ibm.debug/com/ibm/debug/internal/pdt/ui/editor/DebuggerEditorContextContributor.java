package com.ibm.debug.internal.pdt.ui.editor;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/editor/DebuggerEditorContextContributor.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 16:01:15)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

import com.ibm.lpex.alef.LpexContextContributor;

/**
 * This class provides editor actions like print and cut & paste,
 * all of which is inherited from LpexContextContributor.
 */

public class DebuggerEditorContextContributor extends LpexContextContributor {

	/**
	 * Constructor for DebuggerEditorContextContributor
	 */
	public DebuggerEditorContextContributor() {
		super();
	}
}