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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

public class BuildSystemBlock {
	
	String[][] testBuildSystems = new String[][] {
			{ "make", "User provides Makefile that guides the build." },
			{ "managed make", "CDT generates the Makefile for the build." },
			{ "autoconf", "Use the GNU autoconf build system." },
			{ "other", "User specifies which external build system to use." }
	};

	public BuildSystemBlock(Composite parent) {
		Group buildSystemGroup = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		buildSystemGroup.setLayout(layout);
		buildSystemGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		buildSystemGroup.setText("Build System");
		
		final List buildSystems = new List(buildSystemGroup, SWT.SINGLE | SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		buildSystems.setLayoutData(layoutData);
		
		final Label label = new Label(buildSystemGroup, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		for (String[] system : testBuildSystems) {
			buildSystems.add(system[0]);
		}
		
		buildSystems.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = buildSystems.getSelectionIndex();
				label.setText(testBuildSystems[i][1]);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		buildSystems.setSelection(0);
		label.setText(testBuildSystems[0][1]);
	}
}
