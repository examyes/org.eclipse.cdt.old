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

import java.io.*;

/**
  *This class is used to import source RPMs into Eclipse projects.
  *It is called by the GUI to invoke the run method which does the import.
  */
public class SRPMImport extends RPMCore {

	private boolean doAutoconf = true;
	private boolean doPatches = true; 
	
	//	When debug is set to true, lots of debug statements are printed.
	private static final boolean debug = false;
	/*
	 * Constructor to create data for the RPMCore class.
	 */
  public SRPMImport(String c_proj_path, String c_path_to_rpm) 
  	throws CoreException {
		super(c_proj_path, c_path_to_rpm);
		
		int j = path_to_rpm.lastIndexOf(file_sep);
		if (j == -1) {
			srpm_full_name = path_to_rpm;
		} else {
			srpm_full_name = path_to_rpm.substring(j+1);
		}
  }
		  
	/**
		 * Method setupSRPMworkarea.
		 * This method is called by the GUI to setup the RPM workarea specified
		 * in the "rpmdirs_bld" path.  The directories that rpm will use to install and 
		 * setup the source rpm before importing it into the Eclipse project.
		 * @return return if successful, throw CoreException if failed
		 */
	public void run() throws CoreException {
		if (debug) {
			System.out.println("******************Import run*****************"); //$NON-NLS-1$
		}
		setRpmbuild_logname();
		rpmbuild_logname = getRpmbuild_logname();
		try {
			createRPMdirectories(rpmdirs_path);
			createRPMmacros(rpm_macros);
			createRPMrpmrc(rpmrc);
			createRPMLogFile();
		} catch (Exception e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
			throw new CoreException(error); 
		}
		String orig_proj_path;
		try {
			orig_proj_path = getSourceCode();
		} catch (Exception e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
			throw new CoreException(error); 
		}
				
		// Now see if 'configure' should be run, but only if we applied patches
		if (doAutoconf & doPatches) {
			executeProjConfigure(orig_proj_path);
		}
		// We are now ready to copy the files from the work area to the Eclipse project
		
		try {
			LinuxShellCmds.linuxCopy(orig_proj_path, proj_path, rpmbuild_logname, rpm_shell);
		} catch (CoreException e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
			throw new CoreException(error); 
		}
		String spec_dir = file_sep + "SPECS" + file_sep; //$NON-NLS-1$
		// Copy the spec file and source RPM to the project for safekeeping
		File f = new File(rpmdirs_path + spec_dir); 
		String[] subdir = f.list();
		if (subdir == null | subdir.length > 1) {
			String throw_message = "There should only be one file in: " + rpmdirs_path + spec_dir; //$NON-NLS-1$
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
			throw new CoreException(error); 
		}
		String from_file = rpmdirs_path + spec_dir + subdir[0];
		try {
			super.copyFile(from_file, proj_path + file_sep + spec_file_prefix + subdir[0]);
			super.copyFile(path_to_rpm, proj_path + file_sep + srpm_full_name);
		} catch (Exception e) {
			String throw_message = e.getMessage();
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
			throw new CoreException(error); 
		}
		// Now, create a file that resides in the project that contains information 
		int j = path_to_rpm.lastIndexOf(file_sep);
		String srpm_full_name = path_to_rpm.substring(j + 1); 
		createSRPMinfo(spec_file_prefix + subdir[0], srpm_full_name);
		// Now clean up the RPM work area
		deleteRPMresources(rpmdirs_path);
	}
	
