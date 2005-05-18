/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */
package org.eclipse.cdt.rpm.core.utils;

import org.eclipse.cdt.rpm.core.IRPMConfiguration;
import org.eclipse.cdt.rpm.core.IRPMConstants;
import org.eclipse.cdt.rpm.core.RPMCorePlugin;
import org.eclipse.cdt.rpm.core.utils.internal.ShellScript;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * A utility class for executing rpmbuild commands.
 *
 */
public class RPMBuild {
    
    private IRPMConfiguration config;
	
	private String macroDefines;
	
	private String rpmBuildCmd;
    
	/**
	 * Constructs a new object.
	 * @param config the RPM configuration to use
	 */
    public RPMBuild(IRPMConfiguration config) {
        this.config = config;
		rpmBuildCmd = 
			RPMCorePlugin.getDefault().getPluginPreferences().getString(IRPMConstants.RPMBUILD_CMD) + 
			" -v "; //$NON-NLS-1$
		macroDefines = " --define '_sourcedir " + 
    			config.getSourcesFolder().getLocation().toOSString() + "' "; //$NON-NLS-1$
		macroDefines += "--define '_srcrpmdir " + //$NON-NLS-1$
			config.getSrpmsFolder().getLocation().toOSString() + "' "; //$NON-NLS-1$
		macroDefines += "--define '_builddir " + //$NON-NLS-1$
			config.getBuildFolder().getLocation().toOSString() + "' "; //$NON-NLS-1$
		macroDefines += "--define '_rpmdir " + //$NON-NLS-1$
			config.getRpmsFolder().getLocation().toOSString() + "' "; //$NON-NLS-1$
		macroDefines += "--define '_specdir " + //$NON-NLS-1$
			config.getSpecsFolder().getLocation().toOSString() + "' "; //$NON-NLS-1$
    }
    
	/**
	 * Prepares the sources for a given spec file.
	 * @param specFile the spec file
	 * @throws CoreException if the operation fails
	 */
    public void buildPrep(IFile specFile) throws CoreException {
        String command = rpmBuildCmd;
        command += macroDefines;
        command += " -bp " + specFile.getLocation().toOSString(); //$NON-NLS-1$
        ShellScript script = new ShellScript(command, 0);
		script.exec();
    }
	
	/**
	 * Builds a binary RPM for a given spec file.
	 * @param specFile the spec file
	 * @throws CoreException if the operation fails
	 */
	public void buildBinary(IFile specFile) throws CoreException {
		String command = rpmBuildCmd;
        command += macroDefines;
        command += " -bb " + specFile.getLocation().toOSString(); //$NON-NLS-1$
        ShellScript script = new ShellScript(command, 0);
		script.exec();
	}
	
	/**
	 * Rebuilds a binary RPM from a given source RPM.
	 * @param sourceRPM the source RPM
	 * @throws CoreException if the operation fails
	 */
    public void rebuild(IFile sourceRPM) throws CoreException {
        String command = rpmBuildCmd;
        command += macroDefines;
        command += " --rebuild " + sourceRPM.getLocation().toOSString(); //$NON-NLS-1$
        ShellScript script = new ShellScript(command, 0);
		script.exec();
    }
	
	/**
	 * Builds both a binary and source RPM for a given spec file.
	 * @param specFile the spec file
	 * @throws CoreException if the operation fails
	 */
    public void buildAll(IFile specFile) throws CoreException {
        String command = rpmBuildCmd;
        command += macroDefines;
        command += " -ba " + specFile.getLocation().toOSString(); //$NON-NLS-1$
        ShellScript script = new ShellScript(command, 0);
		script.exec();
    }
	
	/**
	 * Builds a source RPM for a given spec file.
	 * @param specFile the spec file
	 * @throws CoreException if the operation fails
	 */
    public void buildSource(IFile specFile) throws CoreException {
        String command = rpmBuildCmd;
        command += macroDefines;
        command += " -bs " + specFile.getLocation().toOSString(); //$NON-NLS-1$
        ShellScript script = new ShellScript(command, 0);
		script.exec();
    }
}
