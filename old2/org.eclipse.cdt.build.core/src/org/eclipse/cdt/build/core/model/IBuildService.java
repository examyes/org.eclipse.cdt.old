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
package org.eclipse.cdt.build.core.model;

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IResource;

/**
 * The OSGi Service access point into the CDT Build System.
 *  
 * @author Doug Schaefer
 */
public interface IBuildService {

	/**
	 * Return the scanner info for the given resource based in the current build settings.
	 * 
	 * @param resource
	 * @return
	 */
	IScannerInfo getScannerInfo(IResource resource);
	

}
