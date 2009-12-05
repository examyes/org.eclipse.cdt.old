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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdapterFactory;

/**
 * @author Doug Schaefer
 *
 */
public class BuildAdapterFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof IProject) {
			IProject project = (IProject)adaptableObject;
			if (adapterType == IBuildProject.class)
				return new BuildProject(project);
		}
		
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] { IBuildProject.class };
	}

}
