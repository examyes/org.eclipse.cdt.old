package org.eclipse.cdt.internal.build.core;

import org.eclipse.cdt.build.core.IBuildService;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.build.core"; //$NON-NLS-1$

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
		context.registerService(IBuildService.class.getName(), new BuildService(), null);
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
	
	public static void logError(Throwable e) {
		plugin.getLog().log(getError(e));
	}

	public static IStatus getError(Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, e.getLocalizedMessage(), e);
	}
	
}
