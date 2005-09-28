/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.pdom.ui;

import org.eclipse.cdt.ui.index.AbstractIndexerPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMIndexerUI extends AbstractIndexerPage {

	public void loadPreferences() {
	}

	public void removePreferences() {
	}

	public void createControl(Composite parent) {
	    Composite comp = new Composite(parent, SWT.NULL);
        setControl(comp);
	}

	public void performApply(IProgressMonitor monitor) throws CoreException {
	}

	public void performDefaults() {
	}

	public void initialize(IProject currentProject) {
	}
	
}
