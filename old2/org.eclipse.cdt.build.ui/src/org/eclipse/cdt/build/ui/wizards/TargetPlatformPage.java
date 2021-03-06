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

import org.eclipse.cdt.internal.build.ui.Activator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Doug Schaefer
 */
public class TargetPlatformPage extends WizardPage {

	public TargetPlatformPage() {
		super("Target Platform");
	}

	@Override
	public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        
		try {
			new TargetPlatformBlock(composite);
		} catch (CoreException e) {
			Activator.getService(ILog.class).log(e.getStatus());
		}
		
        setControl(composite);
        
		setTitle("Select Target Platform");
		setMessage("Specify the initial target platform for this project.");
	}

}
