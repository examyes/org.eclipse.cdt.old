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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;

public class TargetPlatformBlock {

	private String[] testTargetPlatforms = {
			"Windows MinGW",
			"Windows Cygwin",
			"Linux GCC",
			"Cross Target GCC",
			"Wind River vxWorks"
	};
	
	public TargetPlatformBlock(Composite parent) {
		Group targetPlatformGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		targetPlatformGroup.setLayout(layout);
		targetPlatformGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		targetPlatformGroup.setText("Target Platform");
		
		final List targetPlatforms = new List(targetPlatformGroup, SWT.SINGLE | SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		targetPlatforms.setLayoutData(layoutData);

		for (String targetPlatform : testTargetPlatforms) {
			targetPlatforms.add(targetPlatform);
		}
		
		targetPlatforms.setSelection(0);
	}
}
