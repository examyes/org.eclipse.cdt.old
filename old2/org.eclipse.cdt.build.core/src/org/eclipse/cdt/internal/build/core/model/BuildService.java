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
package org.eclipse.cdt.internal.build.core.model;

import org.eclipse.cdt.build.core.model.IBuildProject;
import org.eclipse.cdt.build.core.model.IBuildService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * @author Doug Schaefer
 *
 */
public class BuildService implements IBuildService {

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unsubscribe(IResource resource,	IScannerInfoChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public IBuildProject getBuildProject(IProject project) {
		return (IBuildProject)project.getAdapter(IBuildProject.class);
	}
	
}
