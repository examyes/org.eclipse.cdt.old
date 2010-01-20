/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Doug Schaefer (WRS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.build.core.model;

import org.eclipse.cdt.build.core.model.BuildProject;
import org.eclipse.cdt.build.core.model.Configuration;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IResource;

/**
 * Get the scanner info provider from the active indexer configuration.
 */
public class ScannerInfoProvider implements IScannerInfoProvider {

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		BuildProject buildProject = BuildProject.getBuildProject(resource.getProject());
		Configuration indexConfig = buildProject.getActiveIndexConfiguration();
		return indexConfig.getScannerInformation(resource);
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		// TODO where to manage this, build project
		// The main issue is to send events when active config changes.
		// BTW, I'm not sure this is even called any more ???
	}

	@Override
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		// TODO see above
	}

}
