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

import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.IPDOMProvider;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.pdom.core.PDOMCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;

/**
 * @author Doug Schaefer
 *
 */
public class SQLPDOMProvider implements IPDOMProvider, IElementChangedListener, IJobChangeListener {

	private SQLPDOMUpdaterJob currJob;
	
	private static final QualifiedName pdomProperty
		= new QualifiedName(PDOMCorePlugin.ID, "sqlpdom"); //$NON-NLS-1$
	
	public IPDOM getPDOM(IProject project) {
		try {
			IPDOM pdom = (IPDOM)project.getSessionProperty(pdomProperty);
			
			if (pdom == null) {
				pdom = new SQLPDOM(project, this);
				project.setSessionProperty(pdomProperty, pdom);
			}
			
			return pdom;
		} catch (CoreException e) {
			PDOMCorePlugin.log(e);
			return null;
		}
	}

	public void deletePDOM(IProject project) throws CoreException {
		IPDOM pdom = (IPDOM)project.getSessionProperty(pdomProperty); 
		project.setSessionProperty(pdomProperty, null);
		pdom.delete();
	}
	
	public IElementChangedListener getElementChangedListener() {
		return this;
	}

	public synchronized void elementChanged(ElementChangedEvent event) {
		// Only respond to post change events
		if (event.getType() != ElementChangedEvent.POST_CHANGE)
			return;
		
		currJob = new SQLPDOMUpdaterJob(event.getDelta(), currJob);
		currJob.addJobChangeListener(this);
		currJob.schedule();
	}

	public void aboutToRun(IJobChangeEvent event) {
	}

	public void awake(IJobChangeEvent event) {
	}

	public synchronized void done(IJobChangeEvent event) {
		if (currJob == event.getJob())
			currJob = null;
	}

	public void running(IJobChangeEvent event) {
	}

	public void scheduled(IJobChangeEvent event) {
	}

	public void sleeping(IJobChangeEvent event) {
	}
	
}
