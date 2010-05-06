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

/**
 * Represents a command that was discovered by a toolChain.
 * 
 * @author Doug Schaefer
 */
public class DiscoveredCommand {

	private String toolChainId;
	private String command;
	private String[] args;
	
}
