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
  *This class is used to interface with the Linux "tar" command when
  *manipulating rpm's
  */
public class TarOps {
	
//	When debug is set to true, lots of debug statements are printed.
	 private static final boolean debug = false;
	 private static final String Error = Messages.getString("RPMCore.Error_1"); //$NON-NLS-1$
	 private static final String file_sep = System.getProperty("file.separator"); //$NON-NLS-1$
	 private static final String line_sep = System.getProperty("line.separator"); //$NON-NLS-1$
	 
	 private String rpmdirs_path;
	 private String proj_path;
	 
	 public TarOps (String c_proj_path, String c_rpmdirs_path) 
		throws CoreException {
		
	 	proj_path = c_proj_path;
	 	rpmdirs_path = c_rpmdirs_path;
	 	
	 }

	/**
		 * Method untarSource.
		 * This method is called when the source RPM being imported and the developer does
		 * not want patches applied to the source code.
		 * @return void if OK, throws CoreException if error occurs
		 * @throws CoreException
		 */
		public boolean untarSource(String tarball_path, String rpmbuild_logname,
		   String rpm_shell) throws CoreException {
			if (debug) {
				System.out.println("untarSource"); //$NON-NLS-1$
			}

			String tarball_type = null;
			String from_path;

			// Get a list of files in the SOURCES directory
			File tarball = new File(tarball_path);

			// See what the tarball ends with to see which compression we need
			if (tarball_path.endsWith(".gz")) { //$NON-NLS-1$
					tarball_type = "z"; //$NON-NLS-1$
				} else if (tarball_path.endsWith(".bz2")) { //$NON-NLS-1$
					tarball_type = "j"; //$NON-NLS-1$
				} else if (tarball_path.endsWith(".tar")) { //$NON-NLS-1$
					tarball_type = ""; //$NON-NLS-1$
				} else {
					return false;
				}

			// change to the directory and untar it, first get the path to the tarball
			int j = tarball_path.lastIndexOf(file_sep);
			String tar_path = tarball_path.substring(0,j);
			// now get the name of the tarball
			String tarball_name = tarball_path.substring(j+1);
			String tar_cmd = "cd " + tar_path + line_sep + "/bin/tar x" + //$NON-NLS-1$ //$NON-NLS-2$
						tarball_type + "f " + tarball_name; //$NON-NLS-1$

			try {
				LinuxShellCmds.createLinuxShellScript(tar_cmd, rpmbuild_logname, rpm_shell);
				LinuxShellCmds.executeLinuxCommand(rpm_shell, 0);
			} catch (CoreException e) {
				String throw_message = e.getMessage();
				IStatus error = new Status(IStatus.ERROR, Error, 1,
								throw_message, null);
				throw new CoreException(error);
			}

		return true;
		}

