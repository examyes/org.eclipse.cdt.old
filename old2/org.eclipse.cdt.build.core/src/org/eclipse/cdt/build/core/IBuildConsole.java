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
package org.eclipse.cdt.build.core;

import java.io.OutputStream;

/**
 * Interface to display messages either in the console view when the workbench is up
 * or to stdout/stderr when running headless. 
 */
public interface IBuildConsole {

	/**
	 * Is this build console available. For example, the Console view is not available if the build
	 * is running headless.
	 * 
	 * @return is the build console available.
	 */
	boolean isAvailable();

	/**
	 * @return a stream to write output to
	 */
	OutputStream getOutputStream();
	
	/**
	 * @return a stream to write errors to
	 */
	OutputStream getErrorStream();
	
	/**
	 * Activate the console
	 */
	void activate();
	
}
