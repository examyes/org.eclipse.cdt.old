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

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @author Doug Schaefer
 *
 */
public class SQLPDOMIndexTU extends SQLPDOMIndexJob {

	private final ITranslationUnit tu;
	
	public SQLPDOMIndexTU(IProgressMonitor group, ITranslationUnit tu) {
		super("SQL PDOM Index " + tu.getElementName(), group);
		this.tu = tu;
	}

	protected IStatus run(IProgressMonitor monitor) {
		monitor.worked(1);
		return Status.OK_STATUS;
	}

}
