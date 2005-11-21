package org.eclipse.cdt.pdom.ui;

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PDOMUIPlugin extends AbstractUIPlugin {

	public static final String ID = "org.eclipse.cdt.pdom.ui";
	
	//The shared instance.
	private static PDOMUIPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public PDOMUIPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PDOMUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.cdt.pdom.ui", path);
	}
	
	public static void log(CoreException e) {
		plugin.getLog().log(e.getStatus());
	}

}
