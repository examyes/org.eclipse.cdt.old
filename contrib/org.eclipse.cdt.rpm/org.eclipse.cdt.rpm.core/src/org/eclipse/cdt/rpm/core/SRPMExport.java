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
  *This class is called from the GUI when a source RPM is to be created.
  */
public class SRPMExport extends RPMExportCore {
	
	private static final boolean debug = false;
	
	/**
	 * This is the constructor called by the GUI to instantiate this class.
	 * @param c_proj_path is the path to the Eclipse project to be exported
	 * @param c_wksp_path is the path to the temporary work area to be used by rpm
	 * @throws CoreException
	 */
	
	public SRPMExport(String c_proj_path) throws CoreException {
		super(c_proj_path, "-bs"); //$NON-NLS-1$
	}
	
	/**
	 * This run method is called by the GUI to run the methods to create a source RPM.
	 * @return if successful, throw CoreException if not
	 * @throws CoreException
	 */
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