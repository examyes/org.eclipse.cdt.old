/*
 * (c) 2004, 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.rpm.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.rpm.core.IRPMProject;
import org.eclipse.cdt.rpm.core.RPMProjectFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;

/**
 * Import Operation Class for RPM plug-in. This allows us to abstract the operations
 *  to  a utility class which also inherits IRunnableWithProgress that allows use of
 * progress bar
 */

public class SRPMImportOperation implements IRunnableWithProgress {
	private IProject project;
	private File sourceRPM;

	// Progressmonitor
	private IProgressMonitor monitor;

	private List rpm_errorTable;

	/**
	 * Method SRPMImportOperation.
	 * @param name - (IProject) name
	 * @param srpm_name - name of input srpm
	 * @param applyPatchesFlag - Apply patches on import
	 * @param runAutoConfFlag - Run autoconf on import
	 */
	public SRPMImportOperation(IProject project, File sourceRPM) {
		this.project = project;
		this.sourceRPM = sourceRPM;
	}

	/**
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(IProgressMonitor)
	 *
	 * Perform the import of  SRPM import. Call the build class incrementally
	 */
	public void run(IProgressMonitor progressMonitor)
		throws InvocationTargetException {
		// Total number of work steps needed
		int totalWork = 2;

		monitor = progressMonitor;
		rpm_errorTable = new ArrayList();

		monitor.beginTask(Messages.getString("SRPMImportOperation.Starting"), //$NON-NLS-1$
		totalWork); //$NON-NLS-1$

		// Try to create an instance of the build class. 
		try {
			IRPMProject rpmProject = RPMProjectFactory.getRPMProject(project);
			monitor.worked(1);
			monitor.setTaskName(Messages.getString("SRPMImportOperation.Importing_SRPM")); //$NON-NLS-1$
			rpmProject.importSourceRPM(sourceRPM);
		} catch (Exception e) {
			rpm_errorTable.add(e);
			return;
		}
		monitor.worked(1);
	}


	public MultiStatus getStatus() {
	IStatus[] errors = new IStatus[rpm_errorTable.size()];
	Iterator count = rpm_errorTable.iterator();
	int iCount = 0;
	String error_message=Messages.getString("SRPMImportOperation.0"); //$NON-NLS-1$
	while (count.hasNext()) {

		Object anonErrorObject = count.next();
		if (anonErrorObject instanceof Throwable) {
			Throwable errorObject = (Throwable)  anonErrorObject;
			error_message=errorObject.getMessage();
			if (error_message == null)
				error_message=Messages.getString("SRPMImportOperation.1"); //$NON-NLS-1$
				
		}
		else
			if (anonErrorObject instanceof Status)
			{
				Status errorObject = (Status) anonErrorObject;
				error_message=errorObject.getMessage();
				if (error_message == null)
					error_message=Messages.getString("SRPMImportOperation.2"); //$NON-NLS-1$
			}
		IStatus error =
			new Status(
				Status.ERROR,
				"RPM Plugin",Status.OK, //$NON-NLS-1$
				error_message,
				null);
		errors[iCount] = error;
		iCount++;
	}

	return new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK, errors, Messages.getString("SRPMImportOperation.3"), //$NON-NLS-1$
	null);
}
	
}
