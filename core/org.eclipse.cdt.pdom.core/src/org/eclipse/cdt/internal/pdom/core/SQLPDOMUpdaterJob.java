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
package org.eclipse.cdt.internal.pdom.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.PDOM;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Doug Schaefer
 *
 */
public class SQLPDOMUpdaterJob extends Job {

	private SQLPDOMUpdaterJob prevJob;
	private ICElementDelta delta;
	private List addedTUs;
	private List changedTUs;
	private List removedTUs;
	
	public SQLPDOMUpdaterJob(ICElementDelta delta, SQLPDOMUpdaterJob prevJob) {
		super("SQL PDOM Updater");
		this.prevJob = prevJob;
		this.delta = delta;
	}

	protected IStatus run(IProgressMonitor monitor) {
		if (prevJob != null)
			try {
				prevJob.join();
			} catch (InterruptedException e) {
			}
			
		processDelta(delta);
		
		if (addedTUs != null)
			for (Iterator i = addedTUs.iterator(); i.hasNext();) {
				ITranslationUnit tu = (ITranslationUnit)i.next();
				processAddedTU(tu);
			}
		
		if (changedTUs != null)
			for (Iterator i = changedTUs.iterator(); i.hasNext();) {
				ITranslationUnit tu = (ITranslationUnit)i.next();
				processChangedTU(tu);
			}
		
		if (removedTUs != null)
			for (Iterator i = removedTUs.iterator(); i.hasNext();) {
				ITranslationUnit tu = (ITranslationUnit)i.next();
				processRemovedTU(tu);
			}
		
		return Status.OK_STATUS;
	}

	private void processDelta(ICElementDelta delta) {
		// process the children first
		ICElementDelta[] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; ++i)
			processDelta(children[i]);

		// what have we got
		ICElement element = delta.getElement();
		if (element.getElementType() != ICElement.C_UNIT)
			// Not a TU, don't care
			return;
		
		ITranslationUnit tu = (ITranslationUnit)element;
		if (tu.isWorkingCopy())
			// Don't care about working copies either
			return;
		
		switch (delta.getKind()) {
		case ICElementDelta.ADDED:
			if (addedTUs == null)
				addedTUs = new LinkedList();
			addedTUs.add(element);
			break;
		case ICElementDelta.CHANGED:
			if (changedTUs == null)
				changedTUs = new LinkedList();
			changedTUs.add(element);
			break;
		case ICElementDelta.REMOVED:
			if (removedTUs == null)
				removedTUs = new LinkedList();
			removedTUs.add(element);
			break;
		}
	}

	private void processAddedTU(ITranslationUnit tu) {
		IASTTranslationUnit ast;
		try {
			ast = CDOM.getInstance().getTranslationUnit((IFile)tu.getResource());
		} catch (IASTServiceProvider.UnsupportedDialectException e) {
			return;
		}
		
		IPDOM pdom = ast.getPDOM();
		if (pdom == null || !(pdom instanceof SQLPDOM))
			return;
		
		SQLPDOM sqlpdom = (SQLPDOM)pdom;
		sqlpdom.addSymbols(ast);
	}

	private void processRemovedTU(ITranslationUnit tu) {
		IProject project = tu.getCProject().getProject();
		IPDOM pdom = PDOM.getPDOM(project);
		if (pdom == null || !(pdom instanceof SQLPDOM))
			return;

		SQLPDOM sqlpdom = (SQLPDOM)pdom;
		sqlpdom.removeSymbols(tu);
	}

	private void processChangedTU(ITranslationUnit tu) {
		IASTTranslationUnit ast;
		try {
			ast = CDOM.getInstance().getTranslationUnit((IFile)tu.getResource());
		} catch (IASTServiceProvider.UnsupportedDialectException e) {
			return;
		}
		
		IPDOM pdom = ast.getPDOM();
		if (pdom == null || !(pdom instanceof SQLPDOM))
			return;
		
		SQLPDOM sqlpdom = (SQLPDOM)pdom;
		sqlpdom.removeSymbols(tu);
		sqlpdom.addSymbols(ast);
	}

}
