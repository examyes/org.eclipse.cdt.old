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

import org.eclipse.cdt.build.core.model.IBuildService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoChangeListener;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.internal.build.core.Activator;
import org.eclipse.core.resources.IResource;

/**
 * @author Doug Schaefer
 *
 */
public class ScannerInfoProvider implements IScannerInfoProvider {

	@Override
	public IScannerInfo getScannerInformation(IResource resource) {
		return Activator.getService(IBuildService.class).getScannerInformation(resource);
	}

	@Override
	public void subscribe(IResource resource, IScannerInfoChangeListener listener) {
		Activator.getService(IBuildService.class).subscribe(resource, listener);
	}

	@Override
	public void unsubscribe(IResource resource, IScannerInfoChangeListener listener) {
		Activator.getService(IBuildService.class).unsubscribe(resource, listener);
	}

}
