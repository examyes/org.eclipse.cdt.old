/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.build.model;

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IResource;


/**
 * @author Doug Schaefer
 * 
 * The toolchain selects a build output parser for scanner discovery, an
 * error parser or two, and whatever we need.
 * 
 * Note that these objects are read-only and are supplied by the toolChain extension point.
 */
public interface IToolChain {

	/**
	 * Returns the id for this toolchain. It must match the id in the extension point
	 * that creates this object.
	 * 
	 * @return
	 */
	String getId();
	
	/**
	 * Returns the name for the toolchain.
	 * 
	 * @return name
	 */
	String getName();

	/**
	 * Returns the built-in environment settings required by this toolchain.
	 * 
	 * @return toolchain environment settings
	 */
	EnvironmentSetting[] getEnvironmentSettings();
	
	/**
	 * Returns the ids of the error parsers to use with this toolchain.
	 * 
	 * @return error parser ids
	 */
	String[] getErrorParserIds();

	/**
	 * Returns the scanner info for the given resource.
	 * 
	 * @param command or null
	 * @return
	 */
	IScannerInfo getScannerInfo(IResource resource);
	
}
