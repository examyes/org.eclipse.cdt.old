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
package org.eclipse.cdt.build.core.tests;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author Doug Schaefer
 *
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.build.core.tests"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
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

	/**
	 * Quick way to get ahold of a service.
	 * 
	 * @param service the desired service class
	 * @return the service instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getService(Class<T> service) {
		if (plugin == null)
			return null;
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference ref = context.getServiceReference(service.getName());
		return ref != null ? (T)context.getService(ref) : null;
	}

}
