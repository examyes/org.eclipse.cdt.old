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

/**
 * @author pmuldoon
 *
 * Export Class for RPM plug-in. This allows us to abstract the operations to
 * a utility class which also inherits IRunnableWithProgress that allows use of
 * progress bar
 */
public class RPMExportOperation implements IRunnableWithProgress {
	// Class variables that are use for storage
	// as they are passed into the constrcutor
	private String project_name;
	private String actual_proj_name;
	private boolean preserve_env;
	private boolean keep_log;
	private String spec_file;
	private String patch_name = ""; //$NON-NLS-1$
	private String patch_tag = ""; //$NON-NLS-1$
	private String patch_changelog = ""; //$NON-NLS-1$
	private String patch_version = ""; //$NON-NLS-1$
	private String patch_release = ""; //$NON-NLS-1$
	private boolean gen_patch;
	private String exportType;

	// Progressmonitor
	private IProgressMonitor monitor;

	public ArrayList rpm_errorTable;

	/**
	 * Method RPMExportOperation.
	 * @param a_proj_name - actual project name
	 * @param work_path - path to workspace of project
	 * @param pSpec_File - path to spec file
	 * @param pPatchName - patch file name to use
	 * @param pPatchComment - Comment for patch entry
	 * @param pPatchChangeLog - ChangeLog header entry
	 * @param pPatchVersion - Version Number to use
	 * @param pPatchRelease- Release Number to use
	 * @param pexportType - -ba, -bs or -bp (all, source, binary)
	 * @param pGenPatch - Patch Generation toggle
	 *
	 */
	public RPMExportOperation(
		String a_proj_name,
		String work_path,
		String pSpec_File,
		String pPatchName,
		String pPatchComment,
		String pPatchChangeLog,
		String pPatchVersion,
		String pPatchRelease,
		String pexportType,
		boolean pGenPatch) {

		// Copy passed variables to constructor, to class variables	
		project_name = work_path;
		actual_proj_name = a_proj_name;
		spec_file = pSpec_File;
		patch_name = pPatchName;
		patch_tag = pPatchComment;
		patch_changelog = pPatchChangeLog;
		patch_version = pPatchVersion;
		patch_release = pPatchRelease;
		exportType = pexportType;
		gen_patch = pGenPatch;
	}

	/**
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(IProgressMonitor)
	 *
	 * Perform the incremental build of the S/RPM export. 
	 */
	public void run(IProgressMonitor progressMonitor)
		throws InvocationTargetException {

		int totalWork = 0;
		ArrayList PatchInfo = null;
		RPMExport rpmexport;
		SRPMExport srpmexport;

		// Total number of work steps needed
		if (exportType.equals("-ba")) //$NON-NLS-1$
			totalWork = 4;
		else
			totalWork = 2;

		monitor = progressMonitor;

		// We keep a all our reported errors in an ArrayList.
		rpm_errorTable = new ArrayList();

		// Start progress
		monitor.beginTask(Messages.getString("RPMExportOperation.Starting"), //$NON-NLS-1$
		totalWork); //$NON-NLS-1$

		// If the export type is all  (-ba) or export type is source (-bs)
		if (exportType.equals("-bs") || exportType.equals("-ba")) { //$NON-NLS-1$ //$NON-NLS-2$

			monitor.setTaskName(Messages.getString("RPMExportOperation.Starting_SRPM_Export")); //$NON-NLS-1$

			// Try to create an instance of the srpm export class
			try {
				srpmexport = new SRPMExport(project_name);
			} catch (Exception e) {
				rpm_errorTable.add(e);
				refreshWorkspace();
				return;
			}
			// set the various properties of a SRPM export
			srpmexport.setChangelog_entry(patch_changelog);
			srpmexport.setUi_ver_no(patch_version);
			srpmexport.setUi_rel_no(patch_release);
			srpmexport.setUi_spec_file(spec_file);
			srpmexport.setPatch_tag(patch_tag);

			monitor.worked(1);
			monitor.setTaskName(Messages.getString("RPMExportOperation.Executing_SRPM_Export")); //$NON-NLS-1$

			// execue the srpm export
			try {
				srpmexport.run();
			} catch (CoreException e) {
				rpm_errorTable.add(e.getStatus());
				refreshWorkspace();
				return;
			}
			monitor.worked(1);

		}
		// If the export type is all  (-ba) or export type is binary (-bb)
		if (exportType.equals("-bb") || exportType.equals("-ba")) { //$NON-NLS-1$ //$NON-NLS-2$

			monitor.setTaskName(Messages.getString("RPMExportOperation.Starting_RPM_Export")); //$NON-NLS-1$

			// Try to create an instance of the rpm export class
			try {
				rpmexport = new RPMExport(project_name);
			} catch (Exception e) {
				rpm_errorTable.add(e);
				refreshWorkspace();
				return;
			}

			// set the various properties of an RPM export
			rpmexport.setUi_ver_no(patch_version);
			rpmexport.setUi_rel_no(patch_release);
			rpmexport.setUi_spec_file(spec_file);
			if (exportType.equals("-bb")) { //$NON-NLS-1$
				rpmexport.setPatch_tag(patch_tag);
				rpmexport.setChangelog_entry(patch_changelog);
			}

			monitor.worked(1);
			monitor.setTaskName(Messages.getString("RPMExportOperation.Executing_RPM_Export")); //$NON-NLS-1$

			// execue the rpm export
			try {
				rpmexport.run();
			} catch (CoreException e) {
				rpm_errorTable.add(e.getStatus());
				refreshWorkspace();
				return;
			}

			monitor.worked(1);
		}

		refreshWorkspace();

	}


	public void refreshWorkspace()
	{
		// Refresh the workspace
		IWorkspaceRoot myWorkspaceRoot =
		ResourcesPlugin.getWorkspace().getRoot();
		IProject myProject = myWorkspaceRoot.getProject(actual_proj_name);

		try {
			myWorkspaceRoot.refreshLocal(2, null);
			myProject.refreshLocal(2, null);
		} catch (CoreException e1) {
			rpm_errorTable.add(e1.getStatus());
		}
	}
	
	public MultiStatus getStatus() {
		IStatus[] errors = new IStatus[rpm_errorTable.size()];
		Iterator count = rpm_errorTable.iterator();
		int iCount = 0;
		String error_message=Messages.getString("RPMExportOperation.0"); //$NON-NLS-1$
		while (count.hasNext()) {

			Object anonErrorObject = count.next();
			if (anonErrorObject instanceof Throwable) {
				Throwable errorObject = (Throwable)  anonErrorObject;
				error_message=errorObject.getMessage();
				
			}
			else
				if (anonErrorObject instanceof Status)
				{
					Status errorObject = (Status) anonErrorObject;
					error_message=errorObject.getMessage();
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

		return new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.OK, errors, Messages.getString("RPMExportOperation.Open_SRPM_Errors"), //$NON-NLS-1$
		null);
	}
	
}
