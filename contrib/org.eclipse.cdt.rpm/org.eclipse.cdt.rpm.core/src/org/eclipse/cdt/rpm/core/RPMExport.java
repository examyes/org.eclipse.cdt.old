/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.cdt.rpm.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
  *This class is used to manipulate RPM files for Linux systems
  */
public class RPMExport extends RPMExportCore {

	private static final boolean debug = false;
	
	public RPMExport(String c_proj_path) 
		throws CoreException {
	  	super(c_proj_path, "-bb"); //$NON-NLS-1$
	}
	
	public void run() throws CoreException {
		try {
			super.run();
		} catch (Exception e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
			throw new CoreException(error); 	
		}
	}
}