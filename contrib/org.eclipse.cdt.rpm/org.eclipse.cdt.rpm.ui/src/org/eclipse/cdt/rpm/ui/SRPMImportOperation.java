/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.rpm.ui;

import org.eclipse.cdt.rpm.core.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.ui.PlatformUI;

import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author pmuldoon
 *
 * Import Operation Class for RPM plug-in. This allows us to abstract the operations
 *  to  a utility class which also inherits IRunnableWithProgress that allows use of
 * progress bar
 */

public class SRPMImportOperation implements IRunnableWithProgress {
	// Class variable that are use for storage
	// as they are passed into the constrcutor
	private String srpmname;
	private IProject project_name;
	private boolean preserve_env;
	private boolean keep_log;
	private boolean applyPatches;
	private boolean runAutoConf;

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
	public SRPMImportOperation(
		IProject name,
		String srpm_name,
		boolean applyPatchesFlag,
		boolean runAutoConfFlag) {
		// Copy passed variables to constructor, to class variables	
		project_name = name;
		srpmname = srpm_name;
		applyPatches = applyPatchesFlag;
		runAutoConf = runAutoConfFlag;
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

		SRPMImport srpmimport;

		monitor.beginTask(Messages.getString("SRPMImportOperation.Starting"), //$NON-NLS-1$
		totalWork); //$NON-NLS-1$

		// Try to create an instance of the build class. 
		try {
			srpmimport = new SRPMImport(project_name.getLocation().toOSString(), srpmname); //$NON-NLS-1$
		} catch (Exception e) {
			rpm_errorTable.add(e);
			return;
		}
		monitor.worked(1);
		String rpm_release;
		String rpm_version;
		String usr_rpm_cmd = RPMCorePlugin.getDefault().getPreferenceStore().getString("IRpmConstants.RPM_CMD"); //$NON_NLS-1$
		
		rpm_version = LinuxShellCmds.getInfo(usr_rpm_cmd + " --qf %{VERSION} -qp " + //$NON-NLS-1$
						srpmname);
		rpm_release = LinuxShellCmds.getInfo(usr_rpm_cmd + " --qf %{RELEASE} -qp " + //$NON-NLS-1$;
						srpmname);
		// If the generated checksum, and the one in the srpmInfo file are the same
		// then the project has not changed since last import and does not need a patch
					
				
		// set state and options
		srpmimport.setDoAutoconf(runAutoConf);
		srpmimport.setDoPatches(applyPatches);
		srpmimport.setRpm_release(rpm_release);
		srpmimport.setRpm_version(rpm_version);

		monitor.setTaskName(Messages.getString("SRPMImportOperation.Importing_SRPM")); //$NON-NLS-1$

		// execute import
		try {
			srpmimport.run();
		} catch (CoreException e) {
			rpm_errorTable.add(e.getStatus());
			return;
		}

		monitor.worked(1);

		// Refresh the workspace
		IWorkspaceRoot myWorkspaceRoot =
			ResourcesPlugin.getWorkspace().getRoot();

		try {
			myWorkspaceRoot.refreshLocal(2, null);
			project_name.refreshLocal(2, null);
		} catch (CoreException e1) {
			rpm_errorTable.add(e1.getStatus());
		}
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
