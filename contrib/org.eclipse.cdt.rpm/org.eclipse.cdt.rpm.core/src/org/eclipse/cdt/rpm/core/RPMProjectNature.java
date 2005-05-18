/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.cdt.rpm.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * The RPM project nature.
 *
 */
public class RPMProjectNature implements IProjectNature {

	/**
	 * The unique nature ID associated with the RPM project nature.
	 */
	public static final String RPM_NATURE_ID = RPMCorePlugin.ID + ".rpmnature"; //$NON-NLS-1$
	
	IProject project;
	
	public RPMProjectNature() {
	}
	
	public RPMProjectNature(IProject project) {
		this.project = project;
	}
	
	/**
	 * Adds the RPM project nature to a given workspace project.
	 * @param project the project
	 * @param mon a progress monitor, or <code>null</code> if progress monitoring
	 * is not desired
	 * @throws CoreException if adding the RPM project nature fails
	 */
	public static void addRPMNature(IProject project, IProgressMonitor mon) throws CoreException {
		addNature(project, RPM_NATURE_ID, mon);
	}

	/**
	 * Removes the RPM project nature from a given workspace project.
	 * @param project the project
	 * @param mon a progress monitor, or <code>null</code> if progress monitoring
	 * is not desired
	 * @throws CoreException if removing the RPM project nature fails
	 */
	public static void removeRPMNature(IProject project, IProgressMonitor mon) throws CoreException {
		removeNature(project, RPM_NATURE_ID, mon);
	}
	
	/**
	 * Utility method for adding a nature to a project.
	 * 
	 * @param proj
	 *            the project to add the nature
	 * @param natureId
	 *            the id of the nature to assign to the project
	 * @param monitor
	 *            a progress monitor to indicate the duration of the operation,
	 *            or <code>null</code> if progress reporting is not required.
	 *  
	 */
	private static void addNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		if(project.hasNature(natureId)) {
			return;
		}
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length] = natureId;
		description.setNatureIds(newNatures);
		project.setDescription(description, monitor);
	}

	/**
	 * Utility method for removing a project nature from a project.
	 * 
	 * @param proj
	 *            the project to remove the nature from
	 * @param natureId
	 *            the nature id to remove
	 * @param monitor
	 *            a progress monitor to indicate the duration of the operation,
	 *            or <code>null</code> if progress reporting is not required.
	 */
	private static void removeNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		List newNatures = new ArrayList(Arrays.asList(prevNatures));
		newNatures.remove(natureId);
		description.setNatureIds((String[]) newNatures.toArray(new String[newNatures.size()]));
		project.setDescription(description, monitor);
	}
	
	public void configure() throws CoreException {
	}

	public void deconfigure() throws CoreException {
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

}
