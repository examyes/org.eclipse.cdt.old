package org.eclipse.cdt.internal.build.core;

import org.eclipse.cdt.build.model.IBuildService;
import org.eclipse.cdt.internal.build.model.BuildService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
	private ServiceRegistration buildServiceReg;

	public static BundleContext getContext() {
		return context;
	}

	public static String getId() {
		return context.getBundle().getSymbolicName();
	}
	
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		// register the BuildService
		buildServiceReg = bundleContext.registerService(IBuildService.class.getName(), new BuildService(), null);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		
		// unregister the BuildService
		if (buildServiceReg != null) {
			buildServiceReg.unregister();
			buildServiceReg = null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getService(Class<T> clazz) {
		ServiceReference ref = context.getServiceReference(clazz.getName());
		try{
			return (ref != null) ? (T)context.getService(ref) : null;
		} finally {
			if(ref != null)
				context.ungetService(ref);
		}
	}

}