	/**
	 * The method takes care of renaming the source tarball in the SOURCES
	 * directory of the temporary RPM work area.  Usually the source tarball
	 * is named of the form: packagename-version#.tar.gz/bz2.  When the
	 * version number changes both the name of the tarball needs to change
	 * and the directory it creates (which is also usually of the format
	 * packagename-version).  The problem is that sometimes the
	 * "make clean/make dist-clean/make maintainer-clean does not totally
	 * remove files created by the auto* tools.  To get around this we use the
	 * original tarball from the source RPM.  We untar it, rename the directory
	 * it creates and then re-tar it with the new name.
	 * @param rpm_name - contains a string with the rpm name
	 * @param rpmbuild_logname - string pointing to the file where errors are being logged
	 * @param rpm_shell - string to use for creating a Linux shell script
	 * @param rpm_version - string containing the version of the rpm
	 * @param prev_ver_no - string with the previous version number of the rpm
	 * @return true if successful, throw CoreException if not
	 * @throws CoreException
	 */
	public boolean renameRPMtarfile(String rpm_name, String rpmbuild_logname,
	   String rpm_shell, String rpm_version, String prev_ver_no) throws CoreException {
		if (debug) {
			System.out.println("renameRPMtarfile"); //$NON-NLS-1$
		}

		String source_dir = rpmdirs_path + file_sep +"SOURCES" + file_sep; //$NON-NLS-1$
		String tarball_name = ""; //$NON-NLS-1$
		String tar_cmd_opt = ""; //$NON-NLS-1$
		String tar_extension = ""; //$NON-NLS-1$
		File f = new File(source_dir);
		String[] dirlist = f.list();


// Get the name of the current tarball
loop: 
		for (int i = 0; i < dirlist.length; i++) {
			if (dirlist[i].length() > rpm_name.length()) {
				String temp = dirlist[i].substring(0, rpm_name.length());

				if (temp.equals(rpm_name)) {
					if (dirlist[i].endsWith(".tar.bz2")) { //$NON-NLS-1$
						tarball_name = dirlist[i];
						tar_cmd_opt = "j"; //$NON-NLS-1$
						tar_extension = ".tar.bz2"; //$NON-NLS-1$

						break loop;
					} else if (dirlist[i].endsWith(".tar.gz")) { //$NON-NLS-1$
						tarball_name = dirlist[i];
						tar_cmd_opt = "z"; //$NON-NLS-1$
						tar_extension = ".tar.gz"; //$NON-NLS-1$

						break loop;
					}
				}
			}
		}

		// If tar_cmd_opt did not get set, we did not find a tarball
		if (tar_cmd_opt.equals("")) { //$NON-NLS-1$

			String throw_message = Messages.getString(
					"RPMCore.Cannot_find_source_tarball_in__644") + //$NON-NLS-1$
				source_dir;
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		// Set up to untar the source tarball
		String tar_cmd = "cd " + source_dir + line_sep + "/bin/tar -x" + tar_cmd_opt + //$NON-NLS-1$ //$NON-NLS-2$
			"f " + tarball_name; //$NON-NLS-1$

		try {
			LinuxShellCmds.createLinuxShellScript(tar_cmd, rpmbuild_logname, rpm_shell);
			LinuxShellCmds.executeLinuxCommand(rpm_shell, 0);
		} catch (CoreException e) {
			String throw_message = Messages.getString(
					"RPMCore.Error_trying_to_extract_the_source_tarball_using_this_command_--__652") + //$NON-NLS-1$
				tar_cmd;
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		File f1 = new File(source_dir + rpm_name + "-" + prev_ver_no); //$NON-NLS-1$

		if (f1.exists() & f1.isDirectory()) {
			File f2 = new File(source_dir + rpm_name + "-" + rpm_version); //$NON-NLS-1$

			if (!f2.exists()) {
				if (!f1.renameTo(f2)) {
					String throw_message = Messages.getString(
							"RPMCore.Error_trying_to_rename_directory__656") + //$NON-NLS-1$
						f1 + Messages.getString("RPMCore._to__") + f2; //$NON-NLS-1$
					IStatus error = new Status(IStatus.ERROR, Error, 1,
							throw_message, null);
					throw new CoreException(error);
				} else {
					tar_cmd = "/bin/tar -C " + source_dir + " -c" + //$NON-NLS-1$ //$NON-NLS-2$
						tar_cmd_opt + "f " + source_dir + rpm_name + "-" + //$NON-NLS-1$ //$NON-NLS-2$
						rpm_version + tar_extension + " ." + file_sep + rpm_name + //$NON-NLS-1$
						"-" + rpm_version; //$NON-NLS-1$

					try {
						LinuxShellCmds.executeLinuxCommand(tar_cmd, 0);
					} catch (CoreException e) {
						String throw_message = Messages.getString(
								"RPMCore.Error_trying_to_create_a_new_source_tarball_using_this_command_--__665") + //$NON-NLS-1$
							tar_cmd;
						IStatus error = new Status(IStatus.ERROR, Error, 1,
								throw_message, null);
						throw new CoreException(error);
					}
				}
			}
		}

		return true;
	}
	
	/**
	 * Method createRPMtarfile
	 * This method creates a tar file from a source directory in the RPM work area
	 * under the "/BUILD" directory and puts it in the "/SOURCES" directory for use
	 * by the "rpmbuild" command when it creates either a source or binary RPM.
	 * @param rpm_name - string with the name of the rpm
	 * @param rpm_version - string with the rpm verison number
	 * @param path_to_specfile - string with a path to the specfile
	 * @return returns true if successful, else it throws a CoreException
	 * @throws CoreException
	 */
	public boolean createRPMtarfile(String rpm_name, String rpm_version,
	   String path_to_specfile) 
	   throws CoreException {
		if (debug) {
			System.out.println("createRPMtarfile"); //$NON-NLS-1$
		}
		
		String tar_compression;
		String tar_suffix;
		String rpm_new_name = rpm_name + "-" + rpm_version; //$NON-NLS-1$
		// Figure out what the directory name is under "/BUILD" in the workarea is
		File f = new File(rpmdirs_path + file_sep + "BUILD"); //$NON-NLS-1$
		String[] dirlist = f.list();
		if (dirlist.length != 1) {
				  String throw_message = Messages.getString(
						  "RPMCore.There_are_too_many_directories_in__432") + //$NON-NLS-1$
						rpmdirs_path + file_sep + "BUILD"; //$NON-NLS-1$
				  IStatus error = new Status(IStatus.ERROR,
						  Error, 1, throw_message, null);
				  throw new CoreException(error);
			  }

		if (!dirlist[0].equals(rpm_new_name)) {
			File f1 = new File(rpmdirs_path + file_sep + "BUILD" + file_sep + dirlist[0]); //$NON-NLS-1$

			if (!f1.renameTo(new File(rpmdirs_path + file_sep + "BUILD" + file_sep + rpm_new_name))) { //$NON-NLS-1$

				String throw_message = Messages.getString(
						"RPMCore.Error_trying_to_rename_directory_in__438") + //$NON-NLS-1$
					rpmdirs_path + file_sep + "BUILD" + line_sep + //$NON-NLS-1$
					Messages.getString(
						"RPMCore.Permissions_problem__440"); //$NON-NLS-1$
				IStatus error = new Status(IStatus.ERROR, Error, 1,
						throw_message, null);
				throw new CoreException(error);
			}
		}

		// compression we need for the tar file, gzip or bzip2
			try {
				tar_suffix = LinuxShellCmds.checkCompression(path_to_specfile);
			} catch (CoreException e) {
				String throw_message = e.getMessage();
				IStatus error = new Status(IStatus.ERROR, Error, 1,
						throw_message, null);
				throw new CoreException(error);
			}
		
		if (tar_suffix.equals(".bz2")) { //$NON-NLS-1$
			tar_compression = "j"; //$NON-NLS-1$
		} else {
			tar_compression = "z"; //$NON-NLS-1$
		}

		String tar_cmd = "/bin/tar -C " + rpmdirs_path + file_sep + "BUILD" + " -c" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			tar_compression + "f " + rpmdirs_path + file_sep + "SOURCES" + file_sep + rpm_new_name + //$NON-NLS-1$ //$NON-NLS-2$
			".tar" + tar_suffix + " ." + file_sep + rpm_new_name; //$NON-NLS-1$ //$NON-NLS-2$

		try {
			LinuxShellCmds.executeLinuxCommand(tar_cmd, 0);
		} catch (CoreException e) {
			String throw_message = Messages.getString(
					"RPMCore.Error_trying_to_create_a_tarball_of_the_source_using_this_command_--__454") + //$NON-NLS-1$
				tar_cmd;
			IStatus error = new Status(IStatus.ERROR, Error, 1, throw_message,
					null);
			throw new CoreException(error);
		}

		return true;
	}
	
}