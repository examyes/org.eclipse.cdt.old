/**
 * 
 */
package org.eclipse.cdt.android.core;

import java.io.File;

/**
 * Service for getting information about the Android NDK.
 */
public interface INDKService {

	File getNDKLocation();
	
	void setNDKLocation(File location);
	
}