	/**
		 * Method getSourceCode.
		 * This method is called to install the source code and then depending
		 * on whether or not patches are to be applied either run the rpmbuild
		 * command or untar the source tarball.
		 * @return return path to the source if successful, return "" or 
		 *    throw CoreException if failed
		 */
	public String getSourceCode() throws CoreException {
		if (debug) {
			System.out.println("getSourceCode"); //$NON-NLS-1$
		}
		try {
					installRPMsource(path_to_rpm);
				} catch (CoreException e) {
					String throw_message = e.getMessage();
					IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
					throw new CoreException(error); 
				}
				// If the doPatches switch is set run the "rpmbuild -bp" command to install the 
				//  source with patches
				if (doPatches) {
					try {
						executeRPMbuildprep();
					}  catch (CoreException e) {
						String throw_message = e.getMessage();
						IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
						throw new CoreException(error); 
					}
					// Figure out what the path to the installed sources is
					File f = new File(rpmdirs_path + file_sep + "BUILD"); //$NON-NLS-1$
					String subdir[] = f.list();
					if (subdir == null | subdir.length != 1) {
						String throw_message = Messages.getString("SRPMImport.Error_occurred_during_the_source_install._n_1") + //$NON-NLS-1$
						   Messages.getString("SRPMImport.There_are_either_too_many_or_0_directories_under__2") + f; //$NON-NLS-1$
						IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
						throw new CoreException(error); 
					}
					return rpmdirs_path + file_sep + "BUILD" + file_sep + subdir[0]; //$NON-NLS-1$
				// If doPatches is not set, untar the source myself
				} else {
					String tarball_path = findTarBallPath(rpmdirs_path + file_sep + "SOURCES"); //$NON-NLS-1$
					if (tarball_path != "") { //$NON-NLS-1$
					  TarOps tarOps = new TarOps(proj_path, rpmdirs_path);
					  try {
						tarOps.untarSource(tarball_path, rpmbuild_logname, rpm_shell);
					  } catch (CoreException e) {
						  String throw_message =e.getMessage();
						  IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
						  throw new CoreException(error); 
					  }
					} else {
						String throw_message = Messages.getString("SRPMImport.Cannot_find_a_tarball_to_untar_in___3") + tarball_path; //$NON-NLS-1$
						IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message, null);
						throw new CoreException(error); 
					}
				}
						// Figure out where the source code is, there *should* be only one directory
						 // in the 'rpmdirs_path/SOURCES/' directory
						 String src_path = rpmdirs_path + file_sep + "SOURCES" + file_sep; //$NON-NLS-1$
						 File f = new File(src_path);
						 String[] subdir = f.list();
						 for (int i =0; i<subdir.length; i++) {
							 File f1 = new File(src_path + subdir[i]);
							 if (f1.isDirectory()) {
								 return src_path + subdir[i];
							 }
						 }
		return ""; //$NON-NLS-1$
	}
	/**
	 * Method findTarBallPath.
	 * This method is called to find a tarball in the directory passed to it.
	 * @param String containing the path to the directory to search for the tarball
	 * @return String containing a path to the tarball if there is one, else return ""
	 */
	public String findTarBallPath(String path) {
		File f = new File(path);
		String[] subdir = f.list();
		for (int i = 0; i<subdir.length;i++) {
			if (subdir[i].endsWith(".bz2") | subdir[i].endsWith(".gz") | //$NON-NLS-1$ //$NON-NLS-2$
			  subdir[i].endsWith(".tar")) { //$NON-NLS-1$
			  	return path + file_sep + subdir[i];
			  }
		}
	  return "";	//$NON-NLS-1$
	}
	
	/**
		 * Method executeRPMbuildprep.
		 * This method creates a shell script with the 'rpmbuild -bp' command that takes the sources
		 * peviously installed in the RPM work area, untars them into the BUILD directory and applies
		 * all of the patches specified in the spec file.
		 * @return String - path to the original srpm that was copied into the work area
		 */
		/******************************************************************************/
		public String executeRPMbuildprep() throws CoreException {
			if (debug) {
				System.out.println(" Import executeRPMbuildprep"); //$NON-NLS-1$
			}
			String rpm_shell = getRpm_shell(); 
			boolean cmd_stat;
			String orig_proj_path = proj_path;
			// Get the path to the spec file directory to use
			String specdir = rpmdirs_path + file_sep + "SPECS" + file_sep; //$NON-NLS-1$
			File f = new File(specdir);

			if (!f.isDirectory()) {
				String throw_message = Messages.getString(
						"RPMCore.There_is_not_a__360") + specdir + //$NON-NLS-1$
					Messages.getString(
						"RPMCore._directory._nCheck_permissions_in_the_path_directories._361"); //$NON-NLS-1$
				IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
						null);
				throw new CoreException(error);
			}

			FilenameFilter only = new OnlyExt("spec"); //$NON-NLS-1$
			String[] s = f.list(only);

			/* Get ready to execute the rpmbuild command to do a "build prep" which
			 *  will untar the source into a directory under BUILD and apply all patches
			 *  specified in the "spec" file.
			 */
			String build_opt = "-bp"; //$NON-NLS-1$

			String rpmbuild_cmd = "/usr/bin/rpmbuild " + build_opt + //$NON-NLS-1$
				" -v --rcfile " + rpmrc + " --nodeps" + " " + specdir + s[0]; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			try {
				LinuxShellCmds.createLinuxShellScript(rpmbuild_cmd, rpmbuild_logname, rpm_shell);
				LinuxShellCmds.executeLinuxCommand(rpm_shell, 0);
			} catch (CoreException e) {
				String throw_message = e.getMessage();
				IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
						null);
				throw new CoreException(error);
			}

			// Need to figure out what the name of the directory under the BUILD dir is
			File build_dir = new File(rpmdirs_path + file_sep + "BUILD" + file_sep); //$NON-NLS-1$
			String[] build_dir_list = build_dir.list();
			String orig_srpm_path = build_dir + file_sep + build_dir_list[0];

				if (build_dir_list.length != 1) {
					String throw_message = Messages.getString(
							"RPMCore.There_should_be_only_one_directory_under__391") + //$NON-NLS-1$
						build_dir +
						Messages.getString("RPMCore._at_this_point_392"); //$NON-NLS-1$
					IStatus error = new Status(IStatus.ERROR, Error, 1,
							throw_message, null);
					throw new CoreException(error);
				}

				String eclipse_specfile_path = orig_srpm_path + file_sep + spec_file_prefix + 
					s[0];

				File f1 = new File(eclipse_specfile_path);

				if (f1.exists()) {
					String throw_message = Messages.getString(
							"RPMCore.This_file_already_exists___396") + //$NON-NLS-1$
						eclipse_specfile_path;
					IStatus error = new Status(IStatus.ERROR, Error, 1,
							throw_message, null);
					throw new CoreException(error);
				} 

			return orig_srpm_path;
		}
	
	/**
	 * @param b
	 */
	public void setDoAutoconf(boolean b) {
		doAutoconf = b;
	}

	/**
	 * @param b
	 */
	public void setDoPatches(boolean b) {
		doPatches = b;
	}

  }
