package com.ibm.debug.internal.pdt.ui.dialogs;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/internal/pdt/ui/dialogs/ISettingsWriter.java, eclipse, eclipse-dev, 20011128
// Version 1.3 (last modified 11/28/01 15:59:11)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

public interface ISettingsWriter {

	/** Method that will trigger settings to be written to properties file.
	 * For example, pages should write any information that needs to be restored in a text field
	 * the next time the dialog is used.
	 * Especially useful when Wizards need to tell their pages to write their settings when
	 * performFinish() has been called on the Wizard.
	 */
	public void writeSettings();

}

