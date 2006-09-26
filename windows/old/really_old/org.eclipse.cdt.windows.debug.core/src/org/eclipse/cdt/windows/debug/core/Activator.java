/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.windows.debug.core;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.windows.debug.core";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		// Load in the right dbghelp and dbgeng DLLs
		// TODO find this path in the registry
		String dbgDir = "C:\\Program Files\\Debugging Tools for Windows";
		String dbghelpDLL = dbgDir + "\\dbghelp.dll";
		System.load(dbghelpDLL);
		String dbgengDLL = dbgDir + "\\dbgeng.dll";
		System.load(dbgengDLL);
		
		// Load our library
		try {
			System.loadLibrary("cdtwindbg");
		} catch (UnsatisfiedLinkError e) {
			// try the debug version in the native program
			URL loc = FileLocator.find(getBundle(), new Path("."), null);
			loc = FileLocator.toFileURL(loc);
			IPath path = new Path(loc.getFile()).removeLastSegments(1).append("org.eclipse.cdt.windows.debug.native\\Debug\\cdtwindbg.dll");
			System.load(path.toOSString());
		}
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
