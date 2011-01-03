package org.eclipse.cdt.android.build.internal.core;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

	public static String getId() {
		return context.getBundle().getSymbolicName();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getService(Class<T> clazz) {
		ServiceReference ref = context.getServiceReference(clazz.getName());
		return (ref != null) ? (T)context.getService(ref) : null;
	}
	
	public static Bundle getBundle(String id) {
		for (Bundle bundle : context.getBundles()) {
			if (bundle.getSymbolicName().equals(id)) {
				return bundle;
			}
		}
		return null;
	}
	
	public static IStatus newStatus(Exception e) {
		return new Status(IStatus.ERROR, getId(), e.getMessage(), e);
	}
	
	public static void log(Exception e) {
		getService(ILog.class).log(newStatus(e));
	}

	public static URL find(IPath path) {
		return FileLocator.find(context.getBundle(), path, null);
	}

	public static IEclipsePreferences getPreferences() {
		return new InstanceScope().getNode(getId());
	}
	
}
