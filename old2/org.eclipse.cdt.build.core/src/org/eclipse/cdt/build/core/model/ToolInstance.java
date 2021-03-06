/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Doug Schaefer (WRS) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.build.core.model;

import java.util.Map;

/**
 * A tool instance managed a tool's option settings for a given configuration.
 */
public class ToolInstance {

	// TODO this will likely be replaced by preference store
	private Map<String, String> optionValues;

	public Configuration getConfiguration() {
		return null;
	}
	
	/**
	 * @return the associated tool.
	 */
	public Tool getTool() {
		return null;
	}
	
	public Object getOptionValue(Option option) {
		return option.deserialize(optionValues.get(option.getId()));
	}

	public void setOptionValue(Option option, Object value) {
		optionValues.put(option.getId(), option.serialize(value));
	}
	
}
