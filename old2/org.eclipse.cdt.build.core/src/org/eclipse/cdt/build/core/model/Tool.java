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

/**
 * @author Doug Schaefer
 * 
 * A tool is a processor that takes input IResources and produces build targets.
 * Tools have properties that control it's behavior.
 * Tools are external tools invoked with a command line formed from the tools properties.
 * (TODO could we have internal tools? For the internal builder?)
 * Tools are instantiated at the folder level (e.g. pattern rules) or file level
 */
public class Tool {

	public String getId() {
		return null;
	}
	
}
