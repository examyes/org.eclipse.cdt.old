/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.build.ui.wizards;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public class NewCDTProjectPage extends WizardNewProjectCreationPage {

	public NewCDTProjectPage() {
		super("New CDT Project");
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		setTitle("CDT Project");
		setDescription("Specify name and location for this project.");
	}
	
}
