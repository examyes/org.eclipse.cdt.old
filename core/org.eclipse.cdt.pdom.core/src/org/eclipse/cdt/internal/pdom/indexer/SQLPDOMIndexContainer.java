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
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Doug Schaefer
 *
 */
public class SQLPDOMIndexContainer extends SQLPDOMIndexJob {

	private final ICContainer container;
	
	public SQLPDOMIndexContainer(IProgressMonitor group, ICContainer container) {
		super("SQL PDOM Index " + container.getElementName(), group);
		this.container = container;
	}

	protected IStatus run(IProgressMonitor monitor) {
		try {
			ICContainer[] subcontainers = container.getCContainers();
			for (int i = 0; i < subcontainers.length; ++i) {
				Job subjob = new SQLPDOMIndexContainer(group, subcontainers[i]);
				subjob.setRule(getRule());
				subjob.schedule();
			}
			
			ITranslationUnit[] tus = container.getTranslationUnits();
			for (int i = 0; i < tus.length; ++i) {
				Job subjob = new SQLPDOMIndexTU(group, tus[i]);
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
