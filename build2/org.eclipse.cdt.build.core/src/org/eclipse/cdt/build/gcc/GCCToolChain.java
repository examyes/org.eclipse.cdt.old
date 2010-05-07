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

package org.eclipse.cdt.build.gcc;

import java.util.regex.Pattern;

import org.eclipse.cdt.build.model.DiscoveredCommand;
import org.eclipse.cdt.build.model.EnvironmentSetting;
import org.eclipse.cdt.build.model.IConfiguration;
import org.eclipse.cdt.build.model.IToolChain;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.core.resources.IResource;

/**
 * The toolchain definition for gcc.
 * Note this is here mainly for testing. It should migrate to a
 * real gcc plug-in some time.
 * 
 * @author Doug Schaefer
 */
public class GCCToolChain implements IToolChain {

	public static final String ID = "org.eclipse.cdt.build.gcc.toolchain";

	private static final String NAME = "GCC";
	private static final String[] ERROR_PARSER_IDS = {
		"org.eclipse.cdt.core.GCCErrorParser",
		"org.eclipse.cdt.core.GASErrorParser",
		"org.eclipse.cdt.core.GLDErrorParser"
	};
	
	private final String prefix;
	
	public GCCToolChain() {
		prefix = null;
	}
	
	/**
	 * Used by subclasses that are generally cross compilers.
	 * 
	 * @param prefix
	 */
	protected GCCToolChain(String prefix) {
		this.prefix = prefix;
	}
	
	public String getId() {
		return ID; 
	}

	public String getName() {
		return NAME;
	}

	public EnvironmentSetting[] getEnvironmentSettings() {
		return new EnvironmentSetting[0];
	}

	public String[] getErrorParserIds() {
		return ERROR_PARSER_IDS;
	}

	public Pattern[] getDiscoveryPatterns() {
		String pattern = "g(cc|\\+\\+)";
		if (prefix != null)
			pattern = prefix + pattern;
		return new Pattern[] { Pattern.compile(pattern) };
	}

	public DiscoveredCommand getDiscoveredCommand(String[] tokens) {
		// TODO Auto-generated method stub
		return null;
	}

	public IScannerInfo getScannerInfo(DiscoveredCommand command, IConfiguration configuration) {
		// TODO Auto-generated method stub
		return null;
	}

}
