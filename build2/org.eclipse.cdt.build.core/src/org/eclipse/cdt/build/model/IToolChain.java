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

import java.util.regex.Pattern;

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

	// TODO these may need to be in a discovery participant object
	
	/**
	 * Returns the regex patterns used by the build output parser to determine if this
	 * toolchain is interested in a given line in the build output.
	 * 
	 * @return interesting discovery patterns
	 */
	Pattern[] getDiscoveryPatterns();
	
	/**
	 * Creates a discovered command for the given tokenized command line. Return null
	 * if this command line wasn't that interesting.
	 * 
	 * @param tokens
	 * @return discovered command
	 */
	DiscoveredCommand getDiscoveredCommand(String[] tokens);
	
	/**
	 * Returns the scanner info for the given command in the given configuration.
	 * 
	 * @param command
	 * @return scanner info for command
	 */
	IScannerInfo getScannerInfo(DiscoveredCommand command, IConfiguration configuration);
	
}
