/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.cdt.rpm.core.internal;

import org.eclipse.cdt.rpm.core.IRPMConfiguration;
import org.eclipse.cdt.rpm.core.IRPMConstants;
import org.eclipse.cdt.rpm.core.RPMCorePlugin;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

public class RPMConfiguration implements IRPMConfiguration {
	
	private IProject project;
	
	private IFolder rpmsFolder;
    private IFolder srpmsFolder;
    private IFolder specsFolder;
    private IFolder sourcesFolder;
    private IFolder buildFolder;
	
	public RPMConfiguration(IProject project) throws CoreException {
		this.project = project;
		initialize();
	}
	
	/**
	 * Sets the internal folder fields according to stored properties
	 * in the workspace project, or according to the default properties
	 * if no stored properties are found.  If the folders do not exist,
	 * they are created.
	 * @throws CoreException if:
	 * <ul>
	 * <li>Getting or setting project properties fails</ul>
	 * <li>Creating project folders fails</li>
	 * </ul>
	 */
    private void initialize() throws CoreException {
		String pluginID = RPMCorePlugin.ID;
        
		String sourcesPath = 
			project.getPersistentProperty(new QualifiedName(pluginID, IRPMConstants.SOURCES_FOLDER));
		if(sourcesPath == null) {
			sourcesFolder = project.getFolder(IRPMConstants.SOURCES_FOLDER);
			sourcesFolder.create(false, true, null);
			sourcesFolder.setDerived(true);
			project.setPersistentProperty(new QualifiedName(pluginID, IRPMConstants.SOURCES_FOLDER), 
	                sourcesFolder.getName());
        } else {
			sourcesFolder = project.getFolder(sourcesPath);
			if(!sourcesFolder.exists()) {
				sourcesFolder.create(false, true, null);
			}
        }
		
		String srcRpmPath = 
			project.getPersistentProperty(new QualifiedName(pluginID, IRPMConstants.SRPMS_FOLDER));
        if(srcRpmPath == null) {
			srpmsFolder = project.getFolder(IRPMConstants.SRPMS_FOLDER);
			srpmsFolder.create(false, true, null);
			srpmsFolder.setDerived(true);
			project.setPersistentProperty(new QualifiedName(pluginID, IRPMConstants.SRPMS_FOLDER),
					srpmsFolder.getName());
        } else {
			srpmsFolder = project.getFolder(srcRpmPath);
			if(!srpmsFolder.exists()) {
				srpmsFolder.create(false, true, null);
			}
        }
		
		String buildPath = 
			project.getPersistentProperty(new QualifiedName(pluginID, IRPMConstants.BUILD_FOLDER));
        if(buildPath == null) {
            buildFolder = project.getFolder(IRPMConstants.BUILD_FOLDER);
			buildFolder.create(false, true, null);
			buildFolder.setDerived(true);
			project.setPersistentProperty(new QualifiedName(pluginID, IRPMConstants.BUILD_FOLDER), 
					buildFolder.getName());
        } else {
			buildFolder = project.getFolder(buildPath);
			if(!buildFolder.exists()) {
				buildFolder.create(false, true, null);
			}
        }
		
		String rpmPath = 
			project.getPersistentProperty(new QualifiedName(pluginID, IRPMConstants.RPMS_FOLDER));
        if(rpmPath == null) {
			rpmsFolder = project.getFolder(IRPMConstants.RPMS_FOLDER);
			rpmsFolder.create(false, true, null);
			rpmsFolder.setDerived(true);
			project.setPersistentProperty(new QualifiedName(pluginID, IRPMConstants.RPMS_FOLDER), 
	                rpmsFolder.getName());
        } else {
			rpmsFolder = project.getFolder(rpmPath);
			if(!rpmsFolder.exists()) {
				rpmsFolder.create(false, true, null);
			}
        }
		
		String specPath = 
			project.getPersistentProperty(new QualifiedName(pluginID, IRPMConstants.SPECS_FOLDER));
        if(specPath == null) {
            specsFolder = project.getFolder(IRPMConstants.SPECS_FOLDER);
			specsFolder.create(false, true, null);
			specsFolder.setDerived(true);
			project.setPersistentProperty(new QualifiedName(pluginID, IRPMConstants.SPECS_FOLDER),
					specsFolder.getName());
        } else {
			specsFolder = project.getFolder(specPath);
			if(!specsFolder.exists()) {
				specsFolder.create(false, true, null);
			}
        }
    }
	
	public IFolder getBuildFolder() {
		return buildFolder;
	}

	public IFolder getRpmsFolder() {
		return rpmsFolder;
	}

	public IFolder getSourcesFolder() {
		return sourcesFolder;
	}

	public IFolder getSpecsFolder() {
		return specsFolder;
	}

	public IFolder getSrpmsFolder() {
		return srpmsFolder;
	}

}
