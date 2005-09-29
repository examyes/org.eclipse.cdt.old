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
package org.eclipse.cdt.internal.pdom.indexer;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Doug Schaefer
 *
 */
public class SQLPDOMIndexProject extends SQLPDOMIndexJob {

	private final ICProject project;
	
	public SQLPDOMIndexProject(IProgressMonitor group, ICProject project) {
		super("SQL PDOM Index Project: " + project.getElementName(), group);
		this.project = project;
	}

	protected IStatus run(IProgressMonitor monitor) {
		try {
			ISourceRoot[] sourceRoots = project.getAllSourceRoots();
			for (int i = 0; i < sourceRoots.length; ++i) {
				Job subjob = new SQLPDOMIndexContainer(group, sourceRoots[i]);
				subjob.setRule(getRule());
				subjob.schedule();
			}
			monitor.worked(1);
			return Status.OK_STATUS;
		} catch (CModelException e) {
			monitor.worked(1);
			return e.getStatus();
		}
	}

}
