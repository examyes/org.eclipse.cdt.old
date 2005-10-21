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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.PDOM;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.pdom.core.PDOMCorePlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Doug Schaefer
 *
 */
public class SQLPDOMUpdaterJob extends Job {

	private SQLPDOMUpdaterJob prevJob;
	private ICElementDelta delta;
	private ICProject project;
	private List addedTUs;
	private List changedTUs;
	private List removedTUs;
	private int count;
	
	public SQLPDOMUpdaterJob(ICElementDelta delta, SQLPDOMUpdaterJob prevJob) {
		super("SQL PDOM Updater");
		this.prevJob = prevJob;
		this.delta = delta;
	}

	public SQLPDOMUpdaterJob(ICProject project, SQLPDOMUpdaterJob prevJob) {
		super("SQL PDOM Project Updater");
		this.prevJob = prevJob;
		this.project = project;
	}
	
	protected IStatus run(IProgressMonitor monitor) {
		if (prevJob != null)
			try {
				prevJob.join();
			} catch (InterruptedException e) {
			}
			
		long start = System.currentTimeMillis();
		
		if (delta != null)
			processDelta(delta);
		if (project != null)
			processNewProject(project);
		
		if (addedTUs != null)
			for (Iterator i = addedTUs.iterator(); i.hasNext();) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				monitor.subTask("Files remaining: " + (count--));
				ITranslationUnit tu = (ITranslationUnit)i.next();
				processAddedTU(tu);
			}
		
		if (changedTUs != null)
			for (Iterator i = changedTUs.iterator(); i.hasNext();) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				monitor.subTask("Files remaining: " + (count--));
				ITranslationUnit tu = (ITranslationUnit)i.next();
				processChangedTU(tu);
			}
		
		if (removedTUs != null)
			for (Iterator i = removedTUs.iterator(); i.hasNext();) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				monitor.subTask("Files remaining: " + (count--));
				ITranslationUnit tu = (ITranslationUnit)i.next();
				processRemovedTU(tu);
			}
		
		System.out.println("Updator Time: " + (System.currentTimeMillis() - start));
		return Status.OK_STATUS;
	}

	private void processDelta(ICElementDelta delta) {
		// process the children first
		ICElementDelta[] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; ++i)
			processDelta(children[i]);

		// what have we got
		ICElement element = delta.getElement();
		if (element.getElementType() == ICElement.C_PROJECT) {
			switch (delta.getKind()) {
			case ICElementDelta.ADDED:
				processNewProject((ICProject)element);
				break;
			}
		} else if (element.getElementType() == ICElement.C_UNIT) {
			ITranslationUnit tu = (ITranslationUnit)element;
			if (tu.isWorkingCopy())
				// Don't care about working copies either
				return;
			
			switch (delta.getKind()) {
			case ICElementDelta.ADDED:
				if (addedTUs == null)
					addedTUs = new LinkedList();
				addedTUs.add(element);
				++count;
				break;
			case ICElementDelta.CHANGED:
				if (changedTUs == null)
					changedTUs = new LinkedList();
				changedTUs.add(element);
				++count;
				break;
			case ICElementDelta.REMOVED:
				if (removedTUs == null)
					removedTUs = new LinkedList();
				removedTUs.add(element);
				++count;
				break;
			}
		}
	}
	
	private void processNewProject(final ICProject project) {
		try {
			project.getProject().accept(new IResourceProxyVisitor() {
				public boolean visit(IResourceProxy proxy) throws CoreException {
					if (proxy.getType() == IResource.FILE) {
						String fileName = proxy.getName();
						IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(fileName);
						if (contentType == null)
							return true;
						String contentTypeId = contentType.getId();
						
						if (CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(contentTypeId)
								|| CCorePlugin.CONTENT_TYPE_CSOURCE.equals(contentTypeId)) {
							if (addedTUs == null)
								addedTUs = new LinkedList();
							addedTUs.add(CoreModel.getDefault().create((IFile)proxy.requestResource()));
							++count;
						}
						// TODO handle header files
						return false;
					} else {
						return true;
					}
				}
			}, 0);
		} catch (CoreException e) {
			PDOMCorePlugin.log(e);
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
		
		try {
			SQLPDOM sqlpdom = (SQLPDOM)pdom;
			sqlpdom.addSymbols(ast);
			sqlpdom.commit();
		} catch (CoreException e) {
			PDOMCorePlugin.log(e);
		}
	}

	private void processRemovedTU(ITranslationUnit tu) {
		IProject project = tu.getCProject().getProject();
		IPDOM pdom = PDOM.getPDOM(project);
		if (pdom == null || !(pdom instanceof SQLPDOM))
			return;

		try {
			SQLPDOM sqlpdom = (SQLPDOM)pdom;
			sqlpdom.removeSymbols(tu);
			sqlpdom.commit();
		} catch (CoreException e) {
			PDOMCorePlugin.log(e);
		}
	}

	private void processChangedTU(ITranslationUnit tu) {
		IPDOM pdom = PDOM.getPDOM(tu.getCProject().getProject());
		if (pdom == null || !(pdom instanceof SQLPDOM))
			return;
		SQLPDOM sqlpdom = (SQLPDOM)pdom;
		
		IASTTranslationUnit ast;
		try {
			ast = CDOM.getInstance().getTranslationUnit(
					(IFile)tu.getResource(),
					new SQLPDOMCodeReaderFactory(sqlpdom));
		} catch (IASTServiceProvider.UnsupportedDialectException e) {
			return;
		}
		
		if (pdom != ast.getPDOM())
			// weird
			return;
		
		try {
			sqlpdom.removeSymbols(ast);
			sqlpdom.addSymbols(ast);
			sqlpdom.commit();
		} catch (CoreException e) {
			PDOMCorePlugin.log(e);
		}
	}

}
