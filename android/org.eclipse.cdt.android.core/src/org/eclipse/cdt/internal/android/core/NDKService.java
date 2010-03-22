/**
 * 
 */
package org.eclipse.cdt.internal.android.core;

import java.io.File;

import org.eclipse.cdt.android.core.Activator;
import org.eclipse.cdt.android.core.INDKService;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

/**
 * Implementation of the NDK service.
 */
public class NDKService implements INDKService {

	private static final String NDK_LOCATION = "ndkLocation";
	
	public File getNDKLocation() {
		IEclipsePreferences prefs = new InstanceScope().getNode(Activator.PLUGIN_ID);
		String loc = prefs.get(NDK_LOCATION, null);
		return loc != null ? new File(loc) : null;
	}

	public void setNDKLocation(File location) {
		IEclipsePreferences prefs = new InstanceScope().getNode(Activator.PLUGIN_ID);
		prefs.put(NDK_LOCATION, location.getAbsolutePath());
	}

}
